# TL17 — CDI Beans: Security and Authentication (Session 13, Part 2)

**Manual Reference:** JAKARTA EE – TL17  
**Book Coverage:** Session 13 (Part 2) — Declarative and Programmatic Security, Application Roles, Security Constraints, Login Modules

This session continues from TL16. While TL16 focused on CDI Beans (Qualifiers, Producers, Interceptors, Events), TL17 dives into **Jakarta Security** — how to configure authentication and enforce role-based access control using both declarative and programmatic approaches.

---

## 🎯 Learning Objectives

1. **Describe** the Jakarta EE Declarative and Programmatic Security approaches.
2. **Explain** Security and Configure Authentication using `HttpAuthenticationMechanism` and `IdentityStore`.
3. **Describe** the basics of Application Roles, Security Constraints, and Login Modules.

---

## 1. Declarative vs. Programmatic Security

Jakarta Security supports two approaches to enforcing access control:

| Approach | How It Works | Where Applied |
|---|---|---|
| **Declarative** | Security is expressed through annotations or `web.xml` — no code change to business logic. | `@RolesAllowed`, `@PermitAll`, `@DenyAll`, `<security-constraint>` in `web.xml` |
| **Programmatic** | Business logic code explicitly calls `SecurityContext` APIs to check identity/roles. | `securityContext.isCallerInRole("ADMIN")`, `securityContext.getCallerPrincipal()` |

---

## 2. Application Roles

A **Role** is a named logical grouping of permissions. Roles are defined at the application level and are independent of the concrete user groups stored in the database or LDAP.

### Defining Roles
Roles are declared in `web.xml`:
```xml
<security-role>
    <role-name>TELLER</role-name>
</security-role>
<security-role>
    <role-name>BANK_MANAGER</role-name>
</security-role>
```

### Mapping Roles
In WildFly, roles are mapped from LDAP groups or database rows to application roles in `WEB-INF/jboss-web.xml`:
```xml
<jboss-web>
    <security-domain>my-bank-domain</security-domain>
</jboss-web>
```

---

## 3. Security Constraints in `web.xml`

Declarative security constraints protect URL patterns at the web tier:
```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Manager Area</web-resource-name>
        <url-pattern>/manager/*</url-pattern>
        <http-method>GET</http-method>
        <http-method>POST</http-method>
    </web-resource-collection>
    <auth-constraint>
        <role-name>BANK_MANAGER</role-name>
    </auth-constraint>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

> `CONFIDENTIAL` forces HTTPS. `INTEGRAL` ensures the data hasn't been tampered with. `NONE` means no transport requirement.

---

## 4. Login Modules and Authentication Configuration

### The Three Pillars of Jakarta Security (Review)

| Pillar | Interface / Annotation | Responsibility |
|---|---|---|
| **Authentication Mechanism** | `HttpAuthenticationMechanism` / `@FormAuthenticationMechanismDefinition` | Intercepts HTTP requests and handles credentials |
| **Identity Store** | `IdentityStore` / `@DatabaseIdentityStoreDefinition` | Validates passwords and loads roles from a database/LDAP |
| **Security Context** | `jakarta.security.enterprise.SecurityContext` | Injectable API for querying current principal and roles |

### Configuring Database-Backed Authentication
```java
@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(loginPage = "/login.xhtml", errorPage = "/login-error.xhtml")
)
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:/jdbc/BankDB",
    callerQuery  = "SELECT password_hash FROM users WHERE username = ?",
    groupsQuery  = "SELECT role_name FROM user_roles WHERE username = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class
)
public class SecurityConfig {}
```

---

## 5. Programmatic Security with `SecurityContext`

```java
@Named @RequestScoped
public class VaultController {

    @Inject private SecurityContext securityContext;

    public String openVault() {
        // Programmatic check before executing business logic
        if (!securityContext.isCallerInRole("BANK_MANAGER")) {
            throw new SecurityException("403 Forbidden: Manager access required.");
        }
        return "vault-contents";
    }
    
    public boolean isManager() {
        return securityContext.isCallerInRole("BANK_MANAGER");
    }
    
    public String getCurrentUser() {
        return securityContext.getCallerPrincipal().getName();
    }
}
```

---

## 6. Declarative Method Security (`@RolesAllowed`)

```java
@ApplicationScoped
public class BankingOperationsService {

    @PermitAll
    public double getPublicExchangeRate(String currency) { return 1.15; }

    @RolesAllowed({"TELLER", "BANK_MANAGER"})
    public void transferFunds(double amount, String from, String to) { /* ... */ }

    @RolesAllowed("BANK_MANAGER")
    public void authorizeInternationalTransfer(double amount) { /* ... */ }

    @DenyAll
    public void legacySweepOperation() { /* Disabled permanently */ }
}
```

---

## 7. Summary

* **Declarative Security:** `@RolesAllowed`, `@PermitAll`, `@DenyAll`, `<security-constraint>` in `web.xml` — no code changes to business logic needed.
* **Programmatic Security:** `SecurityContext.isCallerInRole()` and `getCallerPrincipal()` for runtime checks inside beans.
* **Jakarta Security Pillars:** `HttpAuthenticationMechanism` → `IdentityStore` → `SecurityContext`.
* **Login Modules:** Configured via portable `@DatabaseIdentityStoreDefinition` using PBKDF2 password hashing.
* **Transport Guarantee:** `CONFIDENTIAL` in `web.xml` enforces HTTPS.
