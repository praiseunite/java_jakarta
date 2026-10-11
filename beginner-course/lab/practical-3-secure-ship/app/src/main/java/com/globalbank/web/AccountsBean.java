package com.globalbank.web;

import com.globalbank.ejb.AccountService;
import com.globalbank.ejb.AuditService;
import com.globalbank.ejb.InsufficientFundsException;
import com.globalbank.entity.Account;
import com.globalbank.entity.AuditEntry;
import com.globalbank.messaging.TransferQueueSender;
import com.globalbank.qualifiers.AuditDepth;
import com.globalbank.qualifiers.BankName;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

import java.io.Serializable;
import java.util.List;

/**
 * The thing behind {@code accounts.xhtml} — the "backing bean".
 *
 * <p>The page never touches a database, a bean or an exception. It calls methods
 * on this class through their names in the view ({@code #{bank.accounts}}), and
 * this class does the real work. Keeping that split is what lets you change the
 * page without touching the bank, and the bank without touching the page.</p>
 *
 * <h2>The two annotations</h2>
 * <ul>
 *   <li>{@code @Named("bank")} gives this object a <strong>name in the
 *       view</strong>. That single word is the whole wiring: no registration, no
 *       lookup, no {@code new}. The page writes {@code #{bank.message}} and the
 *       container finds this object.</li>
 *   <li>{@code @ViewScoped} gives it a <strong>lifetime</strong>. One instance is
 *       created when the page opens and it lives until the user leaves that page.
 *       Any number of clicks, and the same object answers them all.</li>
 * </ul>
 *
 * <p>Session 5 taught {@code @SessionScoped}. This bean uses
 * {@code @ViewScoped} instead, and the reason is worth understanding rather than
 * copying: a session-scoped bean survives until the <em>browser</em> is closed,
 * so two browser tabs share one instance and yesterday's half-typed form is still
 * in memory. A view-scoped bean dies with the page. For a form like this one —
 * where the data belongs to the page, not to the person — view scope is the
 * honest choice.</p>
 *
 * <h2>Why there is a cache in here</h2>
 * <p>{@link #getAccounts()} calls the bank only once per page, then remembers the
 * answer. That is only possible <em>because</em> the bean survives between
 * requests — which is precisely what the scope buys you, and the reason this
 * class is worth reading twice.</p>
 */
