# Session 11: Jakarta CDI (Part I)

Welcome to **Session 11: Jakarta CDI (Part I)**. Throughout this course, you have seen annotations like `@Inject` and `@Named`. **Jakarta Contexts and Dependency Injection (CDI)** is the foundational wiring backbone of modern Jakarta EE. It manages the creation, lifecycle, and type-safe injection of application components across all tiers.

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Define** Contexts and Dependency Injection (CDI) and explain its core architecture.
2. **Configure** CDI bean archives using `beans.xml` and understand discovery modes.
3. **Master** all 5 core CDI scopes: `@Dependent`, `@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`, and `@ConversationScoped`.
4. **Apply** type-safe dependency injection using `@Inject` on fields, constructors, and initializer methods.
5. **Contrast** `@Inject` (CDI) with `@EJB` and `@Resource`.

---

## 1. The CDI Architecture: Loose Coupling & Strong Typing

In traditional Java, creating objects via `new ServiceImpl()` ties your code to one concrete implementation. With CDI, the container supplies the instance at runtime, managing its exact lifecycle according to its contextual scope.

### Enabling CDI: The `beans.xml` Marker File
For a JAR or WAR module to be recognized as a CDI bean archive, it must contain a `beans.xml` file in `META-INF/` (for JARs) or `WEB-INF/` (for WARs):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="annotated">
</beans>
```

| Bean Discovery Mode | Which Classes Become CDI Beans? |
| :--- | :--- |
| `annotated` (Default & Recommended) | Only classes with an explicit scope annotation (e.g., `@RequestScoped`, `@ApplicationScoped`). Keeps startup fast. |
| `all` | Every Java class in the archive is treated as a CDI bean (can slow down deployment). |
| `none` | CDI is completely disabled for this archive. |

---

## 2. The Five Core CDI Scopes

A **Scope** defines two things: *how many instances exist* and *how long they live in memory*:

| Scope Annotation | Package | Lifecycle Duration | Typical Use Case |
| :--- | :--- | :--- | :--- |
| `@Dependent` (Default) | `jakarta.enterprise.context` | Lives and dies with the bean that injected it. No shared state. | Stateless utility helpers, loggers, mathematical converters. |
| `@RequestScoped` | `jakarta.enterprise.context` | One single HTTP request/response cycle. Destroyed when response returns. | JSF form backing beans, REST endpoint controllers. |
| `@SessionScoped` | `jakarta.enterprise.context` | One browser session. **Must implement `Serializable`**. | Logged-in user credentials, shopping cart, user theme preferences. |
| `@ApplicationScoped` | `jakarta.enterprise.context` | Single shared instance for the entire lifetime of the application. | Global application configuration, in-memory caches, hit counters. |
| `@ConversationScoped` | `jakarta.enterprise.context` | Spans multiple requests controlled programmatically via `Conversation.begin()` and `end()`. | Multi-step wizards (e.g., Book Flight $\to$ Select Seat $\to$ Payment). |

---

## 3. The Three Ways to Apply `@Inject`

CDI supports three injection points. Constructor injection is widely recognized as the most testable:

| Injection Pattern | Code Example | Architectural Trade-Off |
| :--- | :--- | :--- |
| **Field Injection** | `@Inject private AccountService service;` | Convenient and short, but cannot make fields `final`; harder to test with plain JUnit mocks. |
| **Constructor Injection** *(Best Practice)* | `@Inject public Controller(AccountService s) { this.service = s; }` | Allows `final` immutable fields; cleanly testable with `new Controller(mockService)` in unit tests. |
| **Initializer Method** | `@Inject public void init(AccountService s) { ... }` | Invoked by container after constructor; useful for multi-dependency configuration setups. |

---

## 4. Disambiguating `@Inject` vs. `@EJB` vs. `@Resource`

| Annotation | Specification | What It Injects | Understands CDI Scopes? |
| :--- | :--- | :--- | :--- |
| `@Inject` | Jakarta CDI | Any CDI bean, EJB, or produced object. | **Yes** (Full scope, qualifier, and decorator awareness) |
| `@EJB` | Jakarta Enterprise Beans | Enterprise Beans (Stateless, Stateful, Singleton) only. | **No** (Legacy EJB container lookup) |
| `@Resource` | Jakarta Common Annotations | Server-managed environment resources (DataSources, JMS queues). | **No** (Direct JNDI registry injection) |

---

## 5. Summary

* **CDI Core:** Contextual dependency injection managing instance creation, wiring, and lifecycle.
* **`beans.xml`:** Required marker file setting `bean-discovery-mode="annotated"`.
* **Five Scopes:** `@Dependent` (default), `@RequestScoped`, `@SessionScoped` (serializable), `@ApplicationScoped`, `@ConversationScoped`.
* **Constructor Injection:** Preferred pattern allowing `final` fields and simple unit testing.
* **`@Inject` vs `@EJB`:** `@Inject` is modern, universal, and scope-aware; `@EJB` is legacy EJB-only.
