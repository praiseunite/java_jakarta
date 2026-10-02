# Session 13: CDI Beans & Jakarta Security

Welcome to **Session 13: CDI Beans & Jakarta Security**. Before Jakarta EE 8, configuring security meant wrestling with proprietary, server-specific XML files and complex JAAS login modules. **Jakarta Security** modernizes this entirely: it provides a portable, CDI-driven, annotation-based security framework with three elegant pillars: **HttpAuthenticationMechanism**, **IdentityStore**, and the injectable **SecurityContext**.

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Explain** the Jakarta Security specification and its integration with CDI.
2. **Differentiate** between Authentication (identifying who you are) and Authorization (determining what you can do).
3. **Configure** portable authentication mechanisms using `@FormAuthenticationMechanismDefinition` and `@CustomFormAuthenticationMechanismDefinition`.
4. **Connect** user repositories declaratively using `@DatabaseIdentityStoreDefinition` or custom `IdentityStore` beans.
5. **Perform** programmatic security checks using the CDI-injectable `SecurityContext`.
6. **Apply** role-based access control (`@RolesAllowed`) on CDI managed beans.

---

## 1. The Three Pillars of Modern Jakarta Security

| Pillar | Core Interface / Annotation | Responsibility |
| :--- | :--- | :--- |
| **1. Authentication Mechanism** (The Front Gate Guard) | `HttpAuthenticationMechanism`<br>`@FormAuthenticationMechanismDefinition` | Intercepts HTTP requests, handles login credentials, redirects to login pages, and initiates challenge protocols. |
| **2. Identity Store** (The Central Database) | `IdentityStore`<br>`@DatabaseIdentityStoreDefinition` | Validates username/password credentials against databases or LDAP directories and loads caller group/role memberships. |
| **3. Security Context** (The Keycard) | `jakarta.security.enterprise.SecurityContext` | CDI-injectable API used by application beans to inspect caller principal, check role memberships, and invoke programmatic authentication. |

---

## 2. Declarative Authentication & Database Identity Stores

In modern Jakarta EE, you can wire a complete database-backed authentication system using pure Java annotations — with zero server-specific XML configuration:

```java
package com.globalbank.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
// ① Configure Custom Form Authentication
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.xhtml",
        errorPage = "/login-error.xhtml"
    )
)
// ② Configure the Database Identity Store
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:/jdbc/GlobalBankDB",
    callerQuery = "SELECT password_hash FROM bank_users WHERE username = ?",
    groupsQuery = "SELECT role_name FROM user_roles WHERE username = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class // Secure salted PBKDF2 hashing
)
public class ApplicationSecurityConfig {
    // Declarative configuration class — no method bodies needed!
}
```

---

## 3. Inspecting Credentials with `SecurityContext`

Any CDI bean, EJB, or JSF Backing Bean can inject `SecurityContext` to inspect user roles and identities dynamically:

```java
package com.globalbank.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import java.security.Principal;

@Named("dashboard")
@RequestScoped
public class AccountDashboardBean {

    // ① Inject the portable Jakarta SecurityContext
    @Inject
    private SecurityContext securityContext;

    public String getCurrentUser() {
        Principal caller = securityContext.getCallerPrincipal();
        return (caller != null) ? caller.getName() : "Anonymous Guest";
    }

    public boolean isManager() {
        // ② Verify if user possesses high-privilege role
        return securityContext.isCallerInRole("BANK_MANAGER");
    }

    public String accessVault() {
        if (!isManager()) {
            throw new SecurityException("Unauthorized: Access to cash vault is restricted to Managers!");
        }
        return "vault-opened";
    }
}
```

---

## 4. Declarative Role Enforcement (`@RolesAllowed`)

Jakarta Security extends standard authorization annotations (`@RolesAllowed`, `@PermitAll`, `@DenyAll`) from EJBs to all CDI-managed beans:

```java
package com.globalbank.services;

import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.DenyAll;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WireTransferService {

    @PermitAll
    public double getExchangeRate(String currency) {
        return 1.15; // Public data accessible to all visitors
    }

    @RolesAllowed({"TELLER", "BANK_MANAGER"})
    public void executeTransfer(int from, int to, double amount) {
        // Only authenticated tellers and managers can initiate wire transfers
    }

    @RolesAllowed("BANK_MANAGER")
    public void authorizeForeignOverdraft(int accountId, double amount) {
        // High-level managerial authorization only
    }

    @DenyAll
    public void deprecatedLegacySweep() {
        // Permanently disabled
    }
}
```

---

## 5. Summary

* **Jakarta Security:** Modern, portable, CDI-driven enterprise security replacing proprietary server XML.
* **Auth Mechanism:** `HttpAuthenticationMechanism` handles incoming HTTP login challenges and redirects.
* **`IdentityStore`:** Validates passwords and loads role groups from relational databases or LDAP.
* **`SecurityContext`:** CDI-injectable facade providing `getCallerPrincipal()` and `isCallerInRole()`.
* **RBAC Annotations:** `@RolesAllowed`, `@PermitAll`, and `@DenyAll` enforce method permissions declaratively.
