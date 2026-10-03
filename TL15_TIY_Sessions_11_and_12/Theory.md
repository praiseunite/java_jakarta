# TL15 — Try It Yourself: Review of Sessions 11 & 12

**Manual Reference:** JAKARTA EE – TL15  
**Book Coverage:** Session 11 (CDI Part I) + Session 12 (CDI Part II)

---

## 📚 Recap — Session 11: Jakarta CDI Part I

### CDI Core Architecture
CDI (Contexts and Dependency Injection) is the backbone wiring framework of Jakarta EE. The container manages object creation, lifecycle, and injection.

**Enabling CDI:** `WEB-INF/beans.xml` with `bean-discovery-mode="annotated"` (preferred).

### The Five CDI Scopes

| Scope | Lifetime | Must Serializable? | Use Case |
|---|---|---|---|
| `@Dependent` | Lives with its injector | No | Stateless helpers, utilities |
| `@RequestScoped` | One HTTP request | No | Form backing beans |
| `@SessionScoped` | One browser session | **Yes** | Shopping cart, logged-in user |
| `@ApplicationScoped` | Full app lifetime | No | Config cache, counters |
| `@ConversationScoped` | Controlled multi-request | **Yes** | Multi-step wizards |

### Injection Styles

| Style | Code | Notes |
|---|---|---|
| Field | `@Inject private Service s;` | Simple; not final-friendly |
| Constructor | `@Inject public C(Service s) {...}` | Best for testability |
| Initializer Method | `@Inject public void init(Service s) {...}` | Multiple args at once |

### `@Inject` vs `@EJB` vs `@Resource`

| Annotation | Scope-Aware? | Injects |
|---|---|---|
| `@Inject` | ✅ Yes | CDI beans, EJBs, produced objects |
| `@EJB` | ❌ No | EJBs only (legacy) |
| `@Resource` | ❌ No | Server resources (DataSource, JMS) |

---

## 📚 Recap — Session 12: Jakarta CDI Part II

### Qualifiers — Disambiguating Multiple Implementations
When two beans implement the same interface, use `@Qualifier`-annotated custom annotations to tell CDI which one to inject:
```java
@Inject @CreditCard private PaymentProcessor cardProcessor;
@Inject @PayPal    private PaymentProcessor payPalProcessor;
```

### Producers & Disposers
- `@Produces` — Marks a factory method that CDI calls to create an instance.
- `@Disposes` — Marks a cleanup method called when the produced bean's scope ends.
- Use case: Injecting objects that cannot be instantiated by CDI natively (e.g., third-party classes with required constructor args).

### CDI Interceptors
1. Define binding annotation (`@InterceptorBinding`).
2. Implement the `@Interceptor` class with `@AroundInvoke`.
3. Apply the binding annotation to any target bean or method.
4. Enable in `beans.xml` or via `@Priority`.

### CDI Events (Pub/Sub)
```java
// Publisher
@Inject Event<OrderEvent> eventBus;
eventBus.fire(new OrderEvent(orderId));

// Observer
public void onOrder(@Observes OrderEvent e) { /* react */ }
```

---

## 🔁 TIY Exercise Checklist

- [ ] Build a store page with `@ApplicationScoped` visit counter, `@SessionScoped` shopping cart, and `@RequestScoped` page token. Open two browsers — verify counter is shared, carts are separate.
- [ ] Resolve ambiguous injection of two `TaxCalculator` implementations using custom `@Qualifiers`.
- [ ] Create a `@Produces` method to inject a `java.util.Properties` object loaded from a config file.
- [ ] Build an `@AroundInvoke` interceptor that logs every method call with a timestamp.

---

## ❓ Self-Test Questions

1. What happens to a `@SessionScoped` bean if it is not `Serializable`?
2. What is an `AmbiguousResolutionException` and how do you fix it?
3. What is the difference between an Interceptor and a Decorator?
4. What method in CDI events makes the notification asynchronous (non-blocking)?
5. If a `@Produces @RequestScoped` method creates a JDBC Connection, when does the matching `@Disposes` method run?
