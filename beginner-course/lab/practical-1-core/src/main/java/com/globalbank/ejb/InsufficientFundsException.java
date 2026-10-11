package com.globalbank.ejb;

import jakarta.ejb.ApplicationException;

/**
 * Thrown when an account does not hold enough money for a withdrawal or transfer.
 *
 * <p>The interesting part is the annotation, not the class. Session 4's rule:</p>
 * <ul>
 *   <li>An <strong>unchecked</strong> exception (a RuntimeException) makes the
 *       container roll the transaction back automatically.</li>
 *   <li>A <strong>checked</strong> exception does <em>not</em> — the container
 *       assumes the caller will handle it and leaves the transaction alone.</li>
 * </ul>
 *
 * <p>This is a checked exception (it extends {@link Exception}, not
 * {@link RuntimeException}), so without the annotation below a failed transfer
 * would leave the debit in place. {@code rollback = true} tells the container to
 * treat it like a system failure and undo the transaction.</p>
 */
@ApplicationException(rollback = true)
public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
