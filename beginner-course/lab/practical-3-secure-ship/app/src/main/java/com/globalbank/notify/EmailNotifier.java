package com.globalbank.notify;

import com.globalbank.qualifiers.BankName;
import com.globalbank.qualifiers.Email;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Logger;

/**
 * The email one. Marked {@code @Email}, so it is only ever injected where
 * somebody wrote {@code @Inject @Email Notifier}.
 *
 * <p>{@code @ApplicationScoped} means one instance for the whole running
 * application, created the first time somebody needs it. That is right for a
 * stateless sender: there is nothing per-customer to keep, and making one
 * instance instead of thousands is free.</p>
 *
 * <h2>Why this writes to the log instead of sending email</h2>
 * <p>Real email needs a mail server, a hostname, credentials and a firewall
 * rule — four things to configure before a single transfer can be tested. The
 * point being taught here is <em>how CDI picks between two implementations</em>,
 * and a log line demonstrates that just as well while keeping the practical
 * runnable straight after a fresh install. The class is honest about it: it
 * says in its own log line that it is a stand-in.</p>
 *
 * <p>Swapping in a real implementation later is a one-file change precisely
 * because the caller only knows the {@link Notifier} interface. That is the
 * payoff for the indirection.</p>
 */
@Email
@ApplicationScoped
public class EmailNotifier implements Notifier {

    private static final Logger LOG = Logger.getLogger(EmailNotifier.class.getName());

    /**
     * The bank's name, straight from the {@code @Produces} field in
     * {@link com.globalbank.config.BankConfiguration}.
     *
     * <p>Notice there is no {@code new} here and nowhere to write one. CDI sees
     * the type ({@code String}) plus the qualifier ({@code @BankName}) and looks
     * for a producer that matches both. This is the difference between a value
     * that lives in a config file and a value that lives in the code: change the
     * producer, and every injection point changes with it.</p>
     */
    @Inject
    @BankName
    String bankName;

    @Override
    public void send(String message) {
        LOG.info(() -> "[email] " + bankName + ": " + message + " (simulated — no mail server is configured)");
    }
}
