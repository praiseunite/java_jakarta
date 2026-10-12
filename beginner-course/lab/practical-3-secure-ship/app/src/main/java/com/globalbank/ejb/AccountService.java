package com.globalbank.ejb;

import com.globalbank.entity.Account;
import com.globalbank.events.TransferCompletedEvent;
import com.globalbank.interceptors.Logged;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Every operation the bank can perform on an account.
 *
 * <p>{@code @Stateless} means the container keeps a pool of these and hands one
 * to each request. There is no state held between calls, which is exactly why
 * they can be shared safely and recycled instantly.</p>
 *
 * <p>This class is where Practical 1's whole point lives: <strong>you never
 * write SQL</strong>. You load an object, change it, and the container writes
 * the UPDATE when the transaction commits.</p>
 *
 * <p>Practical 2 wrapped a web page, a queue and a remote interface around it.
 * <strong>Practical 3 adds two things that cut across all of it</strong>: who is
 * allowed to call each method, and what else should happen automatically when a
 * transfer succeeds.</p>
 *
 * <h2>Who may call what — three annotations, and one rule that catches everybody</h2>
 * <table>
 *   <caption>the security annotations on this class</caption>
 *   <tr><th>Annotation</th><th>Means</th></tr>
 *   <tr><td>{@code @DeclareRoles}</td><td>These are the role names this bean uses.
 *       A declaration, so a typo shows up as a role nobody holds.</td></tr>
 *   <tr><td>{@code @RolesAllowed("CUSTOMER")}</td><td>Only a caller holding that
 *       role gets in. Anybody else gets an {@code EJBAccessException} and the
 *       method body never runs.</td></tr>
 *   <tr><td>{@code @PermitAll}</td><td>Anybody may call it — including a caller
 *       with no identity at all.</td></tr>
 * </table>
 *
 * <p><strong>The rule that catches everybody:</strong> an unannotated method on a
 * bean that has security annotations <em>anywhere</em> is not protected by
 * default — it is <strong>unchecked</strong>, which means open to all. There is
 * no "deny by default" here. The only safe habit is the one this class follows:
 * annotate every public method, and let a reviewer see at a glance that none was
 * missed.</p>
 *
 * <p>Which is also why {@code @PermitAll} is written out rather than left off
 * {@link #transferOrRefuse}. Leaving it off would look the same, behave the same,
 * and mean something completely different to the next person reading the file.
 * Say what you mean.</p>
 */
@LocalBean
@Stateless
@Logged
@DeclareRoles({"CUSTOMER", "BANK_MANAGER"})
public class AccountService implements AccountServiceRemote {

    /**
     * The container-managed connection to the database, bound to the
     * persistence unit in persistence.xml. An EntityManager is not a
     * connection — it is a workspace that tracks the objects you have loaded
     * and works out what SQL is needed.
     */
    @PersistenceContext
    private EntityManager em;

    /**
     * Another enterprise bean, injected by the container.
     * {@code @Inject} would work here too; {@code @EJB} is the older, explicit
     * form and says "this is an enterprise bean" out loud.
     */
    @EJB
    private AccountNumberGenerator numbers;

    /**
     * The announcement channel — see {@link com.globalbank.events.TransferNotifier}
     * for who is listening.
     *
     * <p>The type parameter is the news, not the paper. {@code Event<T>} means
     * "I can fire events whose payload is a {@code T}". Firing sends it to
     * everybody observing a compatible type, and this class has no idea how many
     * listeners there are, or whether there are any at all. Firing an event
     * nobody observes is not an error; it is a no-op, which is what keeps the
     * sender honest about not caring.</p>
     */
    @Inject
    private Event<TransferCompletedEvent> transfers;

    // ==================================================================
    // Reading
    // ==================================================================

    /**
     * Load one account by its id.
     *
     * <p>No {@code @TransactionAttribute} is needed: reading a single row is a
     * single operation, and the container supplies a transaction anyway if one
     * is not already running.</p>
     *
     * @throws IllegalArgumentException if there is no such account
     */
    @RolesAllowed({"CUSTOMER", "BANK_MANAGER"})
    public Account find(int id) {
        Account account = em.find(Account.class, id);
        if (account == null) {
            throw new IllegalArgumentException("No account with id " + id);
        }
        return account;
    }

    /**
     * Every account, in id order.
     *
     * <p>That string is <strong>JPQL</strong> — SQL written against your Java
     * classes. Note {@code Account} (the class) and {@code a.id} (the field),
     * not the table and column names. JPA translates it.</p>
     */
    @RolesAllowed({"CUSTOMER", "BANK_MANAGER"})
    public List<Account> findAll() {
        // Practical 2 changed exactly one thing in this method: the copy.
        //
        // It is not decoration. This class now implements AccountServiceRemote,
        // so a program outside the server can call findAll() — and a remote call
        // has to serialise the result. The List a JPA provider hands back is not
        // guaranteed to be serialisable; Hibernate's happens to be an ArrayList,
        // another provider's might not be. Copying into a plain ArrayList makes
        // the method safe for both callers at the cost of one small object.
        return new ArrayList<>(em.createQuery("SELECT a FROM Account a ORDER BY a.id", Account.class)
                 .getResultList());
    }

    // ==================================================================
    // Changing
    // ==================================================================

    /**
     * Open a new account and return it.
     *
     * <p>{@code REQUIRED} is the default and the one you will use almost always.
     * It means: "if the caller already has a transaction, join it; if not, start
     * one." Writing it out is a reminder that a transaction exists at all.</p>
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed("CUSTOMER")
    public Account open(String holder, BigDecimal openingBalance) {
        if (holder == null || holder.isBlank()) {
            throw new IllegalArgumentException("A holder name is required");
        }
        if (openingBalance == null || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("An opening balance cannot be negative");
        }
        Account account = new Account(numbers.nextAccountNumber(), holder, openingBalance);

        // persist() means "this object is new; INSERT it when the transaction
        // commits". It does not run SQL yet.
        em.persist(account);
        return account;
    }

    /** Add money. Returns the updated account. */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed("CUSTOMER")
    public Account deposit(int id, BigDecimal amount) {
        requirePositive(amount);

        Account account = find(id);

        // No update() call, and no save(). We just change the object. The
        // EntityManager is tracking it, so at commit time JPA compares the
        // object with what it loaded and issues the UPDATE for us.
        account.setBalance(account.getBalance().add(amount));

        return account;
    }

    /**
     * Take money out — or refuse.
     *
     * <p>The refusal is a checked exception marked {@code rollback = true}, so
     * the transaction is undone rather than half-applied.</p>
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed("CUSTOMER")
    public Account withdraw(int id, BigDecimal amount) throws InsufficientFundsException {
        requirePositive(amount);

        Account account = find(id);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Account " + id + " holds " + account.getBalance().toPlainString()
                            + ", so " + amount.toPlainString() + " cannot be withdrawn");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return account;
    }

    // ==================================================================
    // Transfer — and the refactor that security forced
    // ==================================================================

    /**
     * Move money between two accounts — the operation that only makes sense if
     * it is all-or-nothing, and the reason transactions exist.
     *
     * <p>Two writes happen here: one debit, one credit. If the credit failed
     * after the debit had been written, money would vanish. It cannot: both
     * changes are part of this one transaction, so either both are committed or
     * neither is.</p>
     *
     * <p>Compare with {@link #withdraw}: the balance check happens
     * <em>before</em> any change, so a refusal here leaves nothing to undo.</p>
     *
     * <p>This method is on {@link AccountServiceRemote}, so it is reachable from
     * outside the server — and now carries {@code @RolesAllowed}, which means the
     * standalone client from Practical 2 must supply a user name and password
     * before it can move anything. Adding a rule changed what a caller has to do;
     * that is what security is.</p>
     *
     * @return a one-line receipt
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @RolesAllowed({"CUSTOMER", "BANK_MANAGER"})
    public String transfer(int fromId, int toId, BigDecimal amount) throws InsufficientFundsException {
        return moveMoney(fromId, toId, amount);
    }

    /**
     * The same transfer, with the refusal reported as a value instead of an
     * exception.
     *
     * <p>{@link com.globalbank.messaging.TransferMDB} needs this, and the reason
     * is not obvious — it is one of the sharpest edges in the whole course, so
     * read it properly.</p>
     *
     * <p>{@link #transfer} throws {@link InsufficientFundsException}, and that
     * exception is marked {@code @ApplicationException(rollback = true)}. The
     * moment it leaves a bean method, the container marks the current transaction
     * rollback-only — and <strong>no catch block anywhere can undo that</strong>.
     * So a message-driven bean that catches the refusal still sees its
     * transaction roll back, which means the message is delivered again. A
     * refusal that can never succeed then loops until the broker gives up.</p>
     *
     * <p>The fix is to refuse without ever throwing, which is what this method
     * does: it catches the refusal and returns a sentence. A returned value is a
     * success, so the transaction commits and the message is acknowledged.</p>
     *
     * <h2>Why this method must be {@code @PermitAll}</h2>
     * <p>It is called by a message-driven bean, and <strong>an MDB has no
     * caller</strong>. There is no user, no session and no principal — the
     * container delivered a message from a queue. A {@code @RolesAllowed} here
     * would therefore fail every single time, and because the failure arrives as
     * an exception it would roll the transaction back and the message would be
     * redelivered forever: the poison-message loop of Session 7, arriving through
     * the security door instead of the transaction one.</p>
     *
     * <p>So this method is open. Saying that plainly is uncomfortable and it
     * should be: an open method on the bank is worth being uncomfortable about.
     * It is acceptable here for a reason you can check rather than a reason you
     * have to trust — {@code transferOrRefuse} is <em>not</em> on
     * {@link AccountServiceRemote}, so no program outside the server can reach it.
     * Its only caller is the queue.</p>
     *
     * @return a receipt, or a sentence beginning {@code "REFUSED: "}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @PermitAll
    public String transferOrRefuse(int fromId, int toId, BigDecimal amount) {
        try {
            return moveMoney(fromId, toId, amount);
        } catch (InsufficientFundsException | IllegalArgumentException e) {
            // Both refusals the bank can make are caught here on purpose:
            // IllegalArgumentException covers "same account" and a bad amount.
            // Anything else — a database failure, say — is deliberately left to
            // propagate, because that one really should be retried.
            return "REFUSED: " + e.getMessage();
        }
    }

    /**
     * The bank's one and only way to move money. <strong>Private, and
     * deliberately not annotated.</strong>
     *
     * <p>Practical 2 had {@code transferOrRefuse} call {@code transfer} directly.
     * That worked, and it was an accident waiting to happen: because the call
     * goes to {@code this} and not through the container, no interception happens
     * — so {@code transfer}'s {@code @RolesAllowed} was simply never consulted.
     * The security check was bypassed by the shape of the code rather than by any
     * decision.</p>
     *
     * <p>The day that breaks is the day somebody "tidies up" the self-invocation:
     * changes it to go through the container (a very reasonable-looking refactor),
     * and the MDB — which has no identity — is suddenly refused by the security
     * check and the queue jams. Nothing in {@code transferOrRefuse} would look
     * wrong.</p>
     *
     * <p>So the two entry points now share this private method instead, and the
     * security rule lives only where it is meant to apply. A private method is not
     * a business method: it is invisible to callers, never intercepted, and no
     * annotation on it could do anything. That is exactly the property wanted
     * here — a place with no security semantics to accidentally inherit from
     * either door.</p>
     *
     * @throws InsufficientFundsException if the source account cannot cover it
     */
    private String moveMoney(int fromId, int toId, BigDecimal amount)
            throws InsufficientFundsException {

        requirePositive(amount);

        if (fromId == toId) {
            throw new IllegalArgumentException("Cannot transfer from an account to itself");
        }

        Account from = find(fromId);
        Account to = find(toId);

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Account " + fromId + " holds " + from.getBalance().toPlainString()
                            + ", so " + amount.toPlainString() + " cannot be transferred");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        // Announce it. Note where this line sits: after both balances have been
        // changed, but with no commit yet — the container commits when this
        // method's transaction ends.
        //
        // That timing is the whole reason TransferNotifier's observer is declared
        // `during = AFTER_SUCCESS`. Firing here only says "this is about to have
        // happened"; the container holds the notification until the commit makes
        // it true. If the commit fails, the announcement is simply never made.
        transfers.fire(new TransferCompletedEvent(
                fromId, toId, amount, from.getHolder(), to.getHolder()));

        return "Transferred " + amount.toPlainString()
                + " from " + fromId + " (" + from.getHolder() + ")"
                + " to " + toId + " (" + to.getHolder() + ")";
    }

    /** One rule, checked in one place, used by every method above. */
    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero");
        }
    }
}
