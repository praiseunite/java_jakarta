package com.globalbank.qualifiers;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "How many audit lines to show", as configured rather than hard-coded.
 *
 * <p>Produced by a {@code @Produces} <em>method</em> in
 * {@link com.globalbank.config.BankConfiguration}, which is the other shape a
 * producer can take. A producer method may take injected parameters and may run
 * code, so it can work the value out; a producer field can only hold a value
 * that is already known.</p>
 *
 * <p>Like {@link BankName}, this produces a primitive and therefore must stay
 * {@code @Dependent} — an {@code int} cannot be proxied. See {@link BankName}
 * for the full explanation, because it is the mistake most likely to stop your
 * application deploying.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface AuditDepth {
}