@Named("bank")
@ViewScoped
public class AccountsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AccountService service;

    @EJB
    private AuditService audit;

    @EJB
    private TransferQueueSender queue;

    /**
     * Who is signed in, and what may they do — the standard Jakarta Security API.
     *
     * <p>Two questions, two methods, and the difference between them is the whole
     * of authentication versus authorization:</p>
     * <ul>
     *   <li>{@code getCallerPrincipal().getName()} — <em>who is this?</em> Returns
     *       a name. Knowing a name grants nothing.</li>
     *   <li>{@code isCallerInRole("BANK_MANAGER")} — <em>may they do this?</em>
     *       Returns a boolean, and this is the one you are allowed to make a
     *       decision with.</li>
     * </ul>
     *
     * <p>A page that shows more because the <em>name</em> is familiar is a page
     * with a hard-coded list of privileged people in it. Ask about roles; roles
     * are the thing that can be changed without a redeploy.</p>
     */
    @Inject
    private SecurityContext security;

    /**
     * How many audit lines to show, from the producer method in
     * {@link com.globalbank.config.BankConfiguration}.
     *
     * <p>An {@code int} injected by qualifier. Nothing here reads a system
     * property, and nothing here has a default of 25 baked into it — this class
     * does not know or care where the number comes from. That is the payoff for
     * the producer: the decision lives in one place, and every consumer just
     * receives the answer.</p>
     */
    @Inject
    @AuditDepth
    private int auditDepth;

    /**
     * The bank's name, from the producer field in
     * {@link com.globalbank.config.BankConfiguration}.
     *
     * <p>Two producers, two shapes, one class. This one is a field and could not
     * be anything else — see {@link BankName} for why a producer of a
     * {@code String} must not carry a normal scope.</p>
     */
    @Inject
    @BankName
    private String bankName;

    /** The form's data. JSF fills it in from the submitted values. */
    private TransferRequest transfer = new TransferRequest();

    /** The last thing that happened, in words, for the page to show. */
    private String message;

    /** True when {@link #message} describes a refusal, so the page can colour it. */
    private boolean refused;

    // null means "not loaded yet", which is why the getters use it as the flag.
    private List<Account> accounts;
    private List<AuditEntry> auditLines;

    // ------------------------------------------------------------------
    // What the page reads
    // ------------------------------------------------------------------

    /**
     * Every account.
     *
     * <p>Called several times per page render by JSF, which is exactly why the
     * result is cached: without the cache this would be several database round
     * trips for one screen.</p>
     */
    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = service.findAll();
        }
        return accounts;
    }

    /**
     * The newest audit lines, for the table at the bottom of the page.
     *
     * <p>Read the first line of the body before anything else: a customer never
     * reaches the bank at all.</p>
     *
     * <p>That is not an optimization and it is not politeness. The page is
     * rendered for whoever is signed in, and <strong>JSF calls this getter
     * whether or not the table that uses it will be rendered</strong> —
     * {@code rendered="false"} hides markup, it does not stop the expression
     * inside it being evaluated. So a customer loading this page would call
     * {@code audit.recent(...)}, which is {@code @RolesAllowed("BANK_MANAGER")},
     * and the container would throw an {@code EJBAccessException} straight into
     * the middle of the page render. The page would not show an empty table; it
     * would fail.</p>
     *
     * <p>The lesson generalises: with role-based security on the server, the
     * page's rendering conditions must agree with the beans' rules. Checking
     * {@code isManager()} here, and rendering on {@code bank.manager} in the
     * Facelet, is one condition written twice — and the two copies are the price
     * of the split.</p>
     */
    public List<AuditEntry> getAuditLines() {
        if (!isManager()) {
            // An empty list, not null: #{bank.auditLines} is also used in an
            // "empty" test, and #{empty null} and #{empty []} both behave as
            // "nothing to show", so the page needs no special case.
            return List.of();
        }
        if (auditLines == null) {
            auditLines = audit.recent(auditDepth);
        }
        return auditLines;
    }

    /** The signed-in user's name, for the page header. */
    public String getCallerName() {
        return security.getCallerPrincipal().getName();
    }

    /**
     * Whether the caller holds {@code BANK_MANAGER}.
     *
     * <p>Named without a {@code get}, so the Facelet writes {@code #{bank.manager}}
     * rather than {@code #{bank.getManager}} — JSF accepts both spellings for a
     * boolean, but this one reads as the question it is.</p>
     */
    public boolean isManager() {
        return security.isCallerInRole("BANK_MANAGER");
    }

    /** The configured bank name, for the page title. */
    public String getBankName() {
        return bankName;
    }

    public TransferRequest getTransfer() {
        return transfer;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRefused() {
        return refused;
    }

    // ------------------------------------------------------------------
    // What the page does
    // ------------------------------------------------------------------

    /**
     * "Transfer now" — carry the transfer out immediately, in this request.
     *
     * <p>By the time this method runs, every constraint on {@link TransferRequest}
     * has already been checked and the form has been rejected if any of them
     * failed. So there is no validation code here either. This method only has to
     * deal with the one refusal a rule cannot predict: not enough money.</p>
     */
    public void submit() {
        try {
            String receipt = service.transfer(
                    transfer.getFromAccount(),
                    transfer.getToAccount(),
                    transfer.getAmount());

            // The transfer is committed by now. Writing the audit line here means
            // it is a SECOND, independent transaction — see AuditService.
            audit.record("TRANSFER", "OK", receipt);

            message = receipt;
            refused = false;
            transfer = new TransferRequest();

        } catch (InsufficientFundsException | IllegalArgumentException e) {
            // A refusal is a normal business answer, not a bug: the bank said no
            // and nothing was moved. We record it and tell the user, in that order.
            audit.record("TRANSFER", "REFUSED", e.getMessage());
            message = e.getMessage();
            refused = true;
        }

        // Something changed, so the cached lists are stale.
        forget();
    }

    /**
     * "Queue it instead" — hand the transfer to the message broker and return
     * straight away.
     *
     * <p>Compare this with {@link #submit()} above. That one finishes the job
     * before the page comes back; this one only <em>posts the request</em>. The
     * browser gets its answer immediately, and a separate bean
     * ({@code TransferMDB}) does the work whenever it gets round to it.</p>
     *
     * <p>What you should see: the balances on this page look unchanged. Reload
     * the page and they have moved, and there are two new lines in the audit log
     * — the queue did it while you were reading.</p>
     */
    public void queueIt() {
        if (transfer.getAmount() == null) {
            message = "Enter an amount before queueing.";
            refused = true;
            return;
        }

        try {
            queue.send(transfer.getFromAccount(), transfer.getToAccount(), transfer.getAmount());
            message = "Queued. The message-driven bean will pick it up in a moment"
                    + " — reload this page to see the result in the audit log.";
            refused = false;
            transfer = new TransferRequest();
        } catch (RuntimeException e) {
            // The broker being unreachable is an infrastructure problem, so it is
            // reported as a failure rather than hidden.
            message = "Could not queue the transfer: " + e.getMessage();
            refused = true;
        }

        forget();
    }

    /** Drop the cached lists so the next read goes back to the bank. */
    private void forget() {
        accounts = null;
        auditLines = null;
    }
}
