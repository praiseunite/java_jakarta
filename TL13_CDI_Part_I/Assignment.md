# Session 11 Assignment: Advanced CDI Injection with Qualifiers

## 📝 Assignment Overview

This assignment evaluates your mastery of Dependency Injection (DI) and how to resolve injection ambiguities when multiple implementations of a single interface exist in the container.

Based on the textbook **Try It Yourself** requirement:
> *"Implement a CDI scenario with multiple bean implementations for a single interface and use CDI Qualifiers to inject the correct instance."*

You will implement an **Authentication Service** where the application supports both "Local" and "LDAP" authentication strategies.

---

## 🏢 Business Scenario

Your application defines a standard `Authenticator` interface. You have built two implementations: `LocalDatabaseAuthenticator` and `LDAPAuthenticator`. 

When a controller attempts to `@Inject Authenticator auth;`, the CDI container crashes on startup with an `AmbiguousResolutionException` because it doesn't know *which* implementation to inject. You must resolve this using **CDI Qualifiers**.

---

## 📋 Specific Requirements

### 1. The Interface and Implementations
* Create an interface `Authenticator` with a method `boolean authenticate(String user, String pass)`.
* Create `LocalDatabaseAuthenticator` (returns true if user=="admin" and pass=="123").
* Create `LDAPAuthenticator` (returns true if user=="ldap_user" and pass=="xyz").
* Both implementations must be annotated as `@Dependent` (the default scope).

### 2. The Custom Qualifiers
* Create a custom CDI Qualifier annotation `@LocalAuth`.
* Create a custom CDI Qualifier annotation `@LDAPAuth`.
* *(Hint: A Qualifier is an annotation marked with `@Qualifier`, `@Retention(RUNTIME)`, and `@Target({FIELD, TYPE, METHOD, PARAMETER})`)*.

### 3. Apply the Qualifiers
* Annotate `LocalDatabaseAuthenticator` with `@LocalAuth`.
* Annotate `LDAPAuthenticator` with `@LDAPAuth`.

### 4. The Controller and Testing
* Create a `LoginController` (e.g., a JSF `@Named @RequestScoped` bean or a standalone test bean).
* Using **Constructor Injection** (or Field Injection), inject *both* authenticators into the controller, applying the custom qualifiers at the injection points.
* Expose a method to test both authenticators and print/return the results.

---

## 📂 Expected Directory Structure

```
Session_11_Assignment/
├── pom.xml
└── src/main/java/com/security/cdi/
    ├── Authenticator.java               (Interface)
    ├── LocalAuth.java                   (Qualifier Annotation)
    ├── LDAPAuth.java                    (Qualifier Annotation)
    ├── LocalDatabaseAuthenticator.java  (Implementation)
    ├── LDAPAuthenticator.java           (Implementation)
    └── LoginController.java             (Injection Point)
```

## 💯 Grading Criteria

1. **Qualifier Definition (30%)**: `@LocalAuth` and `@LDAPAuth` are correctly defined as CDI Qualifiers using the appropriate meta-annotations.
2. **Implementation Mapping (30%)**: The qualifiers are properly applied to the concrete classes.
3. **Injection Resolution (40%)**: The controller successfully injects both implementations without ambiguity errors by explicitly using the qualifiers alongside `@Inject`.
