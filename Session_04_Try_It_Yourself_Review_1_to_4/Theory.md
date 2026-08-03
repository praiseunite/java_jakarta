# Session 5 — Review and Consolidation (Sessions 1-4)
## Theory Guide: Integrating the Try It Yourself (TIY) Exercises

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 5)
**Prerequisites:** Completion of Sessions 1, 2, 3, and 4.

---

## Learning Objectives

By the end of this review session, you will be able to:
- ✅ Connect the isolated concepts from the first four sessions into a single, cohesive architecture.
- ✅ Understand how a Web Client, a Stateful Session Bean, a Stateless Session Bean, and a Database interact.
- ✅ Debug common integration issues (JNDI Name Not Found, Transaction Rollbacks, EJB Access Exceptions).

---

## 5.1 — The Big Picture: How Everything Connects

Over the past four sessions, you have completed several "Try It Yourself" (TIY) exercises. Up until now, they have been isolated pieces of code. 

- **Session 1 & 2 TIY:** You created basic Stateless and Stateful Session Beans.
- **Session 3 TIY:** You used JNDI to look up an EJB and you configured a DataSource.
- **Session 4 TIY:** You applied `@TransactionAttribute` and tested transaction rollbacks.

In the real world, these do not exist in isolation. They form a **layered architecture**.

![Integrated Architecture](/Users/mac/.gemini/antigravity/brain/04f4ab8f-3331-438d-995a-883844318d5f/integrated_architecture_1_to_4_1785735967674.png)

### The Integrated Flow

1. **The Client (Web Servlet/JSF)**: The user clicks a button on a web page. The Servlet needs to process this.
2. **The Stateful Bean (User Session)**: The Servlet looks up a Stateful Session Bean (like a Shopping Cart or a User Session) using **JNDI (Session 3)**. This bean remembers who the user is.
3. **The Stateless Bean (Business Logic)**: When it's time to save data or process a payment, the Stateful bean calls a Stateless bean (like `TransactionBean`). 
4. **Security & Transactions (Session 4)**: The Stateless bean checks if the user has the right permissions (`@RolesAllowed`) and ensures the database operation happens completely or not at all (`@TransactionAttribute(REQUIRED)`).
5. **The Database (Session 3)**: The Stateless bean gets a database connection from the Connection Pool (`@Resource DataSource`) and saves the data.

---

## 5.2 — Debugging Integration Issues (The Developer's Toolkit)

When you combine multiple EJBs, transactions, and security, things will inevitably break. This is completely normal. Here is how you debug the most common errors you will encounter in the integrated TIY tasks.

### 1. The "NameNotFoundException" (JNDI Error)
*What it looks like:* `javax.naming.NameNotFoundException: GlobalBankApp/UserBean -- service jboss.naming.context...`
*What it means:* Your Servlet or EJB is asking for a bean, but WildFly cannot find it in the directory.
*How to fix it:*
1. Look at the WildFly console log immediately after you deploy your code. 
2. Search for the word `java:global`. WildFly prints the EXACT JNDI name it generated for your bean.
3. Copy that exact string into your `@Resource(lookup="...")` or `context.lookup("...")` code.

### 2. The "EJBTransactionRolledbackException"
*What it looks like:* `jakarta.ejb.EJBTransactionRolledbackException: Transaction rolled back`
*What it means:* An exception was thrown inside your business logic, so the EJB Container automatically triggered a rollback (Session 4 concept). 
*How to fix it:*
1. The rollback itself is not the real error! Look further down the stack trace for the **"Caused by:"** line.
2. You will usually find a `NullPointerException` or a `SQLException` (e.g., bad SQL syntax) that actually caused the transaction to fail. Fix *that* error.

### 3. The "EJBAccessException" (Security Error)
*What it looks like:* `jakarta.ejb.EJBAccessException: Invocation on method is not allowed`
*What it means:* You applied `@RolesAllowed` (Session 4) to an EJB method, but the current user (or caller) does not have that role, or they are not logged in at all.
*How to fix it:*
Ensure you have configured the security domain in WildFly properly and that your web application is actually forcing the user to log in before calling the EJB.

---

## 5.3 — Review of the TIY Code Snippets

Let's look at the core code pieces you will need to combine in this session's Class Task.

### The Stateful EJB (Session 2 Review)
*Used to hold conversation state with the client.*
```java
@Stateful
public class UserSessionBean {
    private String username;
    
    public void login(String user) { this.username = user; }
    public String getUsername() { return username; }
}
```

### The Transactional & Secure EJB (Session 4 Review)
*Used to perform the actual database work.*
```java
@Stateless
@RolesAllowed("CUSTOMER") // Security
public class PaymentBean {

    // JNDI DataSource injection (Session 3)
    @Resource(lookup = "java:/jdbc/GlobalBankDB")
    private DataSource ds;

    // Transaction Management (Session 4)
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void processPayment(double amount) {
        // Database logic here...
    }
}
```

### The JNDI Client (Session 3 Review)
*Used by the web layer to find the EJBs.*
```java
InitialContext ctx = new InitialContext();
UserSessionBean userSession = (UserSessionBean) ctx.lookup("java:global/App/UserSessionBean");
```

---

## 5.4 — Summary Checklist Before Proceeding

Before starting the integrated Class Task, ask yourself:
1. Is my WildFly server running?
2. Is my PostgreSQL database running?
3. Did I create the `GlobalBankDB` DataSource in the WildFly Admin Console?
4. Do I understand the difference between `@Stateless` and `@Stateful`?

If you answered yes to all four, proceed to the Class Task to begin building Milestone 1!

---

## 📺 Recommended Video Resources
To reinforce the concepts covered in these review sessions, consider searching for the following video tutorials on YouTube:

1. **Channel:** Programming Techie  
   **Video Title:** *Building a complete Java EE application from scratch*
2. **Channel:** Daily Code Buffer  
   **Video Title:** *Enterprise Java Beans (EJB) Crash Course*
