package com.globalbank.messaging;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.Queue;

import java.math.BigDecimal;

/**
 * Posts a transfer to a queue and returns immediately — the sending half of
 * Session 7's messaging story.
 *
 * <p>The interesting part is how little there is. Session 7 warned that
 * {@code java:/jms/queue/OrderQueue} had to be created in advance by an
 * administrator, and that is normally true: {@code @Resource} can only inject a
 * queue that already exists. Writing a CLI script to create one is a poor first
 * experience, so this class uses the standard shortcut instead — see the
 * annotation below.</p>
 *
 * <h2>{@code @JMSDestinationDefinition}</h2>
 * <p>This declares the queue <strong>in code</strong> and asks the container to
 * create it at deployment time, before anything is injected. So the rule from
 * Session 7 still holds — the queue must exist before you look it up — but now
 * the application states the requirement itself instead of relying on somebody
 * having run a command first.</p>
 *
 * <p>Read the two names carefully, because they are not the same name:</p>
 * <ul>
 *   <li>{@code name = "java:jboss/exported/jms/queue/TransferQueue"} is the
 *       <em>JNDI</em> name. The {@code java:jboss/exported/} prefix means "make
 *       this visible to clients outside the server". WildFly publishes the same
 *       queue automatically under the shorter internal name
 *       {@code java:/jms/queue/TransferQueue}, which is what the
 *       {@code @Resource} below asks for — exactly the form Session 7
 *       taught.</li>
 *   <li>{@code destinationName = "TransferQueue"} is the <em>physical</em> name
 *       inside the broker. This is the name the message sits under in Artemis.</li>
 * </ul>
 *
 * <p>Use the JNDI name in Java, the destination name when you go looking in the
 * server. Mixing them up is the single most common messaging mistake.</p>
 */
@JMSDestinationDefinition(
        name = "java:/jms/queue/TransferQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "TransferQueue",
        description = "Transfers waiting to be carried out in the background")
@Stateless
public class TransferQueueSender {

    /**
     * The connection to the broker.
     *
     * <p>{@code @Inject} rather than {@code @Resource}, which is the modern
     * simplified API: a managed {@code JMSContext} that the container opens,
     * pools and closes for you. That is why no method in this class calls
     * {@code close()} or {@code start()} anywhere.</p>
     */
    @Inject
    @JMSConnectionFactory("java:/ConnectionFactory")
    private JMSContext context;

    /** The destination itself, injected as an administered object. */
    @Resource(lookup = "java:/jms/queue/TransferQueue")
    private Queue transferQueue;

    /**
     * Put a transfer on the queue.
     *
     * <p>The whole operation is one line, and that is the point of the simplified
     * API. Compare it with what Session 7's older code had to do — get a
     * connection, start it, create a session, create a producer, send, then close
     * everything in the right order.</p>
     *
     * <p>The amount is sent as text rather than an object. Sending a Java object
     * to a queue means relying on the same class existing on both sides with the
     * same version — a real cost. Plain text is portable, readable in the
     * broker's console, and easy to debug by eye.</p>
     */
    public void send(int fromId, int toId, BigDecimal amount) {
        String body = fromId + ":" + toId + ":" + amount.toPlainString();
        context.createProducer().send(transferQueue, body);
    }
}
