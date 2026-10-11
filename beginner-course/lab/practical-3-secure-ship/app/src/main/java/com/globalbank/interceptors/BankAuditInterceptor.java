package com.globalbank.interceptors;

import com.globalbank.qualifiers.BankName;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Logger;

/**
 * The code that actually runs around a {@code @Logged} method.
 *
 * <p>Read the two annotations at the top of the class together, because between
 * them they answer the two questions every interceptor raises:</p>
 * <ul>
 *   <li>{@code @Interceptor @Logged} — <em>what does this wrap?</em> It wraps
 *       anything carrying {@code @Logged}. Both annotations are required: with
 *       only {@code @Logged} it is a plain class, and with only
 *       {@code @Interceptor} it wraps nothing in particular.</li>
 *   <li>{@code @Priority(APPLICATION)} — <em>is it switched on?</em> Yes, for the
 *       whole application, without listing it in {@code beans.xml}.</li>
 * </ul>
 *
 * <h2>Why {@code @Priority} is worth knowing</h2>
 * <p>Older material — and a great deal of it is still online — insists that an
 * interceptor must be listed inside {@code <interceptors>} in {@code beans.xml}.
 * That was true in CDI 1.0. Since CDI 1.1 an interceptor annotated
 * {@code @Priority} is enabled globally with no XML at all, and this class uses
 * that route because it cannot be got wrong: there is no file to forget.</p>
 *
 * <p>The XML route still exists and is worth recognising when you meet it in
 * somebody else's project. The equivalent of the annotation below is:</p>
 * <pre>{@code
 * <interceptors>
 *     <class>com.globalbank.interceptors.BankAuditInterceptor</class>
 * </interceptors>
 * }</pre>
 * <p>Use it when you want to switch an interceptor on and off per deployment
 * without recompiling. Use {@code @Priority} when you just want it on.</p>
 *
 * <h2>The number in {@code @Priority} is an ordering, not a switch</h2>
 * <p>{@code APPLICATION} is the value 2000. Lower numbers run further out — they
 * are entered first and exited last. Platform interceptors such as the one that
 * starts the database transaction sit below 2000, so they run outside this one,
 * which is exactly what you want: the transaction should already be open before
 * any business interceptor looks at the call, and should still be open while it
 * finishes.</p>
 */
@Interceptor
@Logged
@Priority(Interceptor.Priority.APPLICATION)
public class BankAuditInterceptor {

    private static final Logger LOG = Logger.getLogger(BankAuditInterceptor.class.getName());

    /** The bank's name, so the log line reads like it came from this application. */
    @Inject
    @BankName
    String bankName;

    /**
     * Runs <em>instead of</em> the real method, with the real method available
     * inside it as {@code ctx.proceed()}.
     *
     * <p>The shape to memorise is: do your before-work, call
     * {@code ctx.proceed()}, do your after-work, and <strong>return what
     * {@code proceed()} returned</strong>. Forget to return it and every
     * intercepted method returns {@code null} — which, for a method declared
     * {@code void}, still looks fine and hides the bug until the day somebody
     * adds a return value to it.</p>
     *
     * <p>If this method does not call {@code proceed()}, the real method never
     * runs at all. That is a legitimate technique — it is how a security check
     * refuses a call — but it is almost never what you want by accident, so the
     * call below is deliberately unmissable.</p>
     *
     * @param ctx everything known about the call: which method, on which object,
     *            with which arguments, plus the {@code proceed()} that runs it
     * @return whatever the real method returned, passed straight through
     * @throws Exception whatever the real method threw, passed straight through
     */
    @AroundInvoke
    public Object audit(InvocationContext ctx) throws Exception {
        String method = ctx.getMethod().getName();
        long startNanos = System.nanoTime();

        // No try/catch around the call: an interceptor must not quietly change
        // what a method does when it fails. Let the exception travel outward to
        // whoever called, exactly as it would have without this class.
        try {
            LOG.info(() -> bankName + ": -> " + method);
            return ctx.proceed();
        } finally {
            long millis = (System.nanoTime() - startNanos) / 1_000_000;
            LOG.info(() -> bankName + ": <- " + method + " (" + millis + " ms)");
        }
    }
}
