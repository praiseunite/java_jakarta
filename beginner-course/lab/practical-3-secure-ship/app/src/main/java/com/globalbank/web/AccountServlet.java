package com.globalbank.web;

import com.globalbank.dao.RawAccountDao;
import com.globalbank.ejb.AccountService;
import com.globalbank.entity.Account;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

/**
 * A tiny web front door, so you can drive the bank from a browser address bar.
 *
 * <p>It contains no business logic at all. Every request is translated into one
 * call on {@link AccountService}, and the answer is printed as plain text. That
 * separation is the whole point of the course: the servlet is the doorway, the
 * bean is the bank.</p>
 *
 * <p>Try these once it is deployed (the war is named {@code globalbank-web}):</p>
 * <pre>
 *   /globalbank-web/account?action=all
 *   /globalbank-web/account?action=find&amp;id=1001
 *   /globalbank-web/account?action=open&amp;holder=New%20Person&amp;balance=500.00
 *   /globalbank-web/account?action=deposit&amp;id=1001&amp;amount=250.00
 *   /globalbank-web/account?action=withdraw&amp;id=1002&amp;amount=1000.00
 *   /globalbank-web/account?action=transfer&amp;id=1001&amp;to=1002&amp;amount=100.00
 *   /globalbank-web/account?action=raw
 * </pre>
 *
 * <p>The first six go through JPA. The last one reads the same table with
 * hand-written SQL — see {@link RawAccountDao} for why that is worth doing
 * once.</p>
 *
 * <p><strong>Practical 3 changed one thing about all of those URLs: you must be
 * signed in to use any of them.</strong> web.xml now protects {@code /account},
 * so an unauthenticated request is redirected to the login page before this class
 * is reached. And {@code action=raw} has a second rule on top — the manager role —
 * because the bean behind it says so.</p>
 */
@WebServlet("/account")
public class AccountServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * The container injects a ready-to-use service bean. Note there is no
     * {@code new AccountService()} anywhere in this project — the container is
     * responsible for creating it, pooling it and wrapping each call in a
     * transaction.
     */
    @EJB
    private AccountService service;

    /**
     * The same data, read a second way. Practical 2 adds this purely so the two
     * approaches can be put side by side — see {@link RawAccountDao}.
     */
    @EJB
    private RawAccountDao raw;

    /**
     * Who is asking — the same {@code SecurityContext} the Facelet page uses.
     *
     * <p>{@code @Inject} in a servlet, rather than {@code @EJB}, because this is
     * not an enterprise bean. Since Servlet 3.0 a servlet is a CDI bean and
     * injection works here exactly as it does anywhere else.</p>
     */
    @Inject
    private SecurityContext security;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // Plain text, because the point of this practical is the bean, not HTML.
        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        // ?action=…  — what the caller wants done. No action at all means "show
        // me everything", which is the friendliest default for a demo.
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "all";
        }

        try {
            switch (action) {

                case "all" -> {
                    var accounts = service.findAll();
                    out.println(accounts.size() + " account(s) in the ledger:");
                    for (Account a : accounts) {
                        out.println("  " + a);
                    }
                }

                case "find" -> out.println(service.find(intParam(req, "id")));

                case "open" -> {
                    Account created = service.open(
                            req.getParameter("holder"),
                            money(req, "balance"));
                    out.println("Opened: " + created);
                }

                case "deposit" -> {
                    Account updated = service.deposit(
                            intParam(req, "id"),
                            money(req, "amount"));
                    out.println("Deposited. Now: " + updated);
                }

                case "withdraw" -> {
                    Account updated = service.withdraw(
                            intParam(req, "id"),
                            money(req, "amount"));
                    out.println("Withdrawn. Now: " + updated);
                }

                case "transfer" -> {
                    // Printed only after the call returns. If the bean refused,
                    // you see FAILED below and the balances are exactly as they
                    // were — that is the transaction doing its job.
                    out.println(service.transfer(
                            intParam(req, "id"),
                            intParam(req, "to"),
                            money(req, "amount")));
                    out.println();
                    out.println("Now: " + service.find(intParam(req, "id")));
                    out.println("Now: " + service.find(intParam(req, "to")));
                }

                case "raw" -> {
                    // NEW IN PRACTICAL 3. Everything above this line is protected
                    // by web.xml, which refuses the whole URL to anyone not signed
                    // in. That is coarse: it can say "a signed-in user", not "a
                    // manager". Fine-grained rules live on the beans, and
                    // RawAccountDao carries @RolesAllowed("BANK_MANAGER") — so this
                    // call WOULD be refused by the container even without the check
                    // below.
                    //
                    // So why check here as well? Because the container's refusal
                    // arrives as an exception, and an exception tells the person
                    // reading the screen nothing useful. Asking first lets us
                    // answer with a sentence and the right status code. Doing both
                    // is the right order: enforce in the bean, explain at the door.
                    if (!security.isCallerInRole("BANK_MANAGER")) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.println("REFUSED: the raw SQL view is for BANK_MANAGER only.");
                        out.println("You are signed in as " + callerName()
                                + ". The same rows are on accounts.xhtml.");
                        return;
                    }

                    // Identical output to action=all, produced without JPA. Read
                    // the two side by side: everything JPA saves you is in here.
                    out.println(raw.whatThisProves());
                    out.println();
                    for (String line : raw.allAccountsByHand()) {
                        out.println("  " + line);
                    }
                }

                default -> throw new IllegalArgumentException(
                        "Unknown action '" + action
                                + "'. Use all, find, open, deposit, withdraw, transfer or raw.");
            }

        } catch (Exception e) {
            // Every refusal the bank makes arrives here: not enough money, no such
            // account, a bad amount. Printing the exception's simple name and its
            // message is enough to show what the bean decided, and 400 tells the
            // browser (and you) that this was a bad request rather than a crash.
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("FAILED: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Who is signed in, or a placeholder.
     *
     * <p>Read through the standard {@code SecurityContext} rather than through the
     * servlet request, so that this servlet and the Facelet pages ask the same
     * question the same way. Two ways of asking "who is this?" is how one of them
     * ends up subtly different from the other.</p>
     */
    private String callerName() {
        var principal = security.getCallerPrincipal();
        // getCallerPrincipal() never returns null under Jakarta Security: an
        // unauthenticated caller gets an anonymous principal whose name is the
        // literal string "anonymous". Worth knowing, because a check like
        // `if (principal != null)` looks reasonable and is always true.
        return principal == null ? "nobody" : principal.getName();
    }

    /**
     * Reads a whole-number parameter.
     *
     * <p>Written out rather than inlined so that a missing or mistyped value gives
     * a sentence you can act on instead of a stack trace from deep inside the
     * servlet library.</p>
     */
    private static int intParam(HttpServletRequest req, String name) {        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("The '" + name + "' parameter is required");
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The '" + name + "' parameter must be a whole number, but was '" + raw + "'");
        }
    }

    /**
     * Reads a money parameter.
     *
     * <p>{@code new BigDecimal(String)} — note the String. There is a
     * {@code BigDecimal(double)} constructor too, and it is a trap: it faithfully
     * stores whatever the double got wrong. Always parse money from text.</p>
     */
    private static BigDecimal money(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("The '" + name + "' parameter is required");
        }
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "The '" + name + "' parameter must be an amount like 250.00, but was '" + raw + "'");
        }
    }
}
