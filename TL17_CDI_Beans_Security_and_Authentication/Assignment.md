# TL17 Assignment: Programmatic + Declarative Security Hybrid

## 📝 Assignment Overview

Build a **Loan Approval System** that uses both declarative annotations AND programmatic `SecurityContext` checks to gate high-value approvals.

## 🏢 Business Scenario

Standard loan approvals (under $10,000) can be done by any `LOAN_OFFICER`. Loans above $10,000 require a `SENIOR_MANAGER` programmatic check inside the business logic itself, even if the method is already role-restricted.

## 📋 Requirements

1. **Configure Jakarta Security** with `@CustomFormAuthenticationMechanismDefinition` and `@DatabaseIdentityStoreDefinition` (or `@InMemoryIdentityStoreDefinition` for testing with 2 users: one `LOAN_OFFICER`, one `SENIOR_MANAGER`).

2. **`LoanApprovalService` (`@ApplicationScoped`):**
   - `@RolesAllowed({"LOAN_OFFICER", "SENIOR_MANAGER"}) public void approveLoan(double amount)`
   - Inside the method, if `amount > 10000` AND `!securityContext.isCallerInRole("SENIOR_MANAGER")`, throw `SecurityException("High-value loans require Senior Manager approval")`.

3. **The UI:** A JSF form with an amount field and submit button. Display the outcome (approved or denied with reason).

4. **Test scenarios:**
   - `LOAN_OFFICER` approves $5,000 → ✅ Approved
   - `LOAN_OFFICER` attempts $15,000 → ❌ Denied (not a Senior Manager)
   - `SENIOR_MANAGER` approves $15,000 → ✅ Approved

## 💯 Grading

| Area | Marks |
|---|---|
| Security configuration (IdentityStore + AuthMechanism) | 25% |
| `@RolesAllowed` on the method | 25% |
| Programmatic `isCallerInRole` check inside the method | 35% |
| UI correctly displays approval/denial outcome | 15% |
