package org.kockpit.backend.security.local;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * The invitations a new account signs up with, when {@code signup.invite-required} is on. Implemented
 * by whoever stores them (kockpit-backend-tenants); without an implementation nobody can sign up.
 */
public interface SignupInvitations {

    /**
     * Creates the account if the invitation is valid for this email, and uses it up in the same
     * transaction. {@code createAccount} runs only for a valid invitation.
     *
     * @return the account, or empty when the email is taken (the invitation stays usable)
     * @throws Refused when the invitation can't be used
     */
    Optional<LocalUser> redeem(String code, String email, Supplier<Optional<LocalUser>> createAccount);

    /** Answered as {@code {error, message}} with its status. */
    @Getter
    class Refused extends RuntimeException {

        private final HttpStatus status;

        private final String error;

        public Refused(HttpStatus status, String error, String message) {
            super(message);
            this.status = status;
            this.error = error;
        }
    }
}
