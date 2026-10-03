# Self-Study Block S5–S8: Assignments — Book Sessions 9 to 14

**Manual Reference:** S5–S8 (Self-study Session — over and above module duration)  
**Covers:** All OnlineVarsity Work Assignments for Book Sessions 9 through 14  
**Schedule:** Conducted around Week 5, following TL20

This self-study block is dedicated to independently completing and submitting all assigned hands-on lab work from Book Sessions 9 through 14. These assignments solidify mastery of Jakarta EE Bean Validation, Transactions, CDI architecture, Security, and Packaging.

---

## 📋 Assignment Checklist — Sessions 9 to 14

### ✅ Session 9 — Jakarta Bean Validation
- [ ] **Assignment:** Build a `PatientRegistration` model class annotated with standard constraints: `@NotNull`, `@Size(min = 3, max = 50)`, `@Email`, `@Past`, and `@Pattern` for phone numbers. Implement a custom constraint `@ValidMedicalRecordNumber` with its corresponding `ConstraintValidator`.
- [ ] **Lab:** Create a standalone test driver using `Validation.buildDefaultValidatorFactory()` that validates invalid patient objects and outputs all violated property paths and constraint messages.

### ✅ Session 10 — Enterprise Beans Transactions and JNDI
- [ ] **Assignment:** Implement a `@Stateless` `FundTransferService` with Bean-Managed Transactions (`@TransactionManagement(TransactionManagementType.BEAN)`). Use `UserTransaction` to coordinate debit and credit operations across two accounts, explicitly invoking `utx.commit()` on success and `utx.rollback()` when encountering insufficient funds or network exceptions.
- [ ] **Lab:** Perform dynamic JNDI lookups using `InitialContext` with portable global JNDI names (`java:global/appName/moduleName/BeanName!interfaceName`) and inspect environment entries configured in `ejb-jar.xml`.

### ✅ Session 11 — Contexts and Dependency Injection (Part I)
- [ ] **Assignment:** Create a CDI-driven report generation subsystem. Implement beans across multiple scopes: `@RequestScoped`, `@SessionScoped`, and `@ApplicationScoped`. Demonstrate how state is isolated or shared across concurrent simulated HTTP sessions.
- [ ] **Lab:** Implement `@PostConstruct` and `@PreDestroy` lifecycle callback methods on CDI beans to verify container initialization and teardown logging.

### ✅ Session 12 — Contexts and Dependency Injection (Part II)
- [ ] **Assignment:** Create custom qualifiers (`@CreditCard`, `@PayPal`, `@Crypto`) and bind them to different implementations of a `PaymentProcessor` interface. Inject them dynamically using `@Inject @Any Instance<PaymentProcessor>` to select the appropriate payment provider at runtime.
- [ ] **Lab:** Build a custom CDI interceptor (`@Audited` and `AuditInterceptor`) using `@AroundInvoke` to intercept sensitive business methods and log invocation execution duration, caller credentials, and method parameters.

### ✅ Session 13 — CDI Beans: Advanced Features & Security
- [ ] **Assignment:** Implement a decoupled event-driven architecture using CDI Events:
  - Define an `OrderPlacedEvent` POJO.
  - Fire the event using `Event<OrderPlacedEvent>.fire()`.
  - Create asynchronous event observers using `@ObservesAsync` to trigger inventory allocation, email dispatch, and invoicing concurrently.
- [ ] **Lab:** Implement declarative Role-Based Access Control (RBAC) using `@RolesAllowed({"ADMIN", "MANAGER"})` and `@PermitAll` on CDI beans and Servlets. Verify access denial (`403 Forbidden`) when accessing protected resources with an unauthorized principal.

### ✅ Session 14 — Packaging of Enterprise Beans and Entities
- [ ] **Assignment:** Construct a complete multi-module Maven enterprise project structure:
  - `ecommerce-ejb`: Contains stateless EJBs and JPA entities (`persistence.xml`).
  - `ecommerce-web`: Contains JSF Facelets templates, servlets, and CDI beans (`beans.xml`, `web.xml`).
  - `ecommerce-ear`: Packages the EJB JAR and WAR into a single enterprise archive (`application.xml`).
- [ ] **Lab:** Package the application as a modern Skinny WAR (containing EJBs, JPA entities, and web views in `WEB-INF/classes`) and deploy it to WildFly. Confirm that JNDI lookups, JPA entity manager injection, and web endpoints function identically.

---

## 📌 Submission & Assessment Guidelines

- Ensure all Maven sub-modules compile cleanly with `mvn clean package`.
- Applications must deploy to WildFly 30+ without deployment errors or unresolved CDI dependencies.
- Submit all source code along with a comprehensive summary report demonstrating screenshot evidence of execution and log outputs to the OnlineVarsity Work Assignments portal before the final evaluation date.
