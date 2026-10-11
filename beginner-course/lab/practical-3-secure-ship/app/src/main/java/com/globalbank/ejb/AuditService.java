package com.globalbank.ejb;

import com.globalbank.entity.AuditEntry;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * The bank's audit log — and the clearest example of
 * {@link TransactionAttributeType#REQUIRES_NEW} you will meet in this course.
 *
 * <p>An audit log has a strange requirement: it must record things that
 * <em>failed</em>. "Somebody tried to move £9,000 out of account 1002 and the
 * bank refused" is the most valuable line in the whole log — and it is precisely
 * the line that a normal transaction would throw away, because the transfer it
 * describes was rolled back.</p>
 *
 * <p>{@code REQUIRES_NEW} solves that. It tells the container:</p>
 * <blockquote><em>"Suspend whatever transaction the caller is in. Start a brand
 * new one for me. Commit mine when I finish, and then resume theirs."</em></blockquote>
 *
 * <p>So the audit line is committed on its own, before the transfer it describes
 * has succeeded or failed. If the transfer later rolls back, the audit line is
 * already safely written and is untouched.</p>
 *
 * <p>Compare the alternatives, because the difference is exactly what Session 10
 * is about:</p>
 * <ul>
 *   <li>{@code REQUIRED} (the default) — join the caller's transaction. A rollback
 *       would erase the audit line along with the transfer. Useless for auditing.</li>
 *   <li>{@code REQUIRES_NEW} — a separate transaction that survives the caller's
 *       rollback. This is what we want.</li>
 *   <li>{@code MANDATORY} — refuse to run at all unless the caller already has a
 *       transaction. Good for "this must never run outside a transaction" rules.</li>
 * </ul>
 *
 * <h2>Practical 3 splits this bean's two jobs apart</h2>
 * <p>Writing to the log and reading from it look like two halves of one thing, and
 * they could hardly be more different:</p>
 * <table>
 *   <caption>two methods, two audiences</caption>
 *   <tr><th>Method</th><th>Who calls it</th><th>Rule</th></tr>
 *   <tr>
 *     <td>{@link #record}</td>
 *     <td>the application itself, on behalf of whoever just did something</td>
 *     <td>{@code @PermitAll} — a customer must be able to have their own
 *         transfer recorded</td>
 *   </tr>
 *   <tr>
 *     <td>{@link #recent}</td>
 *     <td>a person looking at the whole bank's activity</td>
 *     <td>{@code @RolesAllowed("BANK_MANAGER")} — the log is not the customer's
 *         to read</td>
 *   </tr>
 * </table>
 *
 * <p>This is the distinction between an <em>action</em> and a <em>view</em>, and
 * it is where role-based security earns its keep. A customer who may not see the
 * audit log is still perfectly entitled to be <em>in</em> it.</p>
 */
@Stateless
@DeclareRoles({"CUSTOMER", "BANK_MANAGER"})
public class AuditService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Write one line to the audit log, in a transaction of its own.
     *
     * <p>Note that no caller has to think about this. {@code AccountService}
     * simply calls {@code audit.record(...)} and carries on; the container
     * suspends and resumes transactions around the call.</p>
     *
     * <p>{@code @PermitAll} because the caller is a member of staff or a customer
     * going about their business, and every one of them needs their action
     * recorded. Nobody is ever asked "what role are you?" on the way in here.</p>
     *
     * @param action  what was attempted, e.g. {@code "TRANSFER"}
     * @param outcome {@code "OK"} or {@code "REFUSED"}
     * @param detail  the bank's own explanation, shown on the web page
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @PermitAll
    public void record(String action, String outcome, String detail) {
        em.persist(new AuditEntry(action, outcome, detail));
    }

    /**
     * The newest lines first, for the web page.
     *
     * <p>Deliberately capped: an audit log grows forever, and a page that tried to
     * show all of it would fall over. Real systems page through it.</p>
     *
     * <p><strong>The cap is a parameter, not a constant, and that is the point of
     * the {@code @AuditDepth} qualifier.</strong> Twenty-five is a reasonable
     * default, but "how much of the bank's history does an operator see" is a
     * policy question, not a code question — so it comes from configuration. The
     * value arrives through a producer method in
     * {@link com.globalbank.config.BankConfiguration} and is injected wherever it
     * is needed; nothing here has to know whether it came from a system property,
     * a database row or a file.</p>
     *
     * @param max how many lines to return; the caller supplies it
     */
    @RolesAllowed("BANK_MANAGER")
    public List<AuditEntry> recent(int max) {
        return em.createQuery("SELECT a FROM AuditEntry a ORDER BY a.id DESC", AuditEntry.class)
                 .setMaxResults(max)
                 .getResultList();
    }
}
