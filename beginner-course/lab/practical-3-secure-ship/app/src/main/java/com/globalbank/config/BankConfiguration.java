package com.globalbank.config;

import com.globalbank.qualifiers.AuditDepth;
import com.globalbank.qualifiers.BankName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Two configured values, produced for the rest of the application to inject.
 *
 * <p>A <strong>producer</strong> is a method or field whose job is to
 * <em>create</em> something CDI cannot create by itself. CDI can build any class
 * with a suitable constructor; it cannot invent a {@code String} that happens to
 * hold the bank's name, or an {@code int} that holds a configured limit. A
 * producer is how you tell it where those values come from.</p>
 *
 * <p>This class shows both shapes, one of each:</p>
 * <table>
 *   <caption>producer shapes</caption>
 *   <tr><th></th><th>Shape</th><th>Can it run code or take parameters?</th></tr>
 *   <tr><td>{@link #bankName}</td><td>a field</td><td>No — the value must already be known</td></tr>
 *   <tr><td>{@link #auditDepth()}</td><td>a method</td><td>Yes — it can compute, and take injected parameters</td></tr>
 * </table>
 *
 * <h2>The scope on a producer is about the produced value, not the producer</h2>
 * <p>Neither of these carries a scope annotation, and that is deliberate rather
 * than an oversight. The default is {@code @Dependent}. Read
 * {@link BankName} for the full reason: a normal scope makes CDI build a proxy,
 * a proxy works by subclassing, and neither {@link String} nor {@code int} can
 * be subclassed. Adding {@code @ApplicationScoped} to either producer below
 * would <strong>stop the application deploying</strong>.</p>
 *
 * <p>This class itself is {@code @ApplicationScoped}, which is a different
 * question and perfectly fine: it is an ordinary class with a no-argument
 * constructor, so CDI can proxy it happily.</p>
 */
@ApplicationScoped
public class BankConfiguration {

    /**
     * The bank's name — a producer <strong>field</strong>.
     *
     * <p>Note what is missing: there is no {@code return} and no method body,
     * because the value is the field's initialiser. That is the whole producer.
     * The field must not be {@code final} — CDI needs to be able to read it
     * through the container rather than inlining it at compile time.</p>
     *
     * <p>Injected wherever it is needed with {@code @Inject @BankName String name}.</p>
     */
    @Produces
    @BankName
    String bankName = "GlobalBank";

    /**
     * How many audit lines the page shows — a producer <strong>method</strong>.
     *
     * <p>This one earns its keep as a method rather than a constant: it reads a
     * JVM system property, so the depth can be changed at start-up without
     * rebuilding. Add
     * {@code -Dglobalbank.auditDepth=5} to WildFly's start-up options and the
     * audit table shows five lines.</p>
     *
     * <p>The fallback matters more than the feature. If somebody sets the
     * property to something that is not a number, {@code parseInt} throws
     * {@link NumberFormatException} — and a producer that throws takes down
     * whatever asked for the value. So it is caught here and the default is used
     * instead. Configuration reading should never be able to break the
     * application.</p>
     *
     * @return the configured audit depth, or 25 if it is missing or nonsense
     */
    @Produces
    @AuditDepth
    int auditDepth() {
        String raw = System.getProperty("globalbank.auditDepth", "25");
        try {
            int value = Integer.parseInt(raw.trim());
            // A depth of zero or less would show nothing, which looks like a bug
            // rather than a configuration choice.
            return value > 0 ? value : 25;
        } catch (NumberFormatException e) {
            return 25;
        }
    }
}
