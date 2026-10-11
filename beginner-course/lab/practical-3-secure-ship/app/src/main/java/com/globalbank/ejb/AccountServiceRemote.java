package com.globalbank.ejb;

import com.globalbank.entity.Account;
import jakarta.ejb.Remote;

import java.math.BigDecimal;
import java.util.List;

/**
 * The bank's front door for programs that are <strong>not</strong> running inside
 * WildFly.
 *
 * <p>Every method here is also available to code inside the server, because
 * {@link AccountService} implements this interface. The annotation is what makes
 * the difference: {@code @Remote} publishes these methods on the network, so a
 * plain Java program on your laptop can call them as if the bank were a local
 * object.</p>
 *
 * <h2>What {@code @Remote} actually costs you</h2>
 * <p>Nothing here is free. When a call crosses the network:</p>
 * <ul>
 *   <li><strong>Every argument is copied</strong>, not passed. Change an
 *       {@code Account} on the client and the server's copy is untouched. This
 *       is why {@link Account} must be {@code Serializable} — the object is
 *       turned into bytes, posted, and rebuilt on the far side.</li>
 *   <li><strong>Every call is slow.</strong> Nanoseconds become milliseconds.
 *       A bean that needs ten calls to do one job is a bad remote bean.</li>
 *   <li><strong>Exceptions travel too.</strong> {@link InsufficientFundsException}
 *       is thrown on the server and arrives in the client — but as a
 *       <em>copy</em>, so the stack trace points at the server's code, not yours.</li>
 * </ul>
 *
 * <p>Session 6's rule of thumb is worth repeating: chatty beans must stay local.
 * Use {@code @Remote} only when the caller genuinely lives in another program —
 * which is exactly the situation this interface exists for.</p>
 *
 * <p>The full JNDI name a client looks this up under is:</p>
 * <pre>ejb:/globalbank-web/AccountService!com.globalbank.ejb.AccountServiceRemote</pre>
 * <p>Read it as: <em>enterprise bean, in the module called globalbank-web, the
 * bean class AccountService, seen through this interface</em>.</p>
 */
@Remote
public interface AccountServiceRemote {

    /** Every account, in id order. The result is copied down the wire. */
    List<Account> findAll();

    /** One account by its number, or {@link IllegalArgumentException} if there is none. */
    Account find(int id);

    /** Open a new account and return it. */
    Account open(String holder, BigDecimal openingBalance);

    /**
     * Move money between two accounts, all-or-nothing.
     *
     * @return a one-line receipt
     * @throws InsufficientFundsException if the source account cannot cover it
     */
    String transfer(int fromId, int toId, BigDecimal amount) throws InsufficientFundsException;
}
