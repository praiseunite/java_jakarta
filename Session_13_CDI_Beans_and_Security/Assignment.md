# Session 13 Assignment: Role-Based Access Control (RBAC)

## 📝 Assignment Overview

This assignment tests your ability to apply method-level authorization to business services using standard Jakarta Security annotations.

Based on the textbook **Try It Yourself** requirement:
> *"Implement a secure banking service where only users with specific roles can perform high-value operations."*

You will create a `FundsTransferService` that enforces strict role-based access control (RBAC).

---

## 🏢 Business Scenario

The bank requires that normal users can check their balances, but only authorized `TELLER` employees can perform standard transfers, and only `MANAGER` employees can authorize international transfers. Legacy sweep operations have been permanently disabled for security reasons.

You must build the backend service that enforces these constraints at the method level.

---

## 📋 Specific Requirements

### 1. Build the Secure Service
Create a CDI managed bean named `FundsTransferService`.

### 2. Apply Security Annotations
Implement the following methods and apply the exact required annotations:

*   **`public double getInterestRate()`**
    *   **Rule:** Anyone can access this method, regardless of login status or roles.
    *   **Action:** Apply `@PermitAll`.
*   **`public void transferFunds(String from, String to, double amount)`**
    *   **Rule:** Only users who possess either the `TELLER` or `MANAGER` role can access this.
    *   **Action:** Apply `@RolesAllowed`.
*   **`public void authorizeInternationalTransfer(String from, String to, double amount)`**
    *   **Rule:** Only users who possess the `MANAGER` role can access this.
    *   **Action:** Apply `@RolesAllowed`.
*   **`public void executeLegacySweep()`**
    *   **Rule:** This method is deprecated and extremely dangerous. Absolutely no one, not even the admin or manager, should be able to execute it.
    *   **Action:** Apply `@DenyAll`.

### 3. Create a Test Client
Create a JSF backing bean or standalone client that attempts to call all four methods.
*(Note: Full execution testing requires a configured IdentityStore. For this assignment, the evaluation focuses heavily on the correct structural application of the security annotations on the service).*

---

## 📂 Expected Directory Structure

```
Session_13_Assignment/
├── pom.xml
└── src/main/java/com/bank/services/
    └── FundsTransferService.java
```

## 💯 Grading Criteria

1. **Class Structure (20%)**: The class is a properly defined CDI managed bean or Stateless EJB.
2. **Permit / Deny Annotations (40%)**: `@PermitAll` and `@DenyAll` are correctly applied to the public/legacy methods.
3. **Role Enforcement (40%)**: `@RolesAllowed` is correctly configured with single and multiple string array arguments.
