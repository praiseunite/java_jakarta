# Session 12: Jakarta CDI (Part II)

Welcome to **Session 12: Jakarta CDI (Part II)**. Session 11 covered foundational dependency injection and scopes. Session 12 unlocks the advanced powers of CDI: disambiguating multiple implementations with **Qualifiers**, manufacturing custom objects with **Producers & Disposers**, applying cross-cutting logic with **Interceptors & Decorators**, and event-driven decoupling with **CDI Events**.

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Resolve** ambiguous dependencies using custom type-safe **Qualifiers** (`@Qualifier`).
2. **Dynamically instantiate** external classes and configurations using **Producers** (`@Produces`) and **Disposers** (`@Disposes`).
3. **Implement** Aspect-Oriented cross-cutting concerns using CDI **Interceptors** (`@InterceptorBinding`, `@AroundInvoke`).
4. **Enhance** and customize business logic transparently with **Decorators** (`@Decorator`, `@Delegate`).
5. **Implement** decoupled event-driven communication using synchronous and asynchronous **CDI Events** (`Event<T>`, `@Observes`).

---

## 1. Qualifiers: Resolving Ambiguous Dependencies

If you enter a hospital asking for "a Doctor", the receptionist doesn't know who to call — there is a Surgeon, a Cardiologist, and a Pediatrician. CDI hits the exact same wall if two beans implement `PaymentService`.

Instead of using fragile string names (like `@Named("creditCard")`), CDI uses **Qualifiers** — custom annotations that provide compiler-checked, type-safe differentiation.

### Defining and Using a Custom Qualifier

**1. Define the Qualifier Annotations**
```java
package com.globalbank.qualifiers;

import jakarta.inject.Qualifier;
import java.lang.annotation.*;

@Qualifier // ① Marks this annotation as a CDI Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface CreditCard {}

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
public @interface PayPal {}
```

**2. Tag the Concrete Implementations**
```java
@CreditCard
public class CreditCardProcessor implements PaymentProcessor {
    public void pay(double amt) { System.out.println("Paid via Credit Card: $" + amt); }
}

@PayPal
public class PayPalProcessor implements PaymentProcessor {
    public void pay(double amt) { System.out.println("Paid via PayPal: $" + amt); }
}
```

**3. Inject with Type Safety**
```java
public class CheckoutService {
    // CDI knows EXACTLY which implementation to supply based on the qualifier tag!
    @Inject @CreditCard
    private PaymentProcessor cardService;

    @Inject @PayPal
    private PaymentProcessor payPalService;
}
```

---

## 2. Producers (`@Produces`) and Disposers (`@Disposes`)

What if you want to inject an object from a third-party library that doesn't have a no-argument constructor, or dynamically configure a connection based on runtime environment variables? You use a **Producer Method**:

```java
package com.globalbank.producers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@ApplicationScoped
public class DatabaseConnectionProducer {

    // ① @Produces turns this method into a factory for @RequestScoped Connections
    @Produces
    @RequestScoped
    public Connection createConnection() throws SQLException {
        String url = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/bank");
        System.out.println("CDI Producer: Opening physical connection for request...");
        return DriverManager.getConnection(url, "user", "secret");
    }

    // ② @Disposes automatically executes when the RequestScope ends, closing the connection!
    public void closeConnection(@Disposes Connection connection) {
        System.out.println("CDI Disposer: Deterministically closing connection on request exit.");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## 3. Interceptors: Aspect-Oriented Logging & Auditing

Don't clutter your business methods with timing code, security checks, and audit logging. **Interceptors** execute transparently around method invocations:

**1. The Interceptor Binding Annotation**
```java
package com.globalbank.interceptors;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@InterceptorBinding // Declares an interceptor contract
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PerformanceLogged {}
```

**2. The Interceptor Implementation**
```java
package com.globalbank.interceptors;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
@PerformanceLogged // Links to binding
@Priority(Interceptor.Priority.APPLICATION)
public class PerformanceInterceptor {

    @AroundInvoke
    public Object profile(InvocationContext context) throws Exception {
        long start = System.currentTimeMillis();
        String methodName = context.getMethod().getName();

        try {
            // Proceed to execute the actual target business method
            return context.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("[AUDIT PROFILE] Method '" + methodName + "' executed in " + duration + "ms.");
        }
    }
}
```

**3. Applying the Interceptor**
```java
@Stateless
public class LoanService {
    @PerformanceLogged // Transparently profiled!
    public void approveLoan(int customerId, double amount) {
        // Pure business logic — zero logging boilerplate here!
    }
}
```

---

## 4. Decoupled Event Architecture: Synchronous & Asynchronous

CDI provides a built-in publish-subscribe notification model that decouples components in-process without requiring heavyweight JMS message brokers:

```java
// 1. Event Payload POJO
public record CustomerCreatedEvent(int customerId, String email) {}

// 2. Event Producer
@Stateless
public class CustomerRegistrationService {

    @Inject
    private Event<CustomerCreatedEvent> eventPublisher;

    public void register(String email) {
        int id = 12345;
        // Fire event synchronously (or fireAsync() for background threads)
        eventPublisher.fire(new CustomerCreatedEvent(id, email));
        System.out.println("Registration complete.");
    }
}

// 3. Independent Observer
@ApplicationScoped
public class WelcomeEmailNotifier {

    // Runs automatically whenever CustomerCreatedEvent is fired!
    public void onNewCustomer(@Observes CustomerCreatedEvent event) {
        System.out.println("Sending welcome package email to: " + event.email());
    }
}
```

---

## 5. Summary

* **Qualifiers:** Type-safe annotations (`@Qualifier`) resolving ambiguous bean implementations at compile time.
* **Producers:** `@Produces` factories manufacturing dynamic objects; cleaned up by `@Disposes`.
* **Interceptors:** AOP cross-cutting wrappers (`@InterceptorBinding`, `@AroundInvoke`) for timing, logging, and security.
* **Decorators:** Implements the business interface with `@Delegate` to augment domain logic.
* **CDI Events:** In-process decoupled messaging via `Event<T>.fire()` and `@Observes` methods.
