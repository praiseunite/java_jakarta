package com.globalbank.client;

import com.globalbank.ejb.AccountServiceRemote;
import com.globalbank.entity.Account;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

/**
 * A program that talks to the bank from OUTSIDE WildFly.
 *
 * <p>This class is not part of the war and is never deployed. It is an ordinary
 * Java program with a {@code main} method, sitting on your laptop, calling
 * methods on a bean running inside the application server. Nothing about it
 * looks like a web request, because it is not one.</p>
 *
 * <p>That is what {@code @Remote} on {@link AccountServiceRemote} buys you: a
 * second front door for programs instead of people.</p>
 *
 * <h2>Why the imports say javax</h2>
 * <p>{@code javax.naming} and {@code javax.sql} were never renamed to
 * {@code jakarta}. The big rename in Jakarta EE 9 covered the Enterprise
 * Edition specifications; JNDI and JDBC belong to Java SE and kept their names.
 * So seeing {@code javax} here is correct, not a leftover.</p>
 *
 * <h2>How to run it</h2>
 * <p>The full instructions are in the project's README, and the short version is
 * that you need two things on the classpath: this project's compiled classes
 * (for the interface and the {@link Account} class) and WildFly's own
 * {@code bin/client/jboss-client.jar} (for the remote protocol). Then:</p>
 * <pre>
 *   java -cp "remote-client/classes;target/classes;%WILDFLY_HOME%\bin\client\jboss-client.jar" \
 *        com.globalbank.client.RemoteClient
 * </pre>
 *
 * <p>Note what is <strong>not</strong> on that classpath: the war. The client
 * needs the interface, not the implementation. Sending the bean class itself
 * would be pointless — the whole idea is that the code runs over there.</p>
 */
public class RemoteClient {

    /**
     * Where the bean lives, in JNDI's remote form:
     * {@code ejb:/<module>/<BeanClass>!<interface>}.
     *
     * <p>Read it piece by piece:</p>
     * <ul>
     *   <li>{@code ejb:/} — this is an enterprise bean, not a datasource or a
     *       queue.</li>
     *   <li>{@code globalbank-web} — the module, which for a war means its
     *       name without the {@code .war}.</li>
     *   <li>{@code AccountService} — the bean class.</li>
     *   <li>{@code !com.globalbank.ejb.AccountServiceRemote} — which view of it
     *       we want. A bean with several interfaces needs this; get it wrong and
     *       the lookup fails with a {@link NamingException} even though the bean
     *       is there.</li>
     * </ul>
     */
    private static final String JNDI_NAME =
            "ejb:/globalbank-web/AccountService!com.globalbank.ejb.AccountServiceRemote";

    public static void main(String[] args) {

        // The two lines that make a plain JNDI context able to reach WildFly.
        // The factory class ships in jboss-client.jar; the URL is WildFly's
        // remote naming port, not the web port — note remote+http, and note 8080
        // is where the HTTP upgrade starts.
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.wildfly.naming.client.WildFlyInitialContextFactory");
        env.put(Context.PROVIDER_URL, "remote+http://localhost:8080");

        Context ctx = null;
        try {
            ctx = new InitialContext(env);

            // This single line is the whole trick. To the code below, "bank" is
            // an ordinary object. It is not: it is a stub, and every method call
            // on it is serialised, posted to WildFly, run there, and the answer
            // posted back.
            AccountServiceRemote bank = (AccountServiceRemote) ctx.lookup(JNDI_NAME);

            System.out.println("Connected to the bank running inside WildFly.");
            System.out.println();

            showBalances(bank);

            System.out.println();
            System.out.println("Asking the server to move 25.00 from 1001 to 1002...");
            System.out.println("  " + bank.transfer(1001, 1002, new BigDecimal("25.00")));

            System.out.println();
            showBalances(bank);

            System.out.println();
            System.out.println("Now asking for something the bank will refuse:");
            System.out.println("  " + bank.transfer(1003, 1001, new BigDecimal("99999.00")));

        } catch (NamingException e) {
            // The bean was not found. This is nearly always one of three things,
            // so they are listed rather than left to guesswork.
            System.err.println("Could not find the bean: " + e.getMessage());
            System.err.println();
            System.err.println("  Check, in this order:");
            System.err.println("   1. Is WildFly running?");
            System.err.println("   2. Is the war deployed? Look for");
            System.err.println("      standalone/deployments/globalbank-web.war.deployed");
            System.err.println("   3. Is the name exact? It must be");
            System.err.println("      " + JNDI_NAME);

        } catch (Exception e) {
            // The call got through and the bank said no. A refusal is a normal
            // answer, not a crash — and note that it arrives here as a real
            // exception, thrown thousands of times further away.
            System.err.println("The call reached the server, and the bank refused:");
            System.err.println("  " + e.getClass().getName());
            System.err.println("  " + e.getMessage());
            System.err.println();
            System.err.println("  (That exception was constructed inside WildFly and");
            System.err.println("   sent to you as a copy. Nothing was changed: the");
            System.err.println("   balances above are exactly as they were.)");

        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignored) {
                    // Closing a context we are about to discard cannot matter.
                }
            }
        }
    }

    /**
     * Fetches every account and prints it.
     *
     * <p>Worth noticing: {@link Account} objects crossed the network to get here.
     * They did not exist in this JVM a moment ago. That is serialisation — the
     * object was turned into bytes on the server and rebuilt here — and it is why
     * {@code Account} implements {@code Serializable}.</p>
     */
    private static void showBalances(AccountServiceRemote bank) {
        List<Account> accounts = bank.findAll();
        System.out.println(accounts.size() + " account(s), copied down the wire:");
        for (Account a : accounts) {
            System.out.println("  " + a);
        }
    }
}
