# Session 13 Class Task: Declarative Security Configuration

## 📌 Task Overview

In this hands-on lab, you will configure Jakarta Security for a new enterprise web application. You will define the authentication mechanism and the identity store entirely through Java annotations.

You will:
1. **Configure a Custom Form Authentication Mechanism.**
2. **Configure an embedded database Identity Store** to map users to roles.
3. **Use the `SecurityContext`** inside a JSF bean to programmatically verify roles.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17.
* **Jakarta EE Application Server:** WildFly 30+ (or Payara/GlassFish).
* **IDE:** IntelliJ IDEA or Eclipse.

---

## 🏗️ Project Architecture

```
Session_13_Security_Lab/
├── pom.xml
└── src/main/
    ├── java/com/security/app/
    │   ├── AppSecurityConfig.java     (Declarative Security Settings)
    │   └── UserDashboardController.java(Protected JSF Controller)
    └── webapp/
        ├── login.xhtml                (Custom login form)
        ├── login-error.xhtml          (Error page)
        └── WEB-INF/
            └── web.xml                (Declare security constraints)
```

---

## 📋 STEP 1 — Define the Security Configuration

Create a centralized configuration class. We will use the `@CustomFormAuthenticationMechanismDefinition` to define our login and error pages. We will also use a built-in memory identity store for testing (in a real app, you would use `@DatabaseIdentityStoreDefinition`).

```java
package com.security.app;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.InMemoryIdentityStoreDefinition;

@ApplicationScoped
@CustomFormAuthenticationMechanismDefinition(
    loginToContinue = @LoginToContinue(
        loginPage = "/login.xhtml",
        errorPage = "/login-error.xhtml"
    )
)
@InMemoryIdentityStoreDefinition(
    validCredentials = {
        @InMemoryIdentityStoreDefinition.Credentials(name = "admin", password = "123", groups = {"ADMIN", "USER"}),
        @InMemoryIdentityStoreDefinition.Credentials(name = "guest", password = "abc", groups = {"USER"})
    }
)
public class AppSecurityConfig {
    // No code needed - purely declarative!
}
```

---

## 📋 STEP 2 — Create the Protected Controller

Use the `SecurityContext` to conditionally render information based on the authenticated user's role.

```java
package com.security.app;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

@Named
@RequestScoped
public class UserDashboardController {

    @Inject
    private SecurityContext securityContext;

    public String getUsername() {
        if (securityContext.getCallerPrincipal() != null) {
            return securityContext.getCallerPrincipal().getName();
        }
        return "Not Logged In";
    }

    public boolean isAdmin() {
        return securityContext.isCallerInRole("ADMIN");
    }

    public String getSecretData() {
        if (isAdmin()) {
            return "TOP SECRET SERVER METRICS: CPU 45%, RAM 80%";
        }
        return "Access Denied. You must be an ADMIN to see this.";
    }
}
```

---

## 📋 STEP 3 — Configure `web.xml`

Even though authentication and identity stores are defined in Java, you still need to tell the server *which URLs* require authentication.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee 
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <security-constraint>
        <web-resource-collection>
            <web-resource-name>Protected Area</web-resource-name>
            <url-pattern>/secured/*</url-pattern>
        </web-resource-collection>
        <auth-constraint>
            <role-name>USER</role-name>
            <role-name>ADMIN</role-name>
        </auth-constraint>
    </security-constraint>

    <security-role>
        <role-name>USER</role-name>
    </security-role>
    <security-role>
        <role-name>ADMIN</role-name>
    </security-role>

</web-app>
```

---

## ✅ Verification

1. Attempt to access `/secured/dashboard.xhtml`. You should be redirected to `/login.xhtml`.
2. Login as `guest` / `abc`. The dashboard should display "Access Denied" for the secret data.
3. Login as `admin` / `123`. The dashboard should display the TOP SECRET SERVER METRICS.
