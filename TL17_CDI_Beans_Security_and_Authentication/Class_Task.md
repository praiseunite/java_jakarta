# TL17 Class Task: Configuring Jakarta Security with RBAC

## 📌 Task Overview

Configure a complete Jakarta Security setup for a banking web application using a declarative `@DatabaseIdentityStoreDefinition` and enforce role-based access at both the URL pattern level (`web.xml`) and the method level (`@RolesAllowed`).

---

## 📋 STEP 1 — Central Security Config Bean

```java
package com.bank.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.xhtml",
        errorPage = "/login-error.xhtml"
    )
)
@DatabaseIdentityStoreDefinition(
    dataSourceLookup = "java:/jdbc/BankDS",
    callerQuery  = "SELECT password_hash FROM bank_users WHERE username = ?",
    groupsQuery  = "SELECT role_name FROM user_roles WHERE username = ?",
    hashAlgorithm = Pbkdf2PasswordHash.class
)
public class BankSecurityConfig {}
```

---

## 📋 STEP 2 — Protected Business Service

```java
package com.bank.service;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WireTransferService {

    @PermitAll
    public String getStatus() { return "OPERATIONAL"; }

    @RolesAllowed({"TELLER", "BANK_MANAGER"})
    public void executeTransfer(double amount) {
        System.out.println("Transferring: $" + amount);
    }

    @RolesAllowed("BANK_MANAGER")
    public void approveInternational(double amount) {
        System.out.println("International approved: $" + amount);
    }

    @DenyAll
    public void legacyFlush() {
        System.out.println("THIS SHOULD NEVER RUN");
    }
}
```

---

## 📋 STEP 3 — URL-Level Security in `web.xml`

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Teller Area</web-resource-name>
        <url-pattern>/teller/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>TELLER</role-name>
        <role-name>BANK_MANAGER</role-name>
    </auth-constraint>
</security-constraint>

<security-constraint>
    <web-resource-collection>
        <web-resource-name>Manager Only Area</web-resource-name>
        <url-pattern>/manager/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>BANK_MANAGER</role-name>
    </auth-constraint>
</security-constraint>
```

---

## ✅ Verification

1. Login as a `TELLER` → can access `/teller/*` but NOT `/manager/*` (403).
2. Login as a `BANK_MANAGER` → can access both URL patterns.
3. Any caller attempting `legacyFlush()` gets a security exception.
