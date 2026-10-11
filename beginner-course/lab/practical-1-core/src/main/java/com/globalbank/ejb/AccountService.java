package com.globalbank.ejb;

import com.globalbank.entity.Account;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
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
 */
@Stateless
public class AccountService {

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

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    /**
     * Load one account by its id.
     *
     * <p>No {@code @TransactionAttribute} is needed: reading a single row is a
     * single operation, and the container supplies a transaction anyway if one
     * is not already running.</p>
     *
     * @throws IllegalArgumentException if there is no such account
     */
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
    public List<Account> findAll() {
        return em.createQuery("SELECT a FROM Account a ORDER BY a.id", Account.class)
                 .getResultList();
    }

    // ------------------------------------------------------------------
    // Changing
    // ------------------------------------------------------------------

    /**
     * Open a new account and return it.
     *
     * <p>{@code REQUIRED} is the default and the one you will use almost always.
     * It means: "if the caller already has a transaction, join it; if not, start
     * one." Writing it out is a reminder that a transaction exists at all.</p>
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
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
     * @return a one-line receipt
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String transfer(int fromId, int toId, BigDecimal amount) throws InsufficientFundsException {
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
