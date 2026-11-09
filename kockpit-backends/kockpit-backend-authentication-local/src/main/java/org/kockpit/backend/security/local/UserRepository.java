package org.kockpit.backend.security.local;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Users and their groups (tables of db/kockpit-accounts). Emails are compared lower-cased. */
public class UserRepository {

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Creates the user, or returns empty when the email is already taken. */
    public Optional<LocalUser> create(String email, String passwordHash) {
        UUID id = UUID.randomUUID();
        String normalized = normalize(email);
        // on conflict rather than catching the error, which would abort a surrounding transaction
        int inserted = jdbc.sql("insert into users (id, email, password_hash) values (:id, :email, :hash) on conflict do nothing")
                .param("id", id)
                .param("email", normalized)
                .param("hash", passwordHash)
                .update();
        return inserted == 1 ? Optional.of(new LocalUser(id, normalized, passwordHash, true)) : Optional.empty();
    }

    public Optional<LocalUser> findByEmail(String email) {
        return jdbc.sql("select id, email, password_hash, enabled from users where lower(email) = :email")
                .param("email", normalize(email))
                .query(LocalUser.class)
                .optional();
    }

    public Optional<LocalUser> findById(UUID id) {
        return jdbc.sql("select id, email, password_hash, enabled from users where id = :id")
                .param("id", id)
                .query(LocalUser.class)
                .optional();
    }

    public List<String> groups(UUID userId) {
        return jdbc.sql("select group_name from user_groups where user_id = :id order by group_name")
                .param("id", userId)
                .query(String.class)
                .list();
    }

    /** The group's accounts, by email. */
    public List<LocalUser> inGroup(String group) {
        return jdbc.sql("""
                        select u.id, u.email, u.password_hash, u.enabled
                        from users u join user_groups g on g.user_id = u.id
                        where g.group_name = :group
                        order by u.email""")
                .param("group", group)
                .query(LocalUser.class)
                .list();
    }

    /** Locks the group's memberships until the end of the transaction, and counts them. */
    public int lockGroup(String group) {
        return jdbc.sql("select group_name from user_groups where group_name = :group for update")
                .param("group", group)
                .query(String.class)
                .list()
                .size();
    }

    public void addGroup(UUID userId, String group) {
        jdbc.sql("insert into user_groups (user_id, group_name) values (:id, :group) on conflict do nothing")
                .param("id", userId)
                .param("group", group)
                .update();
    }

    public void removeGroup(UUID userId, String group) {
        jdbc.sql("delete from user_groups where user_id = :id and group_name = :group")
                .param("id", userId)
                .param("group", group)
                .update();
    }

    public void setEnabled(UUID userId, boolean enabled) {
        jdbc.sql("update users set enabled = :enabled where id = :id")
                .param("id", userId)
                .param("enabled", enabled)
                .update();
    }

    /** Sets the password and ends the sessions opened so far, see {@link #isSessionCurrent}. */
    public void changePassword(UUID userId, String passwordHash) {
        jdbc.sql("update users set password_hash = :hash, sessions_valid_after = now() where id = :id")
                .param("id", userId)
                .param("hash", passwordHash)
                .update();
    }

    /**
     * Whether a token issued at {@code issuedAt} is still good. Tokens count in whole seconds: one
     * issued right after a password change, in the same second, must pass.
     */
    public boolean isSessionCurrent(UUID userId, Instant issuedAt) {
        return jdbc.sql("""
                        select count(*) from users
                        where id = :id and (sessions_valid_after is null or date_trunc('second', sessions_valid_after) <= :issued)""")
                .param("id", userId)
                .param("issued", OffsetDateTime.ofInstant(issuedAt, ZoneOffset.UTC))
                .query(Integer.class)
                .single() == 1;
    }

    public void touchLogin(UUID userId) {
        jdbc.sql("update users set last_login_at = now() where id = :id")
                .param("id", userId)
                .update();
    }

    static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
