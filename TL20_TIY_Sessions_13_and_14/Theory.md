# TL20 — Try It Yourself: Review of Sessions 13 & 14

**Manual Reference:** JAKARTA EE – TL20  
**Book Coverage:** Session 13 (CDI Beans — all parts) + Session 14 (Packaging — all parts)

This is the final TIY review session of the course. Use it to consolidate all CDI Beans knowledge (TL13–TL17) and all Packaging knowledge (TL18–TL19), and complete any outstanding exercises from the textbook.

---

## 📚 Recap — Session 13: CDI Beans (Complete Review)

### Part 1 (TL16): Advanced CDI Bean Features
| Concept | Key Point |
|---|---|
| **Qualifiers** | Custom annotations (`@Qualifier`) that disambiguate multiple bean implementations. Use `@Inject @MyQualifier` at injection point. |
| **Producers** | `@Produces` turns a method into a CDI factory for objects that can't be instantiated normally. Paired with `@Disposes` for cleanup. |
| **Interceptors** | AOP-style cross-cutting wrappers. Require: binding annotation (`@InterceptorBinding`) + interceptor class (`@Interceptor`) + `@AroundInvoke`. |
| **Events** | In-process pub/sub: `Event<T>.fire()` to publish; `@Observes` parameter in any method to subscribe. `fireAsync()` for async. |
| **Stereotypes** | Meta-annotations bundling multiple CDI annotations into one (e.g., `@Named + @RequestScoped` combined into `@WebController`). |

### Part 2 (TL17): Security
| Concept | Key Point |
|---|---|
| **Declarative** | `@RolesAllowed`, `@PermitAll`, `@DenyAll` on methods; `<security-constraint>` in `web.xml` for URLs. |
| **Programmatic** | `securityContext.isCallerInRole("ROLE")` and `getCallerPrincipal()` for runtime checks inside code. |
| **Jakarta Security Pillars** | `HttpAuthenticationMechanism` (intercepts login) → `IdentityStore` (validates password + loads roles) → `SecurityContext` (carries identity). |
| **Login Config** | `@DatabaseIdentityStoreDefinition` with `callerQuery`, `groupsQuery`, and `hashAlgorithm`. |

---

## 📚 Recap — Session 14: Packaging (Complete Review)

### Archive Types Summary

| Archive | Contains | Use When |
|---|---|---|
| `.jar` | EJB classes, JPA entities, `META-INF/beans.xml` | Shared business module |
| `.war` | Servlets, JSF pages, `WEB-INF/`, Skinny-WAR EJBs | Web tier or full Skinny WAR app |
| `.rar` | JCA connector, `META-INF/ra.xml` | Enterprise system adapter (SAP, CICS) |
| `.ear` | WARs + JARs + `META-INF/application.xml` | Multi-module enterprise application |

### EAR `application.xml` Structure
```xml
<application version="10">
    <module><web><web-uri>portal.war</web-uri><context-root>/app</context-root></web></module>
    <module><ejb>biz.jar</ejb></module>
    <library-directory>lib</library-directory>
</application>
```

### WildFly Deployment Markers

| Marker | Meaning |
|---|---|
| `.dodeploy` | Trigger deployment |
| `.isdeploying` | In progress |
| `.deployed` | ✅ Success |
| `.failed` | ❌ Error (check contents for stack trace) |
| `.undeployed` | Stopped |

---

## 🔁 TIY Exercise Checklist

- [ ] **Session 13 TIY:** Create a `@Stereotype` annotation `@WebController` that bundles `@Named + @RequestScoped`. Apply it to a JSF backing bean.
- [ ] **Session 13 TIY:** Implement a full `@DatabaseIdentityStoreDefinition` connecting to a real database. Verify login with two users of different roles.
- [ ] **Session 14 TIY:** Build a multi-module EAR with one WAR (customer portal) and one EJB JAR. Deploy to WildFly. Verify via context root.
- [ ] **Session 14 TIY:** Rebuild the same app as a Skinny WAR. Verify identical behavior with much simpler structure.

---

## ❓ Final Self-Test Questions (Course Recap)

1. What are the three pillars of Jakarta Security?
2. When would you use `REQUIRES_NEW` transaction propagation?
3. What does `@Disposes` do and when is it invoked?
4. What is the difference between a WAR and an EAR context root?
5. Why is a `@SessionScoped` bean required to be `Serializable`?
6. What happens when a system exception (unchecked) escapes a CMT EJB method?
7. What file inside a `.rar` defines the JCA Resource Adapter capabilities?
8. In CDI, what annotation turns a method into a factory for injectable objects?
