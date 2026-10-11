package com.globalbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One person who may use the bank — and the whole of what the bank knows about
 * them.
 *
 * <p>Read the columns and notice what is <strong>not</strong> here: there is no
 * {@code password}. There is a {@code passwordHash}, which is a different kind of
 * thing entirely.</p>
 *
 * <h2>A hash is not an encrypted password</h2>
 * <p>Encryption is reversible — keep the key and you can get the password back.
 * A hash is not: it is a one-way scramble, and the only way to check a password
 * is to scramble the attempt and compare the two scrambles. That difference
 * matters because it decides what a thief gets. Steal an encrypted password
 * database without its key and you have nothing; steal it <em>with</em> the key
 * and you have everybody's password. Steal a hashed one and you still have to
 * guess each password one at a time, and each guess is deliberately slow to
 * test.</p>
 *
 * <p>That deliberateness is why the value in this column looks the way it does —
 * four pieces separated by colons:</p>
 * <pre>PBKDF2WithHmacSHA256:27500:R2xvYmFsQmFua1NhbHQwMQ==:OveTdHskM+EoBddciBrb2Jia/Ithv0oYizEoyzX+dUs=</pre>
 * <ol>
 *   <li>the algorithm — {@code PBKDF2WithHmacSHA256}</li>
 *   <li>the number of iterations — {@code 27500}, meaning the scramble was
 *       applied 27,500 times, on purpose, so that guessing is slow</li>
 *   <li>the salt — a random value mixed in so that two people with the same
 *       password get different hashes</li>
 *   <li>the hash itself</li>
 * </ol>
 *
 * <p>The salt and the iteration count are stored <em>with</em> the hash because
 * checking a password needs them. They are not secrets. Writing them down is the
 * point.</p>
 *
 * <p>You will never type one of these values. {@code Pbkdf2PasswordHash} generates
 * them and {@code DatabaseIdentityStore} checks against them — see
 * {@link com.globalbank.ejb.UserInitializer} for the generating half, and
 * {@link com.globalbank.security.ApplicationConfig} for the checking half.</p>
 *
 * <h2>Why one table and not three</h2>
 * <p>A textbook schema has {@code users}, {@code roles} and {@code user_roles}.
 * This one has a single {@code group_name} column, because every account in this
 * demo holds exactly one role. If that stopped being true, the only thing that
 * would change is the query in {@code ApplicationConfig} — which is why the
 * queries are written out there rather than hidden inside this class.</p>
 */
@Entity
@Table(name = "bank_users")
public class BankUser {

    /**
     * The user name, and the primary key.
     *
     * <p>No {@code @GeneratedValue} here, unlike {@link AuditEntry}. The user
     * name <em>is</em> the identity — inventing a number for it would give the
     * table two ways of naming the same row, and one of them would eventually
     * disagree with the other.</p>
     */
    @Id
    @Column(name = "username", nullable = false, length = 80)
    private String username;

    /**
     * The hash described above. Named explicitly and length-limited by hand,
     * because a default column length of 255 would be the wrong shape for a value
     * whose size we can predict.
     */
    @Column(name = "password_hash", nullable = false, length = 160)
    private String passwordHash;

    /**
     * The role this person holds, as the identity store will report it.
     *
     * <p>The values are the exact strings used by {@code @RolesAllowed}:
     * {@code CUSTOMER} and {@code BANK_MANAGER}. Case matters — the container
     * compares them as text, and {@code Customer} is not {@code CUSTOMER}.</p>
     */
    @Column(name = "group_name", nullable = false, length = 40)
    private String groupName;

    /** JPA requires a no-argument constructor. */
    protected BankUser() {
    }

    public BankUser(String username, String passwordHash, String groupName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.groupName = groupName;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getGroupName() {
        return groupName;
    }

    /**
     * Deliberately prints the user name and role only.
     *
     * <p>An entity that is logged, shown in a page or dumped in an exception
     * message should not carry the password hash with it. The habit is worth
     * forming now: a {@code toString} that prints a credential is how secrets end
     * up in log files that nobody thought to protect.</p>
     */
    @Override
    public String toString() {
        return username + " (" + groupName + ")";
    }
}
