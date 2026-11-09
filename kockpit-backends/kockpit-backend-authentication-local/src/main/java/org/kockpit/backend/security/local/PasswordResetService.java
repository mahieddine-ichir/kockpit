package org.kockpit.backend.security.local;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.security.local.PasswordResetRepository.PasswordReset;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Password reset links: an administrator creates one for an account and sends it himself (Kockpit
 * sends no mail). The code is 256 random bits, stored hashed, valid {@code password-reset-ttl},
 * once; a new link replaces the account's previous one. Choosing a new password ends the sessions
 * opened before it.
 */
@Slf4j
public class PasswordResetService {

    private static final String CODE_PREFIX = "kpr_";

    private static final int MAX_CODE_LENGTH = 100;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetRepository resets;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transaction;
    private final Duration ttl;
    private final Clock clock;

    public record Created(String email, Instant expiresAt, String code) {
    }

    public record Preview(String email, Instant expiresAt) {
    }

    public PasswordResetService(PasswordResetRepository resets, UserRepository users, PasswordEncoder passwordEncoder,
                                TransactionTemplate transaction, Duration ttl, Clock clock) {
        this.resets = resets;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.transaction = transaction;
        this.ttl = ttl;
        this.clock = clock;
    }

    /** A new link for the account of this email; empty when there is none. */
    public Optional<Created> create(String email, UUID createdBy) {
        return users.findByEmail(email).map(user -> {
            byte[] random = new byte[32];
            RANDOM.nextBytes(random);
            String code = CODE_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            Instant expiresAt = clock.instant().plus(ttl);
            transaction.executeWithoutResult(status -> {
                resets.deleteOpen(user.id());
                resets.create(user.id(), hash(code), createdBy, expiresAt);
            });
            log.info("Password reset link for {} created by {}", user.email(), createdBy);
            return new Created(user.email(), expiresAt, code);
        });
    }

    /** What the link is for; empty when it is unknown, used or expired. */
    public Optional<Preview> preview(String code) {
        return resets.findByCode(hashOf(code))
                .filter(reset -> reset.isOpen(clock.instant()))
                .flatMap(reset -> users.findById(reset.userId()).map(user -> new Preview(user.email(), reset.expiresAt())));
    }

    /** Changes the password and uses the link up; empty, with nothing changed, when the link can't be used. */
    public Optional<String> reset(String code, String newPassword) {
        return transaction.execute(status -> {
            Optional<PasswordReset> reset = resets.findByCodeForUpdate(hashOf(code)).filter(r -> r.isOpen(clock.instant()));
            Optional<LocalUser> user = reset.flatMap(r -> users.findById(r.userId()));
            if (user.isEmpty()) {
                return Optional.empty();
            }
            users.changePassword(user.get().id(), passwordEncoder.encode(newPassword));
            resets.markUsed(reset.get().id());
            log.info("Password of {} changed with a reset link", user.get().email());
            return Optional.of(user.get().email());
        });
    }

    private static String hashOf(String code) {
        return hash(code == null || !code.startsWith(CODE_PREFIX) || code.length() > MAX_CODE_LENGTH ? "" : code);
    }

    private static String hash(String code) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
