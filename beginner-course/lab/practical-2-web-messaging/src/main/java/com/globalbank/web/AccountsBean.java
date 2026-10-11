package com.globalbank.web;

import com.globalbank.ejb.AccountService;
import com.globalbank.ejb.AuditService;
import com.globalbank.ejb.InsufficientFundsException;
import com.globalbank.entity.Account;
import com.globalbank.entity.AuditEntry;
import com.globalbank.messaging.TransferQueueSender;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

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

    /** The newest audit lines, for the table at the bottom of the page. */
    public List<AuditEntry> getAuditLines() {
        if (auditLines == null) {
            auditLines = audit.recent();
        }
        return auditLines;
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
