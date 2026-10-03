# Self-Study Block S1–S4: Comprehensive Revision Guide (Sessions 1–8)

**Manual Reference:** S1–S4 (Self-Study Revision & Practical Sessions — Sessions 1 to 8)  
**Target Specifications:** Jakarta EE 10 / WildFly 30+ / Java 17+  
**Recommended Study Allocation:** 4 to 6 Hours of active reading, architecture mapping, and code review.

---

## 📌 Executive Summary & Architecture Map

Book Sessions 1 through 8 form the **core backbone of Jakarta EE enterprise computing**. This block consolidates:
1. Enterprise Bean Container & Component Architecture (Sessions 1 & 2)
2. Resource Management, Connection Pooling & JNDI (Session 3)
3. Container-Managed Transactions (CMT), Concurrency & Security (Session 4)
4. Web Presentation with Facelets & Composite Components (Session 5)
5. Component Remoting & Client Topologies (Session 6)
6. Asynchronous Messaging with Jakarta JMS & MDBs (Session 7)
7. Enterprise Information System (EIS) Integration via JCA (Session 8)

```
       ┌─────────────────────────────────────────────────────────────┐
       │                   PRESENTATION TIER (S5)                    │
       │     JSF / Facelets Templates (<ui:composition>)             │
       │     Composite Components (<composite:interface>)            │
       └──────────────────────────────┬──────────────────────────────┘
                                      │ In-JVM Local Call / EL Binding
                                      ▼
       ┌─────────────────────────────────────────────────────────────┐
       │                    BUSINESS TIER (S1-S4, S6)                │
       │  @Stateless EJB   │  @Stateful EJB   │  @Singleton EJB      │
       │  (Pooled Workers) │  (Client State)  │  (Shared In-Memory)  │
       │  ─────────────────────────────────────────────────────────  │
       │  Container Services: CMT Transactions, Security, @Lock      │
       └──────────────┬──────────────────────────────┬───────────────┘
                      │                              │
         Asynchronous │ JMS                          │ JCA Connection Pool
         Message Flow │ (S7)                         │ (S3, S8)
                      ▼                              ▼
       ┌──────────────────────────────┐ ┌─────────────────────────────┐
       │    MESSAGING TIER (S7)       │ │     ENTERPRISE DATA (S3, S8)│
       │  Queue (P2P) / Topic (PubSub)│ │  PostgreSQL / Oracle / EIS  │
       │  @MessageDriven Bean (MDB)   │ │  JNDI: java:jboss/datasources│
       └──────────────────────────────┘ └─────────────────────────────┘
```

---

## 🧠 High-Yield Revision Matrix

### 1. Enterprise Bean Types & Lifecycles (Sessions 1 & 2)

| Feature | `@Stateless` | `@Stateful` | `@Singleton` |
|---|---|---|---|
| **Primary Use Case** | Business services, calculations, dispatchers | Shopping carts, multi-step wizards, conversational state | Caching, application counters, global locks |
| **Client Association** | Unbound; any client gets any pooled instance | Dedicated 1:1 binding per client session | Exactly one instance shared across all clients |
| **Instance Pooling** | Yes (WildFly default: 20–64 instances) | No (Instances are instantiated per client) | No (Single instance eagerly or lazily loaded) |
| **Passivation** | Never (No conversational state to preserve) | Yes (`@PrePassivate` / `@PostActivate` to disk) | Never |
| **Lifecycle Callbacks** | `@PostConstruct`, `@PreDestroy` | `@PostConstruct`, `@PrePassivate`, `@PostActivate`, `@PreDestroy`, `@Remove` | `@PostConstruct`, `@PreDestroy` |
| **Thread Safety** | Container ensures 1 thread per instance | Container ensures 1 thread per instance | Container Managed Concurrency (`@Lock(READ/WRITE)`) |

> **Gotcha Alert:** Never store client-specific state in instance variables of a `@Stateless` bean! The container returns instances to the pool and gives them to other callers, causing cross-tenant data leakage.

---

### 2. JNDI Portable Naming Architecture (Session 3)

Jakarta EE specifies four portable JNDI namespaces that guarantee vendor portability across WildFly, GlassFish, and WebLogic:

```
java:global[/<app-name>]/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
java:app/<module-name>/<bean-name>[!<fully-qualified-interface-name>]
java:module/<bean-name>[!<fully-qualified-interface-name>]
java:comp/env/<resource-ref-name>
```

* **`java:global`**: Accessible across all applications running on the entire application server instance, as well as remote standalone Java SE clients.
* **`java:app`**: Accessible only within modules sharing the same `.ear` enterprise archive.
* **`java:module`**: Accessible only within the same `.war` or `.jar` archive.
* **`java:comp`**: Private to the specific calling component instance (`java:comp/env`).

