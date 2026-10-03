# TL20 Class Task: End-to-End Application — CDI + Security + Packaging

## 📌 Task Overview

This final class task integrates everything learned across Sessions 11–14 into one cohesive mini-application. You will build a **secured loan application portal** using CDI, Jakarta Security, and package it as a Skinny WAR.

---

## 🏗️ Application Summary

| Component | Technology |
|---|---|
| Web UI | JSF + Facelets |
| Dependency Wiring | CDI (`@ApplicationScoped`, `@SessionScoped`, `@Named`) |
| Business Logic | `@Stateless` EJB (inside the WAR) |
| Security | `@DatabaseIdentityStoreDefinition` + `@RolesAllowed` |
| Packaging | Single Skinny WAR |

---

## 📋 Components to Build

### 1. Security Config (`@ApplicationScoped`)
Configure form-based authentication with an in-memory identity store with two test users:
- `loan_officer` / `pass1` → role `LOAN_OFFICER`
- `manager` / `pass2` → roles `LOAN_OFFICER`, `SENIOR_MANAGER`

### 2. Loan Calculator EJB (`@Stateless`)
```java
@RolesAllowed("LOAN_OFFICER")
public double calculateRepayment(double principal, double rate, int months) {
    double r = rate / 12 / 100;
    return principal * r / (1 - Math.pow(1 + r, -months));
}
```

### 3. CDI Session Cart (`@SessionScoped`)
Hold the user's current loan application state (principal, rate, months).

### 4. JSF Controller (`@Named @RequestScoped`)
Inject both the `@SessionScoped` state and the `@Stateless` EJB. Render result on the page.

### 5. Package
Build as a single Skinny WAR, deploy to WildFly via `.dodeploy`.

---

## ✅ Verification

1. Access the app — redirected to `/login.xhtml` ✅
2. Login as `loan_officer` — can access calculator ✅
3. Navigate back (session state preserved by `@SessionScoped` bean) ✅
4. Attempt any `@RolesAllowed("SENIOR_MANAGER")` endpoint as `loan_officer` → 403 ✅
5. Login as `manager` — all access granted ✅
