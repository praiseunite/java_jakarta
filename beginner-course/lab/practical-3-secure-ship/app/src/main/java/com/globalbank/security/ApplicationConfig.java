package com.globalbank.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/**
 * The whole of this application's security configuration, in one empty class.
 *
 * <p>There is no code in this file and there never will be. It exists to carry
 * two annotations, and the reason they live on a class rather than in an XML file
 * is that the container reads them at deploy time and wires up everything they
 * describe. Add a line here and behaviour changes at the next deployment — no
 * filter, no interceptor, no login servlet to write.</p>
 *
 * <p>Two annotations answer two separate questions, and keeping them apart is
 * most of what there is to understand:</p>
 *
 * <table>
 *   <caption>the two halves of authentication and authorization</caption>
 *   <tr><th></th><th>Question</th><th>Answered by</th></tr>
 *   <tr>
 *     <td>{@code @CustomFormAuthenticationMechanismDefinition}</td>
 *     <td>How does a person <em>prove who they are</em>?</td>
 *     <td>our own {@code login.xhtml} form</td>
 *   </tr>
 *   <tr>
 *     <td>{@code @DatabaseIdentityStoreDefinition}</td>
 *     <td>Given a name and password, <em>who are they and what may they do?</em></td>
 *     <td>the {@code bank_users} table</td>
 *   </tr>
 * </table>
 *
 * <p>A mechanism without an identity store can collect a name and password but
 * has nothing to check them against. An identity store without a mechanism is a
 * library nobody calls. Both are required, and they are deliberately independent
 * — swap the table for an LDAP server tomorrow and the mechanism above does not
 * change.</p>
 *
 * <h2>Why "custom" form authentication, and what it costs</h2>
 * <p>Jakarta Security ships {@code @FormAuthenticationMechanismDefinition}, the
 * standard form login, and it is less work than this: the container serves a
 * built-in {@code j_security_check} endpoint and you write no Java at all. What
 * you give up is control. The login page must post to a name the container
 * chose, you cannot add a "remember me" checkbox, cannot count failed attempts,
 * cannot log the attempt yourself.</p>
 *
 * <p>{@code @CustomFormAuthenticationMechanismDefinition} hands all of that back
 * to you — the login page is yours, the form is yours, the Java is yours.</p>
 *
 * <p><strong>And that is the catch, in bold, because it is the mistake this
 * configuration invites.</strong> With the standard form mechanism the container
 * performs the login for you. With the custom one, <em>nothing happens unless
 * your own code asks</em>. You must have a bean that calls</p>
 * <pre>{@code securityContext.authenticate(request, response, params)}</pre>
 * <p>and you must call it from the button on your login page. Build the login
 * page, wire the form to a bean that merely reads the two fields and says
 * "welcome", and you have a form that appears to work perfectly and never
 * authenticates anybody — no error, no warning, and every protected page still
 * redirecting back to the login. If your login silently does nothing, this
 * paragraph is why.</p>
 *
 * <p>The bean that does the asking is {@link LoginBean}. It is the other half of
 * this class and cannot be left out.</p>
 */
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                // Where an unauthenticated visitor is sent. It must be a page the
                // visitor is allowed to reach while logged out, or the redirect
                // loops: login page, needs login, login page, needs login. That is
                // why web.xml protects /accounts.xhtml and /account and
                // deliberately does NOT protect this file.
                loginPage = "/login.xhtml",

                // The container's own failure page. Nearly vestigial with a custom
                // form, because our LoginBean handles a bad password itself and
                // never hands control back — but @LoginToContinue declares it, so
                // it is supplied.
                errorPage = "/login-error.xhtml",

                // false = redirect to the login page (the address bar changes).
                // true = forward to it, leaving the protected URL in the address
                // bar. Redirecting is clearer while learning: the URL always tells
                // you which page you are actually on.
                useForwardToLogin = false
        )
)
@DatabaseIdentityStoreDefinition(

        // Which database. WildFly's own ExampleDS — the same in-memory H2 that
        // JPA is using, so there is nothing to install or configure before this
        // lab runs. A real deployment points this at its application datasource.
        dataSourceLookup = "java:jboss/datasources/ExampleDS",

        // Given a user name, find the stored hash. The "?" is a bind parameter,
        // not string concatenation — the same rule RawAccountDao follows, and
        // here it is load-bearing rather than merely good practice. This query is
        // reachable by anyone who can reach the login form, so building it by
        // pasting the submitted user name into the SQL text would be a textbook
        // injection hole, in the authentication path, in the one place where
        // getting it wrong is unrecoverable.
        callerQuery = "SELECT password_hash FROM bank_users WHERE username = ?",

        // Given a user name, find the roles. The values must match the strings in
        // @RolesAllowed exactly — "CUSTOMER", "BANK_MANAGER" — because the
        // container compares them as text and never guesses at case.
        groupsQuery = "SELECT group_name FROM bank_users WHERE username = ?",

        // How to check the password against the stored hash. This is what makes
        // the four-part value in the password_hash column meaningful: the store
        // reads the algorithm, iteration count and salt out of the stored string,
        // re-hashes the attempt with them, and compares.
        //
        // Omitting this line is a silent downgrade, not an error. The default is
        // PasswordHash, which means "the stored value is the password, in plain
        // text". Everything would still work — and every password would be
        // readable by anyone who saw the table.
        hashAlgorithm = Pbkdf2PasswordHash.class
)
@ApplicationScoped
public class ApplicationConfig {
    // Intentionally empty. Every line of this class's meaning is an annotation.
}
