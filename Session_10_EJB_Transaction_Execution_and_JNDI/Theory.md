# Session 10: Execution of Enterprise Beans Using Transactions and JNDI

Welcome to **Session 10: Execution of Enterprise Beans Using Transactions**. In earlier sessions, we introduced basic transaction attributes. This session dives into the engine room of **advanced transaction execution**: how transactions propagate across distributed EJB boundaries, how to take manual programmatic control with **Bean-Managed Transactions (BMT)** using `UserTransaction` (often looked up via **JNDI**), how to control rollbacks, and how stateful beans synchronize cache with `SessionSynchronization`.

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Trace** transaction propagation across nested and distributed enterprise bean calls.
2. **Implement** programmatic Bean-Managed Transactions (BMT) using the `UserTransaction` API.
3. **Lookup** `UserTransaction` and other resources via JNDI (Java Naming and Directory Interface).
4. **Differentiate** rollback semantics between Application Exceptions and System (Runtime) Exceptions.
5. **Apply** programmatic rollback control via `EJBContext.setRollbackOnly()`.
6. **Implement** the `SessionSynchronization` interface to manage stateful bean caching across transaction commits.

---

## 1. Transaction Propagation Across EJB Boundaries

In enterprise systems, business workflows rarely fit in a single method. A checkout service invokes an inventory service, which calls a billing service, which updates an audit log. **Transaction Propagation** governs how the active transaction context travels across these method calls:

| Caller (Bean A) | Callee Attribute (Bean B) | Resulting Transaction Behavior |
| :--- | :--- | :--- |
| In Transaction $T_1$ | `REQUIRED` | Bean B executes inside the **same transaction ($T_1$)**. If B fails, A rolls back too. |
| In Transaction $T_1$ | `REQUIRES_NEW` | $T_1$ is **suspended**. Container starts a fresh transaction $T_2$ for Bean B. When B finishes, $T_2$ commits and $T_1$ resumes. |
| In Transaction $T_1$ | `MANDATORY` | Bean B joins $T_1$. (If Caller had no transaction, B would throw `TransactionRequiredException`). |
| In Transaction $T_1$ | `NOT_SUPPORTED` | $T_1$ is suspended. Bean B runs without any transaction context until B returns. |
| In Transaction $T_1$ | `NEVER` | 🚨 Container throws `EJBException` immediately because a transaction is active. |

---

## 2. Bean-Managed Transactions (BMT)

While Container-Managed Transactions (CMT) cover 95% of use cases, certain batch workflows (e.g., importing 100,000 rows where committing every 500 rows is required) mandate manual programmatic control using **Bean-Managed Transactions (BMT)**:

| Method on `UserTransaction` | Function | Throws |
| :--- | :--- | :--- |
| `utx.begin()` | Starts a new transaction on the current thread | `NotSupportedException` (if already in tx) |
| `utx.commit()` | Commits all changes made within the transaction | `RollbackException`, `HeuristicMixedException` |
| `utx.rollback()` | Aborts and reverses all modifications | `IllegalStateException`, `SecurityException` |
| `utx.setTransactionTimeout(sec)` | Sets maximum seconds before auto-abort | `SystemException` |
| `utx.getStatus()` | Returns current status (e.g., `STATUS_ACTIVE`) | `SystemException` |

### JNDI Lookup vs Dependency Injection

You can obtain the `UserTransaction` either by using the `@Resource` annotation or by looking it up in the JNDI registry:

**Via Injection:**
```java
@Resource
private UserTransaction utx;
```

**Via JNDI Lookup (Legacy / Standalone):**
```java
Context ctx = new InitialContext();
UserTransaction utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
```

### Example: Chunked Batch Processor
```java
package com.globalbank.batch;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.transaction.UserTransaction;
import java.util.List;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN) // ① Declare BMT
public class BatchImportService {

    @Resource
    private UserTransaction utx; // ② Inject JTA UserTransaction coordinator

    public void processBatchRecords(List<String> records) {
        int batchSize = 500;

        for (int i = 0; i < records.size(); i += batchSize) {
            try {
                utx.begin(); // ③ Explicitly open transaction boundary
                utx.setTransactionTimeout(60);

                List<String> sublist = records.subList(i, Math.min(i + batchSize, records.size()));
                for (String record : sublist) {
                    insertRecord(record);
                }

                utx.commit(); // ④ Commit this chunk
            } catch (Exception e) {
                try {
                    utx.rollback(); // ⑤ Abort only this failed chunk
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        }
    }

    private void insertRecord(String rec) { /* JDBC logic */ }
}
```

---

## 3. Exception Classification & Rollback Rules

In Container-Managed Transactions (CMT), how does the container decide whether to commit or rollback when an exception occurs?

| Exception Type | Examples | Container Reaction | Bean Instance Lifecycle |
| :--- | :--- | :--- | :--- |
| **System Exception** (Unchecked) | `RuntimeException`, `NullPointerException`, `EJBException` | **Automatic Rollback** | Container discards the bean instance from the pool. |
| **Application Exception** (Checked by default) | `InsufficientFundsException`, `AccountFrozenException` | **COMMITS by default!** | Bean instance is retained safely in the pool. |
| **Application Exception with Rollback** | `@ApplicationException(rollback = true)` | **ROLLBACK forced** | Bean instance retained in pool, transaction rolled back. |

> [!WARNING]
> **Programmatic Rollback in CMT: `setRollbackOnly()`**
> If you catch an exception inside a CMT bean but still want the container to abort the transaction, call:
> `ejbContext.setRollbackOnly();`
> This marks the transaction so that no matter what else happens, the container will execute a **ROLLBACK** upon method exit.

---

## 4. Stateful Bean Caching: `SessionSynchronization`

When a `@Stateful` session bean manages cached data across a transaction, it can implement the `SessionSynchronization` interface to receive container notifications at critical checkpoints:

| Callback Method | When Container Invokes It | Typical Use Case |
| :--- | :--- | :--- |
| `afterBegin()` | Immediately after the transaction is started. | Pre-load or lock cached data from the database into bean memory. |
| `beforeCompletion()` | Right before the transaction manager issues the `COMMIT`. | Flush dirty cached changes from bean fields down to the database. |
| `afterCompletion(boolean committed)` | Immediately after the transaction commits or rolls back. | If `true`, update UI state. If `false`, discard modified cache and reload from DB. |

---

## 5. Summary

* **BMT**: Programmatic transactions via `@TransactionManagement(BEAN)` and `UserTransaction`.
* **Propagation**: Governs how transaction contexts carry over across nested bean invocations.
* **System Exceptions**: Trigger automatic rollback and bean destruction by container.
* **Application Exceptions**: Commit by default; use `@ApplicationException(rollback=true)` or `setRollbackOnly()` to abort.
* **`SessionSynchronization`**: Hooks for stateful cache coherence during transactions.
