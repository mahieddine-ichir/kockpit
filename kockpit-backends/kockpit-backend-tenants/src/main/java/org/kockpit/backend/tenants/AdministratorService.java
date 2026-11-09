package org.kockpit.backend.tenants;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.security.local.LocalAuthProperties;
import org.kockpit.backend.security.local.LocalUser;
import org.kockpit.backend.security.local.UserRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Who administers the instance: the accounts in the administrators' group. Groups are read from
 * the database on each request, so a change takes effect at once. There is always one left, and
 * the account of {@code kockpit.security.local.admin.email} stays: startup would add it back.
 */
@Slf4j
public class AdministratorService {

    public record Administrator(UUID userId, String email, boolean configured) {
    }

    private final UserRepository users;
    private final TransactionTemplate transaction;
    private final String group;
    private final String configuredEmail;

    public AdministratorService(UserRepository users, TransactionTemplate transaction, LocalAuthProperties properties) {
        this.users = users;
        this.transaction = transaction;
        this.group = properties.getAdmin().getGroup();
        this.configuredEmail = normalize(properties.getAdmin().getEmail());
    }

    public List<Administrator> list() {
        return users.inGroup(group).stream().map(this::administrator).toList();
    }

    public Administrator add(UUID actorId, String email) {
        if (email == null || email.isBlank()) {
            throw TenantException.badRequest("email is required");
        }
        LocalUser user = users.findByEmail(email)
                .orElseThrow(() -> TenantException.notFound("No account for this email: it must be created first"));
        users.addGroup(user.id(), group);
        log.info("User {} made administrator by {}", user.email(), actorId);
        return administrator(user);
    }

    public void remove(UUID actorId, UUID userId) {
        // locked so that two administrators removing each other can't leave none
        transaction.executeWithoutResult(status -> {
            int administrators = users.lockGroup(group);
            LocalUser user = users.inGroup(group).stream()
                    .filter(u -> u.id().equals(userId))
                    .findFirst()
                    .orElseThrow(() -> TenantException.notFound("No administrator " + userId));
            if (user.email().equals(configuredEmail)) {
                throw TenantException.conflict("configured_admin",
                        "This administrator is set by the instance's configuration (auth.local.adminEmail): change it there");
            }
            if (administrators <= 1) {
                throw TenantException.conflict("last_admin", "The last administrator can't be removed");
            }
            users.removeGroup(userId, group);
            log.info("User {} no longer administrator, removed by {}", user.email(), actorId);
        });
    }

    private Administrator administrator(LocalUser user) {
        return new Administrator(user.id(), user.email(), user.email().equals(configuredEmail));
    }

    private static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
