# Session 4 — Assignment
## Transactions, Concurrency, and Security

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 4)

---

## 📝 Part 1: Conceptual Questions (Written)

Answer the following questions in your own words, using examples related to the GlobalBank project where applicable.

1. **ACID Properties:**
   A customer tries to withdraw $200 from an ATM. The ATM dispenses the cash, but before the bank's database can update the customer's balance, the ATM's network connection drops. Which specific ACID property guarantees that the bank's system won't end up in an inconsistent state (where cash was given but the balance wasn't updated)? Explain how the system enforces this.

2. **Concurrency Locking:**
   What is the "lost update" problem? Explain the difference between **Optimistic Locking** and **Pessimistic Locking** in resolving this issue. Which strategy is generally recommended for a high-traffic banking web application and why?

3. **Singleton Bean Concurrency:**
   You have a `@Singleton` bean holding system-wide configuration. You annotate the `getConfig()` method with `@Lock(LockType.READ)` and `updateConfig()` with `@Lock(LockType.WRITE)`.
   Explain exactly what happens if Thread A is executing `updateConfig()` while Thread B and Thread C try to call `getConfig()` at the exact same moment.

4. **Security Layers:**
   Explain the difference between **Transport Layer Security (HTTPS)** and **Application Layer Security (`@RolesAllowed`)**. Why does a secure banking application need *both*?

---

## 💻 Part 2: Code Analysis & Fix

Below is a snippet of code written by a junior developer for the GlobalBank application. It contains a critical security flaw and a transaction bug.

**The Code:**
```java
@Stateless
public class BankManagerService {

    @Inject
    private AccountDAO accountDAO;

    // Used by admins to delete closed accounts
    @PermitAll
    public void deleteAccount(int accountId) {
        accountDAO.remove(accountId);
    }
    
    // Generates a report that takes 45 minutes to run
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void generateMonthlyReport() {
        ReportEngine.runHeavyReport();
    }
}
```

### Your Task:
1. Identify the **two** major mistakes in the code above based on Session 4 principles.
2. Rewrite the code to fix both issues.
   - *Hint 1 for the fix:* Who should be allowed to delete accounts?
   - *Hint 2 for the fix:* Should a 45-minute process hold open a database transaction?

### Submit For Grading:
Submit your answers for Part 1 and your rewritten code for Part 2 in a single document.
