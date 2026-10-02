# Session 10 Class Task: EJB Transactions and Exception Handling

## 📌 Task Overview

In this hands-on lab, you will explore Container-Managed Transactions (CMT), exception handling, and programmatic transaction rollback. You will create a banking scenario to demonstrate how different exception types affect the transaction lifecycle.

You will:
1. **Create an Application Exception** that triggers a rollback.
2. **Implement a Stateless EJB** using Container-Managed Transactions (CMT).
3. **Use the `EJBContext`** to manually roll back a transaction using `setRollbackOnly()`.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 installed.
* **Jakarta EE Application Server:** WildFly 30+ (or Payara/GlassFish).
* **IDE:** IntelliJ IDEA or Eclipse.

---

## 🏗️ Project Architecture

```
Session_10_Tx_Lab/
├── pom.xml
└── src/main/java/com/bank/tx/
    ├── InsufficientFundsException.java (Checked Application Exception)
    ├── AccountService.java             (Stateless EJB)
    └── TxTestClient.java               (Standalone Client)
```

---

## 📋 STEP 1 — Create the Application Exception

Create a checked exception and annotate it to force the container to roll back the transaction when thrown.

```java
package com.bank.tx;

import jakarta.ejb.ApplicationException;

// By default, ApplicationExceptions do NOT roll back transactions.
// We override this behavior using @ApplicationException(rollback = true)
@ApplicationException(rollback = true)
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
```

---

## 📋 STEP 2 — Implement the Stateless EJB

Create `AccountService.java`. We will simulate transferring funds between two accounts. If a rule is violated, we will demonstrate both throwing an exception and manually marking for rollback.

```java
package com.bank.tx;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateless
public class AccountService {

    @Resource
    private EJBContext context;

    // Uses default CMT propagation (REQUIRED)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transferFunds(String fromAccount, String toAccount, double amount) throws InsufficientFundsException {
        
        System.out.println("Starting transfer of $" + amount + " from " + fromAccount + " to " + toAccount);

        // 1. Debit from account (Simulated)
        System.out.println("Debiting $" + amount + " from " + fromAccount);

        // 2. Business Logic Check
        if (amount > 10000) {
            System.err.println("Amount exceeds limit. Throwing InsufficientFundsException.");
            throw new InsufficientFundsException("Cannot transfer more than $10,000 at once.");
        }

        // 3. Credit to account (Simulated)
        System.out.println("Crediting $" + amount + " to " + toAccount);

        // 4. Programmatic Rollback Check
        if (toAccount.equals("SUSPICIOUS-ACC")) {
            System.err.println("Suspicious account detected. Invoking setRollbackOnly().");
            context.setRollbackOnly();
            return;
        }

        System.out.println("Transfer successful.");
    }
}
```

---

## 📋 STEP 3 — Create the Test Client

*(Note: In a real environment, this EJB would be injected into a servlet or REST endpoint. For testing, assume we use a remote EJB client or simple web invocation).*

```java
package com.bank.tx;

import javax.naming.Context;
import javax.naming.InitialContext;

public class TxTestClient {
    public static void main(String[] args) {
        try {
            Context ctx = new InitialContext();
            AccountService service = (AccountService) ctx.lookup("java:global/Session_10_Tx_Lab/AccountService");

            System.out.println("--- Test 1: Valid Transfer ---");
            service.transferFunds("ACC-100", "ACC-200", 500);
            
            System.out.println("\n--- Test 2: Exception Rollback ---");
            try {
                service.transferFunds("ACC-100", "ACC-200", 15000);
            } catch (InsufficientFundsException e) {
                System.out.println("Caught Exception: " + e.getMessage());
            }

            System.out.println("\n--- Test 3: Programmatic Rollback ---");
            service.transferFunds("ACC-100", "SUSPICIOUS-ACC", 500);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## ✅ Expected Outcomes

* **Test 1:** The transaction completes successfully and commits.
* **Test 2:** The method throws `InsufficientFundsException`. Because of `@ApplicationException(rollback=true)`, the container intercepts the exception and issues a rollback before passing the exception back to the client.
* **Test 3:** The method detects an issue and calls `context.setRollbackOnly()`. The method completes normally (no exception thrown to the client), but the container rolls back the transaction upon method exit.
