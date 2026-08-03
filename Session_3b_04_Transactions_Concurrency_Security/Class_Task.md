# Session 4 — Class Task (TIY - Try It Yourself)
## Working with Transactions in EJBs

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 4)
**Duration:** 45 minutes

---

## 🎯 Task Objective

In this hands-on task, you will:
1. Observe how default Container-Managed Transactions (`REQUIRED`) work.
2. Implement the `REQUIRES_NEW` transaction attribute to create an Audit Log that survives transaction rollbacks.
3. Intentionally throw a `RuntimeException` to trigger a rollback and verify the behavior.

---

## 🛠️ Step-by-Step Instructions

### Step 1: Create the Audit Logging Bean

First, we need a bean that logs events. We want this bean to ALWAYS save its data, even if the method that called it fails and rolls back.

1. In your `GlobalBank` project, create a new Stateless Session Bean named `AuditLogBean`.
2. Apply the `@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)` annotation.
3. Write a method to simulate saving an audit record.

```java
package com.globalbank.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateless
public class AuditLogBean {

    // This ensures a BRAND NEW transaction is created every time this is called.
    // If the caller rolls back, this transaction is unaffected.
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void logEvent(String eventDetails) {
        // In a real app, this would use JPA to save to the database.
        // For this task, we will just print to the console.
        System.out.println(">>> AUDIT SECURE LOG: Saved to database: " + eventDetails);
    }
}
```

### Step 2: Create the Main Transaction Bean

Now we create the main business bean. It will start a transaction, call the audit log, and then (for testing) intentionally fail.

1. Create a new Stateless Session Bean named `TransferBean`.
2. Inject the `AuditLogBean` into it.
3. Write a `transferFunds` method that throws an exception halfway through.

```java
package com.globalbank.ejb;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class TransferBean {

    @Inject
    private AuditLogBean auditLog;

    // By default, this method runs with TransactionAttributeType.REQUIRED
    public void transferFunds(String fromAccount, String toAccount, double amount) {
        
        System.out.println("Starting transfer transaction...");
        
        // Step 1: Log the attempt (Runs in a NEW transaction)
        auditLog.logEvent("Attempting transfer of $" + amount + " from " + fromAccount);

        // Step 2: Simulate a failure during the transfer
        // Throwing a RuntimeException automatically triggers a ROLLBACK 
        // for the CURRENT transaction (the REQUIRED one).
        System.out.println("Simulating system crash...");
        throw new RuntimeException("CRITICAL ERROR: Database connection lost during transfer!");
    }
}
```

### Step 3: Test the Rollback Behavior

1. Create a simple Servlet or a REST endpoint to call `TransferBean.transferFunds()`.
2. Deploy the application to WildFly.
3. Trigger the method.
4. **Observe the WildFly Server Console Log.**

**What you should see in the logs:**
```
Starting transfer transaction...
>>> AUDIT SECURE LOG: Saved to database: Attempting transfer of $500.0 from ACC123
Simulating system crash...
jakarta.ejb.EJBException: java.lang.RuntimeException: CRITICAL ERROR...
(Stack trace follows, indicating the transaction was rolled back)
```

---

## 👩‍🏫 Instructor Verification

Call your instructor over and explain the console output. Be prepared to answer:

1. **The Core Question:** Why did the "AUDIT SECURE LOG" message still print successfully, even though the `transferFunds` method threw an exception and rolled back?
2. **What if...?** If you changed the annotation in `AuditLogBean` to `@TransactionAttribute(TransactionAttributeType.REQUIRED)`, what would happen differently in a real database scenario?

---

## 🆘 Common Errors & Troubleshooting

- **Error:** `Cannot resolve symbol TransactionAttributeType`
  - **Why:** You forgot to import the correct Jakarta EE classes.
  - **Fix:** Add `import jakarta.ejb.TransactionAttributeType;` at the top of your file.

- **Error:** The EJB is not deploying properly.
  - **Why:** You might have cyclic dependencies or a syntax error in your bean.
  - **Fix:** Check the top of the WildFly log for `DeploymentException` to see exactly which class failed to compile or deploy.
