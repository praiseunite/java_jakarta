package com.globalbank.qualifiers;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "The one that sends a text message."
 *
 * <p>Identical in shape to {@link Email}, and that is the point: a qualifier has
 * no behaviour. It is a name, not a mechanism. All it does is let an injection
 * point say <em>which</em> of several beans of the same type it wants.</p>
 *
 * <p>See {@link Email} for what each of the three annotations is for. The short
 * version: {@code @Qualifier} makes it a label, {@code RUNTIME} retention keeps
 * the label alive for CDI to read, and {@code @Target} says where it may be
 * written.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface Sms {
}
