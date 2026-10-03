# Self-Study Block S5–S8: Comprehensive Revision Guide (Sessions 9–14)

**Manual Reference:** S5–S8 (Self-Study Revision & Practical Sessions — Sessions 9 to 14)  
**Target Specifications:** Jakarta EE 10 / WildFly 30+ / Java 17+  
**Recommended Study Allocation:** 4 to 6 Hours of active reading, architecture mapping, and code review.

---

## 📌 Executive Summary & Architecture Map

Book Sessions 9 through 14 cover **modern, decoupled enterprise application development** in Jakarta EE:
1. Declarative Data Integrity via **Bean Validation** (Session 9)
2. Fine-grained Transaction Control via **Bean-Managed Transactions (BMT)** and JNDI (Session 10)
3. Dependency Injection, Contexts, and Scopes via **CDI Part I** (Session 11)
4. Advanced CDI: Stereotypes, EL Resolution & Discovery Modes via **CDI Part II** (Session 12)
5. Decoupled Architecture: **Qualifiers, Producers, Interceptors, Events & Security** (Session 13)
6. Enterprise Packaging: **EJB JARs, JPA Persistence, Skinny WARs & EARs** (Session 14)

```
       ┌─────────────────────────────────────────────────────────────┐
       │                   CDI WEB CONTAINER (S11, S12)              │
       │  @RequestScoped  │  @SessionScoped  │  @ApplicationScoped   │
       │  ─────────────────────────────────────────────────────────  │
       │  Bean Validation (@NotNull, @Email, Custom) (S9)            │
       └──────────────────────────────┬──────────────────────────────┘
                                      │ @Inject (Type-Safe Injection)
                                      ▼
       ┌─────────────────────────────────────────────────────────────┐
       │                DECOUPLED BUSINESS LOGIC (S13)               │
       │  @Qualifier (Custom Binding) │ @Produces (Factory Methods)  │
       │  @AroundInvoke (Interceptors)│ @ObservesAsync (CDI Events)  │
       │  ─────────────────────────────────────────────────────────  │
       │  Security: @RolesAllowed, SecurityContext, IdentityStore    │
       └──────────────────────────────┬──────────────────────────────┘
                                      │
                                      ▼
       ┌─────────────────────────────────────────────────────────────┐
       │             TRANSACTION & PACKAGING LAYER (S10, S14)        │
       │  BMT: UserTransaction.begin() / commit() / rollback()       │
       │  Packaging: Maven Multi-Module -> Skinny WAR / Enterprise EAR│
       │  persistence.xml (JPA 3.1) + beans.xml (CDI 4.0)            │
       └─────────────────────────────────────────────────────────────┘
```

---

## 🧠 High-Yield Revision Matrix

### 1. Jakarta Bean Validation (Session 9)

Bean Validation 3.0 provides declarative constraints at the model layer:

| Constraint | Type Supported | Validation Rule |
|---|---|---|
| `@NotNull` | Any Object | Value must not be null (empty string `""` is allowed). |
| `@NotEmpty` | `CharSequence`, `Collection`, `Map` | Must not be null, and `size()` / `length()` must be $> 0$. |
| `@NotBlank` | `CharSequence` | Must not be null and must contain at least one non-whitespace character. |
| `@Size(min, max)` | `String`, `Collection`, `Array` | Size must be between `min` and `max` inclusive. |
| `@Min` / `@Max` | Numeric primitives and wrappers | Evaluates minimum/maximum numeric value. |
| `@Email` | `CharSequence` | Must match valid email RFC address syntax. |
| `@Past` / `@Future` | `LocalDate`, `Instant`, `Date` | Timestamp must strictly precede or follow the current system clock. |
| `@Pattern(regexp)` | `CharSequence` | Must match the supplied regular expression. |

#### Custom Constraint Architecture:
A custom constraint requires two artifacts:
1. **Annotation Definition:** Annotated with `@Constraint(validatedBy = MyValidator.class)`, `@Target`, and `@Retention(RUNTIME)`.
2. **Validator Class:** Implements `ConstraintValidator<MyAnnotation, TargetType>`.

---

### 2. EJB Transactions: BMT vs. CMT (Session 10)

