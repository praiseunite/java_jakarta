package com.globalbank.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.logging.Logger;

/**
 * The bean behind {@code login.xhtml} — and <strong>the piece that makes
 * authentication happen at all</strong>.
 *
 * <p>Read {@link ApplicationConfig} first. It declares a custom form mechanism
 * and a database identity store, which together describe a bank vault: a door,
 * and a list of who is allowed through it. This class is the handle. Without it
 * the door does not open for anybody, however correct the vault is.</p>
 *
 * <p>That asymmetry is the single most common way to get this wrong, and it fails
 * in the most unhelpful way possible: no exception, no warning, no log line. The
 * form submits, the page redisplays, the user is still not logged in. Nothing is
 * broken — nothing was ever asked to happen.</p>
 *
 * <h2>The one line that does the work</h2>
 * <pre>{@code
 * securityContext.authenticate(request, response, params)
 * }</pre>
 * <p>Everything else in this class exists to get those three arguments. Read the
 * method below top to bottom and the shape is: collect what the user typed, wrap
 * it in a {@link Credential}, wrap that in an {@link AuthenticationParameters},
 * hand it to the container, act on the answer.</p>
 *
 * <h2>Why the request and response are needed</h2>
 * <p>It looks odd — a method that decides whether a password is correct, asking
 * for a servlet response object. The reason is that authenticating a user is not
 * only a lookup. The container may need to set a session cookie, and the
 * mechanism may need to write a redirect. Passing the real request and response
 * in means the container can do all of that; a method that only returned
 * true-or-false would leave the caller to do it by hand, badly.</p>
 *
 * <h2>{@code @RequestScoped}, not {@code @ViewScoped}</h2>
 * <p>The opposite of {@link com.globalbank.web.AccountsBean}, and for a reason
 * worth noticing. This bean holds a password in a field. A view-scoped bean lives
 * until the user navigates away, so that password would sit in memory for as long
 * as the page is open. A request-scoped bean is created for the submit and gone
 * immediately afterwards. <em>Lifetime is a security decision, not only a
 * convenience one.</em></p>
 */
@Named("login")
@RequestScoped
public class LoginBean {

    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());

    /**
     * The container's authentication front door.
     *
     * <p>{@code jakarta.security.enterprise.SecurityContext} — not to be confused
     * with WildFly's own {@code org.wildfly.security.auth.server.SecurityDomain}.
     * This one is the standard API: it can authenticate a caller, report who the
     * caller is, and answer whether they hold a role.</p>
     */
    @Inject
    private SecurityContext securityContext;

    /** What the user typed into the form. JSF fills these in. */
    private String username;
    private String password;

    /** Shown on the page after a failed attempt. */
    private String message;

    /**
     * Runs when "Sign in" is pressed.
     *
     * @return a JSF outcome — a view to navigate to, or {@code null} to stay on
     *         this page and redisplay the form with {@link #message} on it
     */
    public String login() {
        // A blank submission is answered here rather than by the identity store,
        // so the user gets a sentence instead of a generic failure.
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            message = "Enter your user name and password.";
            return null;
        }

        FacesContext faces = FacesContext.getCurrentInstance();
        ExternalContext external = faces.getExternalContext();

        // The real servlet request and response behind this JSF request. JSF is
        // itself a servlet (see web.xml), so they exist — but they are typed as
        // Object, which is why the cast is needed here.
        HttpServletRequest request = (HttpServletRequest) external.getRequest();
        HttpServletResponse response = (HttpServletResponse) external.getResponse();

        // The credential the identity store will check. Note the shape: the
        // password goes into a Password object rather than travelling as a plain
        // String, so that code handling credentials can call clear() on it
        // afterwards instead of leaving the characters in memory for the garbage
        // collector to find at some unknowable later time.
        Credential credential =
                new UsernamePasswordCredential(username, new Password(password));

        // ---- The line this whole class exists for ----
        //
        // Notice the third argument is NOT the credential. authenticate() takes
        // an AuthenticationParameters — a small wrapper whose job is to carry the
        // credential together with two switches: "is this a new authentication or
        // a re-authentication?" and "should this be remembered?".
        //
        // In this application the defaults are what we want, so the wrapper is
        // built and handed over without setting either. Writing it as
        // AuthenticationParameters.withParams().credential(credential) rather than
        // new AuthenticationParameters() and then a setter is simply the fluent
        // shape the API offers.
        //
        // This is the kind of detail that is easy to get wrong from memory, and
        // it is worth knowing how to settle rather than guess: the Jakarta EE API
        // jar is on your disk, and `javap -cp <jar> jakarta.security.enterprise.SecurityContext`
        // prints exactly this signature in a second. Reading the API is faster
        // than being wrong.
        AuthenticationParameters params =
                AuthenticationParameters.withParams().credential(credential);

        AuthenticationStatus status =
                securityContext.authenticate(request, response, params);

        // Clear the field as soon as it is no longer needed, whatever happened.
        // Cheap, and it shortens the window in which the password is sitting in a
        // heap dump.
        password = null;

        switch (status) {

            case SUCCESS -> {
                // The user exists and the password matches. The container has
                // already established the session by the time we get here, so
                // "navigating away" is the whole of what is left to do.
                LOG.info(() -> "Signed in: " + username);
                return "/accounts.xhtml?faces-redirect=true";
            }

            case SEND_CONTINUE -> {
                // The mechanism has taken over the response — it is mid-redirect
                // and the response is already committed. Returning anything would
                // try to navigate a response that is already gone.
                return null;
            }

            case SEND_FAILURE -> {
                // Wrong password, or no such user. Both get one message on
                // purpose. Distinguishing them ("no such user" versus "wrong
                // password") tells an attacker which user names are real, which
                // is half of what they need. Real systems accept the small
                // inconvenience to the honest user who mistyped their name.
                message = "That user name and password combination was not recognised.";
                return null;
            }

            case NOT_DONE -> {
                // The mechanism declined to make a decision. Reached here only if
                // the configuration does not match the request, so it is a
                // developer problem and is logged as one.
                LOG.warning("Authentication was not attempted - check that "
                        + "ApplicationConfig's mechanism definition is being picked up.");
                message = "Sign-in could not be completed. See the server log.";
                return null;
            }

            default -> {
                message = "Sign-in could not be completed.";
                return null;
            }
        }
    }

    /**
     * Signs the current user out and returns to the login page.
     *
     * <p>Two things have to happen and only one of them is obvious. The session
     * must be invalidated — that is the obvious one, and it throws away
     * everything the session was holding. The second is that the page must
     * actually go somewhere: invalidating a session does not navigate, and a user
     * left staring at the page they were on will assume the button did nothing.</p>
     *
     * <p>{@code faces-redirect=true} forces a real HTTP redirect rather than an
     * internal forward, so the browser's address bar ends up at the login page.
     * Forwarding would leave the protected URL showing, and a refresh would then
     * re-request it — which the container would answer by redirecting to login
     * anyway, but only after a confusing round trip.</p>
     */
    public String logout() {
        FacesContext faces = FacesContext.getCurrentInstance();
        String who = callerName(faces.getExternalContext());
        faces.getExternalContext().invalidateSession();
        LOG.info(() -> "Signed out: " + who);
        return "/login.xhtml?faces-redirect=true";
    }

    /** The signed-in user's name, or a placeholder. Used by the page header. */
    private static String callerName(ExternalContext external) {
        var principal = external.getUserPrincipal();
        return principal == null ? "nobody" : principal.getName();
    }

    // ---- What the page reads and writes ----

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMessage() {
        return message;
    }
}
