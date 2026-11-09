package org.kockpit.backend.security.local;

import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

/** Password reset links (db/kockpit-accounts V5 password_resets). */
public class PasswordResetRepository {

    public record PasswordReset(UUID id, UUID userId, Instant expiresAt, Instant usedAt) {

        /** Neither used nor expired at {@code now}. */
        public boolean isOpen(Instant now) {
            return usedAt == null && now.isBefore(expiresAt);
        }
    }

    private static final String COLUMNS = "id, user_id, expires_at, used_at";

    private final JdbcClient jdbc;

    public PasswordResetRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void create(UUID userId, String codeHash, UUID createdBy, Instant expiresAt) {
        jdbc.sql("""
                        insert into password_resets (id, user_id, code_hash, created_by, expires_at)
                        values (:id, :user, :hash, :by, :expires)""")
                .param("id", UUID.randomUUID()).param("user", userId).param("hash", codeHash).param("by", createdBy)
                .param("expires", OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
                .update();
    }

    /** The account's links not used yet: only its latest one works. */
    public void deleteOpen(UUID userId) {
        jdbc.sql("delete from password_resets where user_id = :user and used_at is null").param("user", userId).update();
    }

    public Optional<PasswordReset> findByCode(String codeHash) {
        return jdbc.sql("select " + COLUMNS + " from password_resets where code_hash = :hash")
                .param("hash", codeHash).query(this::reset).optional();
    }

    /** By code, locked until the end of the transaction: one use per link. */
    public Optional<PasswordReset> findByCodeForUpdate(String codeHash) {
        return jdbc.sql("select " + COLUMNS + " from password_resets where code_hash = :hash for update")
                .param("hash", codeHash).query(this::reset).optional();
    }

    public void markUsed(UUID id) {
        jdbc.sql("update password_resets set used_at = now() where id = :id").param("id", id).update();
    }

    private PasswordReset reset(ResultSet rs, int row) throws SQLException {
        OffsetDateTime usedAt = rs.getObject("used_at", OffsetDateTime.class);
        return new PasswordReset(rs.getObject("id", UUID.class), rs.getObject("user_id", UUID.class),
                rs.getObject("expires_at", OffsetDateTime.class).toInstant(), usedAt == null ? null : usedAt.toInstant());
    }
}