| Criterion | Container-Managed Transactions (CMT) | Bean-Managed Transactions (BMT) |
|---|---|---|
| **Demarcator** | Container via `@TransactionAttribute` | Developer via `UserTransaction` API |
| **Methods Available** | N/A (Handled declaratively) | `utx.begin()`, `utx.commit()`, `utx.rollback()` |
| **Injection** | Container handles lifecycle automatically | `@Resource private UserTransaction utx;` |
| **Configuration** | Default (or `@TransactionManagement(CONTAINER)`) | `@TransactionManagement(TransactionManagementType.BEAN)` |
| **Applicable Beans** | `@Stateless`, `@Stateful`, `@Singleton`, `@MessageDriven` | `@Stateless`, `@Stateful`, `@MessageDriven` (NOT `@Singleton`) |
| **Best Used For** | 95% of standard CRUD and business workflows | Multi-phase batch processing, manual checkpointing, non-standard rollbacks |

> **Crucial Rule:** In a BMT bean, you cannot leave a transaction uncommitted across method invocations in a `@Stateless` bean! The transaction MUST begin and end within the same method call.

---

### 3. CDI Scopes & Lifecycles (Sessions 11 & 12)

CDI 4.0 defines standard contextual scopes:

| CDI Scope | Annotation | Context Lifespan | Typical Use Case |
|---|---|---|---|
| **Dependent** *(Default)* | `@Dependent` | Shares the exact lifespan of the bean into which it is injected. | Utility classes, loggers, stateless helpers. |
| **Request** | `@RequestScoped` | One single HTTP request / response cycle. | Form backing beans, search query parameters. |
| **Session** | `@SessionScoped` | Survives across multiple HTTP requests from the same browser session. Must be `Serializable`. | User authentication state, shopping cart. |
| **Application** | `@ApplicationScoped` | Lives as long as the deployed web application is active. Shared across all users. | System configuration caches, global counters. |
| **Conversation** | `@ConversationScoped` | Manually demarcated across multiple requests (`conversation.begin()` to `conversation.end()`). | Multi-step registration wizards. |

---

### 4. CDI Qualifiers, Producers, Interceptors & Events (Session 13)

* **Qualifiers (`@Qualifier`):** Disambiguate when multiple implementations of an interface exist. Eliminates brittle string names:
  ```java
  @Inject @CreditCard PaymentGateway gateway;
  ```
* **Producer Methods (`@Produces`):** Turn third-party classes, factory outputs, or dynamic configurations into injectable CDI beans:
  ```java
  @Produces @ApplicationScoped public Connection createDBConnection() { ... }
  ```
* **Interceptors (`@InterceptorBinding` + `@AroundInvoke`):** Separate cross-cutting concerns (logging, performance metrics, security auditing) from business code:
  ```java
  @AroundInvoke public Object audit(InvocationContext ctx) throws Exception { ... return ctx.proceed(); }
  ```
* **CDI Events (`Event<T>` and `@Observes`):** In-memory event bus providing pure architectural decoupling:
  - Synchronous: `event.fire(new OrderPlacedEvent(order));`
  - Asynchronous: `event.fireAsync(new OrderPlacedEvent(order));` observed by `void onOrder(@ObservesAsync OrderPlacedEvent event)`.

---

### 5. Packaging Architectures (Session 14)

1. **EJB JAR (`ejb-jar.xml`):** Contains compiled business interfaces and session beans. Optional in modern Jakarta EE.
2. **Web Archive (WAR):** Contains web resources (`WEB-INF/web.xml`, JSF views, Servlets, and CDI beans in `WEB-INF/classes`).
3. **Enterprise Archive (EAR):** Packages multiple WARs and EJB JARs into a single enterprise distribution (`META-INF/application.xml`).
4. **Skinny WAR:** Modern recommended architecture. Packages EJBs, JPA Entities, CDI beans, and web presentation all inside a single `.war` file without requiring an `.ear`.

---

## 🎯 High-Frequency Exam & Technical Review Questions

#### Q1: What is the difference between `@SessionScoped` in CDI and `@Stateful` in EJB?
**Answer:** `@SessionScoped` CDI beans are bound to the HTTP session and manage web-tier user state. `@Stateful` EJBs are transactional business beans with container-managed clustering, failover, and passivation (`@PrePassivate` / `@PostActivate`). Modern applications frequently inject a `@Stateful` EJB into a `@SessionScoped` CDI backing bean.

#### Q2: What is the difference between CDI Events and JMS Messaging?
**Answer:** CDI Events are lightweight, in-memory events dispatched within the same JVM without network serialization overhead. Jakarta JMS is an external messaging subsystem supporting distributed queues/topics, persistent message storage on disk, and message delivery across distinct servers.

#### Q3: Why is `beans.xml` required in `WEB-INF/` or `META-INF/`?
**Answer:** `beans.xml` instructs the CDI container how to discover beans within the archive. Setting `bean-discovery-mode="annotated"` scans only classes with CDI scope annotations (fastest startup). Setting `bean-discovery-mode="all"` scans every class in the JAR/WAR.