---

### 3. Container-Managed Transaction (CMT) Propagation (Session 4)

Annotated via `@TransactionAttribute(TransactionAttributeType.XXX)`:

| Transaction Attribute | Client Has Active Transaction | Client Has NO Transaction | Best Used For |
|---|---|---|---|
| **`REQUIRED`** *(Default)* | Joins existing transaction | Starts a new transaction | General database operations (updates, inserts) |
| **`REQUIRES_NEW`** | Suspends client tx; starts new independent tx | Starts a new transaction | Audit logging, notifications that must persist even if parent rolls back |
| **`MANDATORY`** | Joins existing transaction | Throws `TransactionRequiredException` | Internal helper methods that must never run without a transaction |
| **`SUPPORTS`** | Joins existing transaction | Executes non-transactionally | Read-only search methods |
| **`NOT_SUPPORTED`** | Suspends client tx; executes non-transactionally | Executes non-transactionally | Long-running operations, network calls |
| **`NEVER`** | Throws `RemoteException` | Executes non-transactionally | Strict non-transactional operations |

> **Key Rule for Rollback:** By default, the container **only rolls back for unchecked exceptions** (`RuntimeException` and `Error`). For checked application exceptions, you must explicitly mark `@ApplicationException(rollback = true)`.

---

### 4. Facelets & JSF Request Processing Lifecycle (Session 5)

When a browser submits a JSF form, the request traverses **6 sequential phases**:
1. **Restore View:** Reconstructs the component tree from session state.
2. **Apply Request Values:** Decodes submitted HTTP form parameters into component values.
3. **Process Validations:** Runs validators (`required="true"`, custom validators, Bean Validation). If any fail, JSF jumps directly to Phase 6!
4. **Update Model Values:** Converts and copies validated values to backing bean properties.
5. **Invoke Application:** Executes business action methods (e.g., `#{orderBean.submit()}`).
6. **Render Response:** Generates the resulting HTML markup to return to the browser.

---

### 5. Local vs. Remote Clients (Session 6)

| Attribute | `@Local` Interface | `@Remote` Interface |
|---|---|---|
| **Client Location** | Same JVM / Same Application Server | Different JVM / Standalone Java SE client |
| **Parameter Passing** | **Pass-by-Reference** (Memory address pointer, fast) | **Pass-by-Value** (Serialized into bytes over TCP) |
| **Serialization Requirement** | Not required | **Mandatory** (`implements Serializable`) |
| **Network Overhead** | Zero (Direct In-Memory call) | Socket I/O, marshalling/unmarshalling overhead |
| **Exception Handling** | Application exceptions thrown directly | Can throw `java.rmi.RemoteException` or EJB remote exceptions |

---

### 6. Jakarta Messaging Service (JMS 3.0) & JCA (Sessions 7 & 8)

* **Point-to-Point (Queue):** Exactly **one consumer** processes each message. Suitable for payment dispatch, order fulfillment, and ticket processing.
* **Publish/Subscribe (Topic):** **Every active subscriber** receives a copy of each published message. Suitable for price broadcasts, notifications, and cache invalidation.
* **Message-Driven Beans (`@MessageDriven`):** Asynchronous, transaction-aware listeners managed by the container. They implement `MessageListener` and do not expose client interfaces.
* **Jakarta Connectors Architecture (JCA):** Standardized adapter framework bridging Jakarta EE to external Enterprise Information Systems (Databases, Mainframes, ERPs). Implements three critical system contracts:
  1. *Connection Management Contract* (Pooling)
  2. *Transaction Management Contract* (2-Phase Commit / XA)
  3. *Work Management Contract* (Thread Scheduling)

---

## 🎯 High-Frequency Exam & Technical Review Questions

#### Q1: Why can a client NOT invoke a method on a `@Stateful` bean concurrently from two threads?
**Answer:** The EJB specification prohibits concurrent access to stateful session beans. If a second thread invokes a method on a stateful bean while an invocation is already in progress, the container throws a `ConcurrentAccessException`. If shared concurrent state is needed, developers must use a `@Singleton` bean configured with `@Lock(LockType.READ)` and `@Lock(LockType.WRITE)`.

#### Q2: What happens if a method annotated with `@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)` throws a `RuntimeException`?
**Answer:** The newly spawned independent transaction rolls back. The suspended parent transaction resumes. Unless the parent method catches the exception, the uncaught exception will propagate upward and cause the parent transaction to roll back as well.

#### Q3: What is the purpose of `<ui:insert>` and `<ui:define>` in Facelets?
**Answer:** `<ui:insert name="regionName">` in a master template declares a dynamic placeholder where child pages can inject content. Child pages use `<ui:composition template="...">` and `<ui:define name="regionName">` to provide page-specific markup for that region.
