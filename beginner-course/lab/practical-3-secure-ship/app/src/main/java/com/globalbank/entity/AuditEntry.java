package com.globalbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * One line in the bank's audit log: what somebody tried to do, and what happened.
 *
 * <p>This entity exists to teach one thing, so read it with that in mind. The
 * audit log is <strong>written in its own transaction</strong>, separate from the
 * transfer it describes. That means a transfer can be refused and rolled back
 * while its audit line survives — which is exactly what a real bank needs.
 * "Somebody tried to move £9,000 and it was refused" is the single most important
 * thing to have on record, and you cannot record it in a transaction that is
 * about to be thrown away.</p>
 *
 * <p>Compare this with {@link Account}: the id here is
 * {@code @GeneratedValue}, because the database should invent audit ids rather
 * than us. And there is deliberately <strong>no {@code @Version}</strong> — an
 * audit line is written once and never changed, so there is nothing to protect
 * against concurrent edits.</p>
 */
@Entity
@Table(name = "audit_log")
public class AuditEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** What was attempted, in the bank's own words. */
    @Column(name = "action", nullable = false, length = 40)
    private String action;

    /** The outcome — an audit line is useless without one. */
    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "detail", nullable = false, length = 255)
    private String detail;

    /**
     * When it happened. {@code java.time.LocalDateTime} is supported directly by
     * JPA 2.2 and later, so no {@code @Temporal} annotation is needed.
     */
    @Column(name = "happened_at", nullable = false)
    private LocalDateTime happenedAt;

    /** JPA requires a no-argument constructor. */
    protected AuditEntry() {
    }

    public AuditEntry(String action, String outcome, String detail) {
        this.action = action;
        this.outcome = outcome;
        this.detail = detail;
        this.happenedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getDetail() {
        return detail;
    }

    public LocalDateTime getHappenedAt() {
        return happenedAt;
    }

    /**
     * The timestamp as text, for a table column.
     *
     * <p>A raw {@link LocalDateTime} would print as
     * {@code 2026-10-09T06:20:33.123456} — correct, and unreadable. Formatting is
     * a presentation decision, but putting the formatter on the entity keeps the
     * Facelet page free of it, which matters because a page has no good way to
     * format anything.</p>
     */
    public String getWhenText() {
        return happenedAt == null ? ""
                : happenedAt.format(DateTimeFormatter.ofPattern("dd MMM HH:mm:ss"));
    }

    /** A compact one-line form for the web page. */
    @Override
    public String toString() {
        return outcome + " | " + action + " | " + detail;
    }
}
