# Session 3 — Assignment
## Advanced JNDI and DataSources

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 3)

---

## 📝 Part 1: Conceptual Questions (Written)

Answer the following questions in your own words.

1. **JNDI Namespaces:**
   You are building an application where an EJB needs to look up a configuration setting that is only relevant to its own JAR file (no other modules care about it). Which JNDI namespace should you use and why? (`java:global`, `java:app`, or `java:module`)

2. **Connection Pooling Benefits:**
   Explain in detail why an enterprise banking application like GlobalBank should use a WildFly Connection Pool (via a `DataSource`) rather than standard `DriverManager.getConnection()`. Mention performance and resource management in your answer.

3. **Deployment Descriptors:**
   What is the difference between `<env-entry>` and `<resource-ref>` inside an `application.xml` deployment descriptor? Provide a realistic banking example for when you would use each.

---

## 💻 Part 2: Hands-On Scenario (GlobalBank Project)

In the class task, you used a manual `InitialContext` lookup. Now, you will use modern Jakarta EE annotations to inject a DataSource.

**Scenario:** 
GlobalBank needs a new feature: a system that can quickly check if the database is online. You need to create a simple EJB that injects the database DataSource and runs a simple query.

### Task Instructions:

1. **Configure the DataSource:**
   Ensure you have a PostgreSQL database running and a DataSource configured in WildFly named `java:/jdbc/GlobalBankDB`. *(Refer to Theory section 3.3 for the WildFly CLI commands or Admin Console steps to set this up).*

2. **Create the EJB:**
   Create a new Stateless Session Bean named `DatabaseHealthBean` with a local interface `DatabaseHealthLocal`.

3. **Inject the Resource:**
   Use the `@Resource` annotation to inject the `DataSource` into your bean, avoiding `InitialContext` entirely.

4. **Write the Business Logic:**
   Implement a method `public boolean isDatabaseOnline()`.
   - Get a connection from the injected DataSource.
   - Execute a simple query (e.g., `SELECT 1;`).
   - If it succeeds, return `true`. If an exception occurs, catch it and return `false`.
   - *Hint: Remember to use a try-with-resources block so the connection is returned to the pool!*

### Submit For Grading:
Copy your complete `DatabaseHealthBean.java` code into a text file and submit it along with your answers to Part 1.

---

## 💡 Extra Credit / Advanced Challenge

Modify your `DatabaseHealthBean` from Part 2. Instead of returning a `boolean`, return the **current size of the connection pool**.
*Hint: You cannot get this directly from standard JDBC. You will need to look into WildFly specific JMX beans or statistics APIs, OR write a brief paragraph explaining why the application code shouldn't normally care about the internal size of the server's connection pool.*
