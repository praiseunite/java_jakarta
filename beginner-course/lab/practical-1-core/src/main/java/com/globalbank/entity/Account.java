package com.globalbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * One bank account, stored in a database table.
 *
 * <p>This is a JPA <strong>entity</strong>: a plain Java class with {@code @Entity}
 * and {@code @Id} that JPA maps onto a row. You never write INSERT or UPDATE
 * yourself — you change the object, and JPA writes the SQL when the transaction
 * commits.</p>
 *
 * <p>Three things to notice, because each one is a lesson:</p>
 * <ol>
 *   <li>{@code @Id} marks the field that identifies the row. Every entity needs
 *       exactly one, and the application will not start without it.</li>
 *   <li>{@code @Version} is the optimistic-lock counter. JPA adds it to the
 *       UPDATE's WHERE clause, so if somebody else changed this row since you
 *       read it, your write fails instead of silently overwriting them.</li>
 *   <li>The balance is a {@link BigDecimal}, not a {@code double}. A double
 *       cannot represent 0.10 exactly — it stores the nearest binary fraction,
 *       and those tiny errors add up across millions of transactions. Money is
 *       always BigDecimal.</li>
 * </ol>
 */
@Entity
@Table(name = "accounts")
public class Account implements Serializable {

    /** The account number. We choose it, so there is no @GeneratedValue here. */
    @Id
    private int id;

    @Column(name = "holder", nullable = false, length = 60)
    private String holder;

    /** precision/scale = "up to 12 digits, 2 of them after the point" — DECIMAL(12,2). */
    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Version
    private int version;

    /**
     * JPA requires a no-argument constructor. Ours is {@code protected} because
     * only JPA should use it — everybody else must supply an id and a holder.
     */
    protected Account() {
    }

    public Account(int id, String holder, BigDecimal openingBalance) {
        this.id = id;
        this.holder = holder;
        this.balance = openingBalance;
    }

    public int getId() {
        return id;
    }

    public String getHolder() {
        return holder;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * There is a setter for the balance but not for the id or the holder.
     * That is deliberate: an account number never changes, and a balance does.
     * The class's methods describe what is allowed to happen to it.
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getVersion() {
        return version;
    }

    /** Used by the servlet to print one readable line. */
    @Override
    public String toString() {
        return "Account " + id
                + " | " + holder
                + " | balance " + balance.toPlainString()
                + " | @Version " + version;
    }
}
