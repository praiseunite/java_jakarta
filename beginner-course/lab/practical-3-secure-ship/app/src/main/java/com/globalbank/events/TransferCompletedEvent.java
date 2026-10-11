package com.globalbank.events;

import java.math.BigDecimal;

/**
 * "A transfer just happened" — the announcement, not the work.
 *
 * <p>This is a plain data carrier with no annotations and no behaviour. A class
 * like this is called an <strong>event payload</strong> or a DTO, and its only
 * job is to be the thing that gets handed to whoever is listening.</p>
 *
 * <h2>Why a record</h2>
 * <p>An event is a fact about something that already happened. A fact should not
 * be editable — if a listener could change the amount on its way past, the
 * second listener would see a different transfer than the first. A
 * {@code record} gives immutability and a constructor and getters in one line,
 * which is why it is the natural choice here. (Java 16 and later; this project
 * compiles for Java 17.)</p>
 *
 * <p>The methods are named after the fields — {@code fromId()},
 * {@code amount()} and so on — not {@code getFromId()}. That is simply how
 * records work.</p>
 *
 * <h2>What is <em>not</em> in here</h2>
 * <p>No entity, no {@code EntityManager}, no service. An event that carried live
 * database objects would drag them outside the transaction that owns them, and
 * the listener would eventually touch something that had already been closed.
 * Carry the values you need, copied out. {@code amount} is a fresh
 * {@link BigDecimal} and the names are {@link String}s, so nothing here can
 * reach back into the database.</p>
 *
 * @param fromId     the account money left
 * @param toId       the account money arrived in
 * @param amount     how much moved, already checked to be positive
 * @param fromHolder the sender's name, for a message a human can read
 * @param toHolder   the recipient's name
 */
public record TransferCompletedEvent(long fromId, long toId, BigDecimal amount,
                                     String fromHolder, String toHolder) {
}
