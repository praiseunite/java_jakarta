package com.globalbank.interceptors;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "Log this call."
 *
 * <p>An <strong>interceptor binding</strong> is a label — like a qualifier — but
 * it means something different. A qualifier chooses <em>which bean</em>. An
 * interceptor binding chooses <em>which code wraps around the call</em>.
 * Writing {@code @Logged} on a class says "before and after every business
 * method on this class, run whatever is bound to {@code @Logged}".</p>
 *
 * <p>Note there is nothing here that does any work. This file is a name. The
 * behaviour lives in {@link BankAuditInterceptor}, and the two are joined by the
 * fact that the interceptor carries the same annotation. That split is what lets
 * you turn logging on for a whole class by adding one word to it, and turn it
 * off everywhere by editing the interceptor instead of forty classes.</p>
 *
 * <h2>Two annotations to notice, and one that is missing</h2>
 * <ul>
 *   <li>{@code @InterceptorBinding} — what makes CDI treat this as a binding
 *       rather than an ordinary annotation.</li>
 *   <li>{@code @Target({TYPE, METHOD})} — where it may be written. Deliberately
 *       narrow: on a class, or on one method. {@code FIELD} and
 *       {@code PARAMETER} would compile but could never do anything, because
 *       interceptors wrap calls and fields are not calls.</li>
 *   <li>{@code RUNTIME} retention — the same trap as a qualifier. The default
 *       retention is {@code CLASS}, the label is dropped before the container
 *       reads it, and nothing is intercepted with no error to tell you why.</li>
 * </ul>
 *
 * <p>Related: {@link com.globalbank.qualifiers.Email} is the other kind of CDI
 * label, and comparing the two files side by side is the fastest way to see the
 * difference.</p>
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Logged {
}
