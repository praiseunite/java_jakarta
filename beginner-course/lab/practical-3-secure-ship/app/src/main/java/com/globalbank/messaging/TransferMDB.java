package com.globalbank.messaging;

import com.globalbank.ejb.AccountService;
import com.globalbank.ejb.AuditService;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

import java.math.BigDecimal;

/**
 * The receiving half: a bean that wakes up when a transfer lands on the queue.
 *
 * <p>Nobody ever calls this class. There is no {@code new TransferMDB()} anywhere
 * in the project, and no other class refers to it. The container watches the
 * queue, and when a message arrives it calls {@link #onMessage(Message)} — on a
 * thread from its own pool, at a moment of its choosing, possibly long after the
 * user closed the browser.</p>
 *
 * <p>That inversion is the whole idea. Compare with every other bean in this
 * project: {@code AccountService} waits to be asked. This one is told.</p>
 *
 * <h2>The three annotations</h2>
 * <ul>
 *   <li>{@code @MessageDriven} says what it is: not {@code @Stateless}, because
 *       it has no methods to call. It <em>is</em> a listener.</li>
 *   <li>{@code destinationLookup} is the queue to watch, in the
 *       {@code java:/jms/queue/...} form that both Session 7 and
 *       {@link TransferQueueSender} use.</li>
 *   <li>{@code destinationType} says queue (one worker, one message) rather than
 *       topic (everybody gets a copy).</li>
 * </ul>
 *
 * <h2>Transactions, and the trap that is waiting here</h2>
 * <p>An MDB method runs inside a container-managed transaction, and the message
 * acknowledgement is part of it. That gives you a guarantee worth pausing on:
 * <strong>if {@code onMessage} throws, the transaction rolls back and the message
 * comes back</strong>. So a crash never loses a transfer — the broker simply
 * hands it over again.</p>
 *
 * <p>The trap is the mirror image. Because that redelivery happens on <em>any</em>
 * exception, a refusal caused by a bug in your code would loop — and a
 * deterministic refusal (not enough money) would loop too, forever.</p>
 *
 * <p>So the calls below are deliberately chosen. {@code transferOrRefuse} reports
 * a business refusal as a <em>return value</em>, which is a success as far as the
 * transaction is concerned — the message is acknowledged and does not come back.
 * Anything else is left to propagate, because a genuine failure <em>should</em>
 * be retried. Getting that split right is the whole skill of writing an MDB.</p>
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(
                propertyName = "destinationLookup",
                propertyValue = "java:/jms/queue/TransferQueue"),
        @ActivationConfigProperty(
                propertyName = "destinationType",
                propertyValue = "jakarta.jms.Queue")
})
public class TransferMDB implements MessageListener {

    @EJB
    private AccountService service;

    /** The audit log. Its own transaction, so its lines survive a rollback. */
    @EJB
    private AuditService audit;

    /**
     * Called by the container, once per message.
     *
     * <p>Note the signature: {@code void onMessage(Message)} and no
     * {@code throws}. {@link MessageListener} does not allow a checked exception,
     * so every checked failure has to be caught here and turned into something
     * else. That is a meaningful constraint rather than a quirk — the container
     * has to decide what to do with your failure, and the only vocabulary it
     * understands is "returned" or "threw".</p>
     */
    @Override
    public void onMessage(Message message) {

        String body;
        try {
            // getBody(String.class) refuses politely if the message turns out to
            // be a MapMessage or a BytesMessage, which is exactly what we want.
            body = message.getBody(String.class);
        } catch (Exception e) {
            audit.record("QUEUED-TRANSFER", "REFUSED",
                    "Unreadable message (" + e.getClass().getSimpleName() + ")");
            return;
        }

        // The wire format, agreed with TransferQueueSender: "from:to:amount".
        // A real system would send JSON or a MapMessage; a three-part string
        // keeps the lesson on the MDB and off the parsing.
        String[] parts = body == null ? new String[0] : body.split(":");
        if (parts.length != 3) {
            audit.record("QUEUED-TRANSFER", "REFUSED",
                    "Expected from:to:amount but the body was '" + body + "'");
            return;
        }

        try {
            int from = Integer.parseInt(parts[0].trim());
            int to = Integer.parseInt(parts[1].trim());
            BigDecimal amount = new BigDecimal(parts[2].trim());

            // See transferOrRefuse: a refusal comes back as text starting with
            // "REFUSED", not as an exception, so the transaction still commits
            // and this message is not delivered again.
            String receipt = service.transferOrRefuse(from, to, amount);

            boolean refused = receipt.startsWith("REFUSED");
            audit.record("QUEUED-TRANSFER", refused ? "REFUSED" : "OK", receipt);

        } catch (NumberFormatException e) {
            // The body was the right shape but not made of numbers. That will
            // never become true, so it is recorded and dropped rather than
            // retried.
            audit.record("QUEUED-TRANSFER", "REFUSED",
                    "The body '" + body + "' is not from:to:amount");
        }
    }
}
