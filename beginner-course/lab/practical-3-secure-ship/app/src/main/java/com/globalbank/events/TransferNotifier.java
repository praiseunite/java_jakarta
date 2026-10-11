package com.globalbank.events;

import com.globalbank.notify.Notifier;
import com.globalbank.qualifiers.Email;
import com.globalbank.qualifiers.Sms;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import java.util.logging.Logger;

/**
 * Who actually tells the customer. This class is the reason the transfer code
 * does not have to.
 *
 * <p>Before this, somebody had to paste "now send the email and the text" onto
 * the end of {@code AccountService.transfer()}. Every new thing that ought to
 * happen after a transfer meant editing the transfer. That is how a money-moving
 * method slowly turns into a money-moving method that also sends email, writes
 * reports and updates a search index — and how a failure in the reporting code
 * starts breaking the banking.</p>
 *
 * <p>With an event, the transfer announces what happened and stops caring.
 * Adding a third notification channel is a new class in this package. The
 * transfer is not opened, not recompiled and not redeployed to make it
 * happen.</p>
 *
 * <h2>Reading the observer method's signature — it is the whole lesson</h2>
 * <pre>{@code
 * public void onTransfer(@Observes(during = AFTER_SUCCESS) TransferCompletedEvent event)
 * }</pre>
 * <ul>
 *   <li>The class carries no interface. An observer does not implement anything;
 *       it is found because of the annotation below.</li>
 *   <li>{@code @Observes} is what makes this a listener rather than an ordinary
 *       method nobody calls. Nothing in the project refers to
 *       {@code onTransfer} by name — that is the point, and it is also why a
 *       typo here fails silently.</li>
 *   <li>{@code TransferCompletedEvent} — the observer is woken by events of
 *       exactly this type. CDI matches on type, so this is the only link between
 *       the sender and this class.</li>
 *   <li>{@code during = AFTER_SUCCESS} — <strong>when</strong>. This is the part
 *       worth slowing down for.</li>
 * </ul>
 *
 * <h2>{@code during} — and why the default is a trap here</h2>
 * <p>If you leave {@code during} off, the observer fires
 * {@code IN_PROGRESS}: synchronously, immediately, <em>inside</em> the caller's
 * transaction. For a notification that is wrong in a specific and expensive way.
 * The customer would be told their money moved before the database has agreed,
 * and if the transaction rolled back a moment later — a constraint violation in
 * the last statement, a crash between the announcement and the commit — the
 * customer would have a text message about a transfer that never happened. There
 * is no unsend.</p>
 *
 * <p>{@code AFTER_SUCCESS} tells the container to hold this observer back until
 * the transaction has committed. Only then does the money have somewhere to have
 * moved from. The alternatives are {@code BEFORE_COMPLETION},
 * {@code AFTER_FAILURE} (put your "it did not go through" message here) and
 * {@code AFTER_COMPLETION}, which runs either way.</p>
 *
 * <p>One consequence to expect when you run it: the notification lines appear in
 * the server log <em>after</em> the transfer's own commit line, not before. That
 * ordering is not a quirk. It is the feature.</p>
 */
@ApplicationScoped
public class TransferNotifier {

    private static final Logger LOG = Logger.getLogger(TransferNotifier.class.getName());

    /**
     * Both senders, injected by qualifier. Same interface, same field shape,
     * different label — and therefore two different beans.
     *
     * <p>These are the injection points that make the ambiguity real. Delete
     * either {@code @Email} or {@code @Sms} and the application stops deploying
     * with an ambiguous dependency, because CDI can then see two candidates for
     * the same type and no way to choose.</p>
     */
    @Inject
    @Email
    Notifier email;

    @Inject
    @Sms
    Notifier sms;

    /**
     * Wakes up once per completed transfer, and only after the commit.
     *
     * @param event what happened — handed to us by the container, not by a caller
     */
    public void onTransfer(@Observes(during = TransactionPhase.AFTER_SUCCESS) TransferCompletedEvent event) {
        String message = event.amount().toPlainString()
                + " moved from account " + event.fromId() + " (" + event.fromHolder() + ")"
                + " to account " + event.toId() + " (" + event.toHolder() + ")";

        // Neither call can break the transfer: the money is already committed and
        // this transaction is over. A failure here is logged and forgotten.
        // That is the trade an observer makes, and it is why a notification must
        // never be the only record that something happened.
        email.send(message);
        sms.send(message);

        LOG.info(() -> "Notified the customer about: " + message);
    }
}
