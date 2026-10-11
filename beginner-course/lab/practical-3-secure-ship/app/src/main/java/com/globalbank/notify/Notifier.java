package com.globalbank.notify;

/**
 * "Tell the customer something happened."
 *
 * <p>One interface, two implementations, and no implementation named here. That
 * is the whole point of this package: the code that wants to notify somebody
 * asks for a {@link Notifier} and does not know — and cannot find out — whether
 * the message leaves as an email or a text.</p>
 *
 * <h2>Why an interface and not just the class</h2>
 * <p>Two beans implement this interface, {@link EmailNotifier} and
 * {@link SmsNotifier}. If either were injected by its own class name, the caller
 * would be welded to that one choice and swapping it would mean editing the
 * caller.</p>
 *
 * <p>Because both implement the same type, an injection point that asks for a
 * plain {@code @Inject Notifier} would be <strong>ambiguous</strong> — CDI would
 * have two candidates and no reason to prefer either, and the application would
 * refuse to deploy with an "ambiguous dependency" error. That is CDI being
 * helpful: it has caught the fact that you did not say which one you meant. The
 * qualifiers {@link com.globalbank.qualifiers.Email} and
 * {@link com.globalbank.qualifiers.Sms} are how you say.</p>
 */
public interface Notifier {

    /**
     * Sends one message. In this project "sends" means "writes a line to the
     * server log" — see {@link EmailNotifier} for why that is the honest thing
     * for a teaching application to do.
     *
     * @param message what to tell the customer
     */
    void send(String message);
}
