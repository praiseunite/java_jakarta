# Self-Study Block S1–S4: Assignments — Book Sessions 1 to 8

**Manual Reference:** S1–S4 (Self-study Session — over and above module duration)  
**Covers:** All OnlineVarsity Work Assignments for Book Sessions 1 through 8  
**Schedule:** Conducted around Week 3, after TL10

This self-study block is dedicated to independently completing and submitting all assigned hands-on lab work from the first eight book sessions. These assignments reinforce the concepts taught in TL01 through TL10.

---

## 📋 Assignment Checklist — Sessions 1 to 8

### ✅ Session 1 — Introduction to Jakarta Enterprise Beans
- [ ] **Assignment:** Research and write a comparison between traditional Java SE applications and Jakarta EE applications. Focus on: scalability, transaction management, and resource pooling.
- [ ] **Lab:** Set up WildFly 30+ and deploy the provided sample EJB application. Confirm the JNDI bindings appear in the server log.

### ✅ Session 2 — Session Beans and their Types
- [ ] **Assignment:** Build a demonstration application containing one `@Stateless`, one `@Stateful`, and one `@Singleton` bean. From a test client, call all three. Document the lifecycle differences observed.
- [ ] **Lab:** Demonstrate the `@Singleton` counter pattern. Increment from two concurrent clients and show that state is shared.

### ✅ Session 3 — Resource Creation and JNDI
- [ ] **Assignment:** Create a PostgreSQL `DataSource` in WildFly Admin Console. Inject it using `@Resource`. Write an EJB that reads a row from a table and returns it as a string.
- [ ] **Lab:** Use the WildFly CLI to list all registered JNDI names in the server: `ls java:`.

### ✅ Session 4 — Working with Jakarta Enterprise Beans
- [ ] **Assignment:** Build a `@Stateless` `PaymentService` EJB using **Container-Managed Transactions**. Test that an exception causes a full rollback using a two-phase operation (debit + credit). Simulate failure after debit.
- [ ] **Lab:** Add a security annotation (`@RolesAllowed`) to one method and test it with and without the correct role.

### ✅ Session 5 — Facelets in Jakarta EE
- [ ] **Assignment:** Build a student grade calculator JSF application using a **Facelets master template** with header, navigation, and footer. The page must use one composite component (e.g., a reusable `inputField` with label + validation error display).
- [ ] **Lab:** Add a PhaseListener and print each phase name to the console as the form processes a submission.

### ✅ Session 6 — Jakarta Local and Remote Clients
- [ ] **Assignment:** Create an EJB (`InventoryService`) with both `@Local` and `@Remote` interfaces. Demonstrate local access from a JSF bean and remote access from a standalone Java SE `main()` class using JNDI lookup.
- [ ] **Lab:** Pass a `Serializable` POJO as parameter to the remote call. Print it on both sides.

### ✅ Session 7 — Jakarta Messaging Services
- [ ] **Assignment:** Build a JMS Point-to-Point order submission system. A JSF form collects order details, a producer sends an `ObjectMessage` to a queue, and an MDB processes and prints the order details.
- [ ] **Lab:** Switch from a Queue to a Topic. Subscribe two MDB classes to the same topic. Send one message and verify both MDBs process it.

### ✅ Session 8 — Jakarta Connectors Architecture
- [ ] **Assignment:** Configure a WildFly JDBC DataSource using the Admin Console. Write a DAO class that uses the pooled `DataSource` to perform CRUD operations on a `Product` table. Demonstrate that connections are returned to the pool after each operation.
- [ ] **Lab:** Draw the JCA three-layer architecture (Application Layer, Resource Adapter Layer, EIS Layer) and annotate it with the three system contracts.

---

## 📌 Submission Guidelines

- All code must compile and deploy to WildFly 30+ without errors.
- Each submission must include a short write-up (max 200 words) explaining what was observed, especially for lifecycle and transaction tests.
- Submit via OnlineVarsity Work Assignments portal before the Week 3 deadline.
