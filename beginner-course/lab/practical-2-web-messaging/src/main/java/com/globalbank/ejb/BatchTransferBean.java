package com.globalbank.ejb;

import com.globalbank.entity.Account;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

import java.math.BigDecimal;

/**
 * A batch of transfers, with the transactions written <strong>by hand</strong>.
 *
 * <p>{@code @TransactionManagement(BEAN)} switches off everything the container
 * used to do for you. That sounds like a downgrade, and it usually is — but it is
 * the only way to get transaction <em>policies</em> the container cannot express.
 * This class has two methods that differ in exactly one decision: <em>when do we
 * commit?</em></p>
 *
 * <h2>The trade you are making</h2>
 * <table>
 *   <caption>Container-managed vs bean-managed</caption>
 *   <tr><th></th><th>Container-managed (CMT)</th><th>Bean-managed (BMT)</th></tr>
 *   <tr><td>Who calls begin/commit</td><td>the container</td><td>you</td></tr>
 *   <tr><td>Rollback on exception</td><td>automatic, by the rules</td><td>your problem</td></tr>
 *   <tr><td>Can one method commit half its work?</td><td>never</td><td>yes, if you ask for it</td></tr>
 *   <tr><td>Can it join a caller's transaction?</td><td>yes</td><td>no</td></tr>
 * </table>
 *
 * <p>That last row is the reason BMT is rare. A bean-managed transaction
 * <strong>cannot join</strong> somebody else's — so if your boss later wraps your
 * batch in a bigger operation, your batch will not be part of it, and a rollback
 * outside will not undo your work. Use BMT when you genuinely need to choose the
 * boundaries, and not before.</p>
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class BatchTransferBean {

    /**
     * With {@code BEAN} management, the transaction must come from somewhere, and
     * this is it. {@code UserTransaction} is the handle the application server
     * gives your code so it can drive transactions itself.
     *
     * <p>{@code @Resource} rather than {@code @PersistenceContext}: this is an
     * application-server resource, not a JPA object.</p>
     */
    @Resource
    private UserTransaction ut;

    /**
     * The EntityManager is still container-managed. In a bean-managed bean it
     * simply follows whatever transaction you have open at the time — so every
     * {@code em} call must happen between your {@code begin()} and your
     * {@code commit()}.
     */
    @PersistenceContext
    private EntityManager em;

    /** The audit log, written in its own transaction (see {@link AuditService}). */
    @EJB
    private AuditService audit;

    /**
     * All-or-nothing, by hand: one transaction for the whole batch.
     *
     * <p>This method produces the <em>same</em> guarantee that
     * {@code AccountService.transfer} got for free — and that is the point. Read
     * it and notice how much code exists only to reproduce behaviour the container
     * was giving you anyway.</p>
     *
     * <p>If any single transfer is refused, {@code ut.rollback()} undoes the ones
     * that already succeeded, and the caller is told which one failed.</p>
     *
     * @return a sentence describing what happened
     */
    public String transferAllOrNothing(int fromId, BigDecimal perTransfer, int... toIds) {
        int completed = 0;
        try {
            ut.begin();

            Account from = require(fromId);
            for (int toId : toIds) {
                Account to = require(toId);
                debit(from, perTransfer, toId);
                to.setBalance(to.getBalance().add(perTransfer));
                completed++;
            }

            ut.commit();
            audit.record("BATCH", "OK",
                    "Moved " + perTransfer.toPlainString() + " to " + completed + " account(s)");
            return "Committed all " + completed + " transfer(s).";

        } catch (Exception e) {
            // Undo everything the loop managed to do. Note that this is OUR job
            // here — the container will not do it for us in a BMT bean.
            try {
                ut.rollback();
            } catch (Exception rollbackFailure) {
                // Nothing useful can be done at this point beyond saying so.
                return "rollback itself failed: " + rollbackFailure;
            }
            audit.record("BATCH", "REFUSED",
                    "Rolled back after " + completed + " transfer(s): " + e.getMessage());
            return "REFUSED, nothing was moved (failed on transfer #" + (completed + 1) + "): "
                    + e.getMessage();
        }
    }

    /**
     * The opposite policy: one transaction <em>per transfer</em>, so the ones that
     * work are kept and only the failure is discarded.
     *
     * <p>No CMT bean can do this. With {@code REQUIRED}, the whole batch would be
     * one unit and a single bad transfer would erase the good ones. This method is
     * the honest reason BMT exists: sometimes partial success is the correct
     * business rule — "pay every supplier you can, and report the ones you could
     * not".</p>
     */
    public String transferOneTransactionEach(int fromId, BigDecimal perTransfer, int... toIds) {
        int succeeded = 0;
        int refused = 0;
        StringBuilder failures = new StringBuilder();

        for (int toId : toIds) {
            try {
                ut.begin();
                Account from = require(fromId);
                Account to = require(toId);
                debit(from, perTransfer, toId);
                to.setBalance(to.getBalance().add(perTransfer));
                ut.commit();
                succeeded++;
            } catch (Exception e) {
                try {
                    ut.rollback();
                } catch (Exception ignored) {
                    // The item is already lost; carry on with the rest.
                }
                refused++;
                if (failures.length() > 0) {
                    failures.append("; ");
                }
                failures.append(toId).append(": ").append(e.getMessage());
            }
        }

        audit.record("BATCH-PER-ITEM", refused == 0 ? "OK" : "PARTIAL",
                succeeded + " succeeded, " + refused + " refused");

        return succeeded + " transfer(s) committed, " + refused + " refused"
                + (failures.length() == 0 ? "." : " -> " + failures);
    }

    // ------------------------------------------------------------------
    // Helpers — deliberately duplicated from AccountService, so that the
    // transaction handling above is the only thing this class is about.
    // ------------------------------------------------------------------

    private Account require(int id) {
        Account account = em.find(Account.class, id);
        if (account == null) {
            throw new IllegalArgumentException("No account with id " + id);
        }
        return account;
    }

    private static void debit(Account from, BigDecimal amount, int toId)
            throws InsufficientFundsException {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero");
        }
        if (from.getBalance().compareTo(amount) < 0) {
            // Exactly the same refusal AccountService throws — and yet nothing
            // rolls back here, because @ApplicationException(rollback = true)
            // only means something for CONTAINER-managed transactions. In a
            // bean-managed bean, undoing the work is our job. That is the whole
            // trade written down in one sentence.
            throw new InsufficientFundsException("Account " + from.getId() + " holds "
                    + from.getBalance().toPlainString() + ", so " + amount.toPlainString()
                    + " cannot be sent to " + toId);
        }
        from.setBalance(from.getBalance().subtract(amount));
    }
}
