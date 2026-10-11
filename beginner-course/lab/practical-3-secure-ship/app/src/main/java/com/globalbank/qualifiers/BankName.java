package com.globalbank.qualifiers;

import jakarta.inject.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * "The bank's own name, as configured."
 *
 * <p>Used with a {@code @Produces} field in
 * {@link com.globalbank.config.BankConfiguration}. The value is a plain
 * {@link String}, and that matters for a reason worth knowing:</p>
 *
 * <p><strong>A producer of a non-proxyable type must be {@code @Dependent}.</strong>
 * When a bean has a normal scope — {@code @ApplicationScoped},
 * {@code @RequestScoped} and so on — CDI hands injection points a
 * <em>proxy</em>: a stand-in object that looks up the real instance on each
 * call. A proxy works by subclassing. {@code String} is {@code final}, so it
 * cannot be subclassed, so CDI cannot proxy it.</p>
 *
 * <p>The consequence is not a warning you can ignore. Giving this producer
 * {@code @ApplicationScoped} is a <strong>definition error</strong>: the
 * application refuses to deploy. The same is true of {@code int}, and of any
 * {@code final} class or primitive. Leave the scope annotation off — the
 * default is {@code @Dependent} — and the value is simply created again for each
 * injection point, which for an immutable configured value costs nothing.</p>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface BankName {
}
