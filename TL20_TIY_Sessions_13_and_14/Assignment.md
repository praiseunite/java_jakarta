# TL20 Assignment: Full Capstone — Secure Enterprise Application

## 📝 Assignment Overview

This is the **final capstone assignment** for the Jakarta EE course. It integrates all 14 book sessions into a single working application.

## 🏢 Business Scenario

Build a **Bank Customer Self-Service Portal** — a Skinny WAR application with the following features:

## 📋 Requirements

### Security (Sessions 13–14)
- Configure form-based login via `@CustomFormAuthenticationMechanismDefinition`
- Two roles: `CUSTOMER` and `BRANCH_ADMIN`
- Use `@DatabaseIdentityStoreDefinition` (or in-memory for testing)

### Business Tier — EJBs inside WAR (Session 1, 2, 14)
- `AccountService` (`@Stateless`) — `getBalance()`, `getTransactions()`
- `TransferService` (`@Stateless`, `@RolesAllowed("CUSTOMER")`) — `transfer(from, to, amount)` with CMT `REQUIRED`
- `AdminReportService` (`@Stateless`, `@RolesAllowed("BRANCH_ADMIN")`) — `generateDailyReport()`

### Validation (Session 9)
- Apply `@NotNull`, `@Positive`, `@Size` to the transfer request object
- Show validation error messages on the JSF form

### CDI Wiring (Sessions 11–13)
- `@SessionScoped UserSession` holds the logged-in account number
- `@RequestScoped TransferController` — `@Named` JSF backing bean injecting both EJBs and `UserSession`
- A `@Produces` method provides a `Logger` instance (demonstrating CDI Producers)

### Web UI (Session 5)
- Facelets master template with header/footer
- Two pages: `dashboard.xhtml` and `transfer.xhtml`

### Packaging (Session 14)
- Build as a single Skinny WAR named `bank-portal`
- Set context root to `/bank` via `jboss-web.xml`
- Deploy to WildFly

## 💯 Grading

| Area | Marks |
|---|---|
| Security fully configured and login working | 20% |
| `@RolesAllowed` correctly gates Admin vs Customer methods | 15% |
| Bean Validation on transfer form | 15% |
| CDI scopes correctly applied (Session, Request) | 15% |
| `@Produces` Logger factory present | 10% |
| Facelets template used for all pages | 10% |
| Skinny WAR deploys and runs on WildFly | 15% |
