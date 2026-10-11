package com.globalbank.qualifiers;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "The one that sends an email."
 *
 * <p>A <strong>qualifier</strong> is a label you stick on a bean so you can ask
 * for that one specific bean when several of the same type exist.</p>
 *
 * <p>The three annotations below are the whole recipe, and each of them is
 * doing a job:</p>
 * <ul>
 *   <li>{@code @Qualifier} — this line is what makes it a CDI qualifier rather
 *       than just an annotation. Without it, {@code @Email} would be a comment
 *       that CDI ignores.</li>
 *   <li>{@code @Retention(RUNTIME)} — CDI reads this label while the application
 *       is <em>running</em>, so the label has to survive into the compiled
 *       class. The default for annotations is {@code CLASS}, which would be
 *       thrown away before CDI ever saw it. This is the line people forget.</li>
 *   <li>{@code @Target} — where the label may be written. {@code TYPE} means the
 *       class itself ({@code @Email class EmailNotifier}), {@code FIELD} and
 *       {@code PARAMETER} mean the injection points
 *       ({@code @Inject @Email Notifier notifier}). Leaving {@code Target} out
 *       would allow it anywhere, including places that make no sense.</li>
 * </ul>
 *
 * <p>Compare this with the string-based alternative, {@code @Named("email")}.
 * A typo in a string is found at runtime, by a person, in production. A typo in
 * {@code @Emial} does not compile.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Email {
}
