# TL15 Class Task: Full CDI Pipeline — Qualifiers + Events + Interceptors

## 📌 Task Overview

Build a single checkout flow that chains together: **Qualifier-based injection**, an **Interceptor** for timing, and a **CDI Event** for post-payment notification.

---

## 📋 The Qualifier

```java
@Qualifier @Retention(RUNTIME) @Target({FIELD, TYPE, PARAMETER})
public @interface StripePayment {}
```

---

## 📋 The Payment Processor

```java
@StripePayment @ApplicationScoped
public class StripeProcessor implements PaymentProcessor {
    @PerformanceLogged // Our interceptor binding
    public void pay(double amount) {
        System.out.println("Stripe: Charged $" + amount);
    }
}
```

---

## 📋 The Checkout Controller

```java
@Named @RequestScoped
public class CheckoutController {

    @Inject @StripePayment
    private PaymentProcessor processor;

    @Inject
    private Event<PaymentCompletedEvent> events;

    public void checkout(double amount) {
        processor.pay(amount);
        events.fire(new PaymentCompletedEvent(amount));
    }
}
```

---

## 📋 The Observer

```java
@ApplicationScoped
public class ReceiptService {
    public void sendReceipt(@Observes PaymentCompletedEvent e) {
        System.out.println("Receipt sent for $" + e.amount());
    }
}
```

## ✅ Expected Log Output

```
[Interceptor] pay() starting...
Stripe: Charged $99.99
[Interceptor] pay() took 2ms.
Receipt sent for $99.99
```
