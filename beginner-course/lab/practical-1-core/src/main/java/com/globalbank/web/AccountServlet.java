package com.globalbank.web;

import com.globalbank.ejb.AccountService;
import com.globalbank.entity.Account;
import jakarta.ejb.EJB;
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
 * <p>Try these once it is deployed (the war is named {@code globalbank-core}):</p>
 * <pre>
 *   /globalbank-core/account?action=all
 *   /globalbank-core/account?action=find&amp;id=1001
 *   /globalbank-core/account?action=open&amp;holder=New%20Person&amp;balance=500.00
 *   /globalbank-core/account?action=deposit&amp;id=1001&amp;amount=250.00
 *   /globalbank-core/account?action=withdraw&amp;id=1002&amp;amount=1000.00
 *   /globalbank-core/account?action=transfer&amp;id=1001&amp;to=1002&amp;amount=100.00
 * </pre>
 */
@WebServlet("/account")
public class AccountServlet extends HttpServlet {

    /**
     * The container injects a ready-to-use service bean. Note there is no
     * {@code new AccountService()} anywhere in this project — the container is
     * responsible for creating it, pooling it and wrapping each call in a
     * transaction.
     */
    @EJB
    private AccountService service;

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

                default -> throw new IllegalArgumentException(
                        "Unknown action '" + action
                                + "'. Use all, find, open, deposit, withdraw or transfer.");
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
     * Reads a whole-number parameter.
     *
     * <p>Written out rather than inlined so that a missing or mistyped value gives
     * a sentence you can act on instead of a stack trace from deep inside the
     * servlet library.</p>
     */
    private static int intParam(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
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
