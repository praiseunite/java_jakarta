package com.globalbank.dao;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The same table, read with hand-written SQL, so you can see what JPA was doing
 * for you all along.
 *
 * <p>Session 8 made two claims that are easy to nod along to and hard to
 * believe. This class is where you check them:</p>
 * <ol>
 *   <li>JPA is not magic. Underneath, something sends SQL over JDBC to a
 *       connection pool. Here is that something.</li>
 *   <li>A {@code DataSource} is just a pool of ready-made connections that
 *       somebody else manages, reached through JNDI.</li>
 * </ol>
 *
 * <p>Deploy this project and open
 * {@code /globalbank-web/account?action=raw}. It prints the same rows as
 * {@code action=all} — one read through JPA, one through raw JDBC. Put the two
 * next to each other and the difference is the entire argument for JPA.</p>
 *
 * <h2>Why the DataSource is the important half</h2>
 * <p>Compare the two ways of getting a connection:</p>
 * <pre>
 *   // What not to do — every request builds a new connection
 *   Connection c = DriverManager.getConnection(url, user, password);
 * </pre>
 * <p>Opening a database connection means a network handshake, authentication and
 * the database allocating memory. It costs milliseconds — more than the query it
 * is about to run. Doing that per request is the classic way to make a working
 * application fall over under load.</p>
 *
 * <p>A {@code DataSource} is a pool: the connections already exist, and
 * {@code getConnection()} hands you one that is sitting idle. When you close it,
 * you are not closing the connection — you are <strong>giving it back</strong>.
 * That is why try-with-resources is not optional here.</p>
 *
 * <p>The JNDI name below is WildFly's own {@code ExampleDS}, the in-memory H2
 * database the container ships with. Same database JPA is using: change the name
 * and you have pointed both halves at a real server.</p>
 */
@Stateless
public class RawAccountDao {

    /**
     * The pool.
     *
     * <p>{@code @Resource} is the annotation for "an application-server resource",
     * as opposed to {@code @PersistenceContext} for a JPA object or {@code @EJB}
     * for another bean. The {@code lookup} is the JNDI name — and the reason this
     * class exists is that this string is the <em>only</em> thing JPA's
     * {@code persistence.xml} was hiding from you.</p>
     */
    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    private DataSource dataSource;

    /**
     * Every account, read with SQL written by hand.
     *
     * <p>Four things to notice, all of which JPA was doing invisibly:</p>
     * <ol>
     *   <li>Try-with-resources opens three objects and closes all three, in
     *       reverse order, even if the query throws. Leaking a pooled connection
     *       is a slow-motion outage: the pool empties and the application stops.</li>
     *   <li>{@code ?} with {@code setInt} is what keeps user input <em>data</em>.
     *       Concatenating it into the SQL string is how injection happens, and
     *       Session 8's warning applies to every query you ever write.</li>
     *   <li>The column names are real table columns. There is no
     *       {@code Account} object here — that is exactly what JPA adds.</li>
     *   <li>{@code getBigDecimal}, not {@code getDouble}, for money. Same reason
     *       {@link com.globalbank.entity.Account} stores a {@code BigDecimal}.</li>
     * </ol>
     *
     * @throws SQLException if the database cannot be reached
     */
    public List<String> allAccountsByHand() throws SQLException {

        String sql = "SELECT id, holder, balance, version FROM accounts ORDER BY id";
        List<String> lines = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lines.add("Account " + rs.getInt("id")
                        + " | " + rs.getString("holder")
                        + " | balance " + rs.getBigDecimal("balance").toPlainString()
                        + " | @Version " + rs.getInt("version"));
            }
        }
        return lines;
    }

    /**
     * A note for the page, so the comparison is spelled out where the student
     * reads it rather than only in a comment they will not open.
     */
    public String whatThisProves() {
        return "Read with JDBC by hand: " + allAccountsByHandUnchecked().size()
                + " row(s). JPA's findAll() returns the same rows, mapped onto "
                + "Account objects, with no SQL anywhere in the application.";
    }

    /** {@link #allAccountsByHand()} without the checked exception, for display. */
    private List<String> allAccountsByHandUnchecked() {
        try {
            return allAccountsByHand();
        } catch (SQLException e) {
            return List.of("(could not read the table: " + e.getMessage() + ")");
        }
    }
}
