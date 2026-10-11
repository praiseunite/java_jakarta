package com.globalbank.notify;

import com.globalbank.qualifiers.BankName;
import com.globalbank.qualifiers.Sms;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.logging.Logger;

/**
 * The text-message one. Marked {@code @Sms}.
 *
 * <p>Line for line this is {@link EmailNotifier} with a different qualifier and
 * a different log prefix, and that is deliberate — it is the control in the
 * experiment. Two beans, identical in shape, told apart only by the label on
 * them. When you read the log output and see both <em>[email]</em> and
 * <em>[sms]</em> for one transfer, you are seeing CDI resolve two injection
 * points to two different beans that share one type.</p>
 *
 * <p>Both are injected at the same place, in
 * {@link com.globalbank.events.TransferNotifier}. That class is the one to read
 * next.</p>
 */
@Sms
@ApplicationScoped
public class SmsNotifier implements Notifier {

    private static final Logger LOG = Logger.getLogger(SmsNotifier.class.getName());

    /** The bank's name, from the same {@code @Produces} field as the email sender. */
    @Inject
    @BankName
    String bankName;

    @Override
    public void send(String message) {
        LOG.info(() -> "[sms]   " + bankName + ": " + message + " (simulated — no gateway is configured)");
    }
}
