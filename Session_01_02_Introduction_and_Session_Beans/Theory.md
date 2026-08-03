# Session 1 & 2 — Theory
## Introduction to Jakarta Enterprise Beans + Session Beans and Their Types

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1
**Covers:** Textbook Sessions 1 and 2

---

## Learning Objectives

By the end of this session, you will be able to:

1. ✅ Define Jakarta Enterprise Beans (EJB) and explain what it is used for
2. ✅ Describe what an Enterprise Bean is and its essential characteristics
3. ✅ Explain why enterprise beans are necessary — the problems they solve
4. ✅ Describe the three types of Session Beans: Stateful, Stateless, and Singleton
5. ✅ Distinguish between Stateful, Stateless, and Singleton session beans using real-world examples
6. ✅ Describe Entity Beans and their purpose
7. ✅ Explain Message-Driven Beans and how they handle asynchronous messaging
8. ✅ Explain Message-Driven Controlled Delivery (Delivery Active and Delivery Groups)

---

## Prerequisites

Before starting this session, make sure you:

- ✅ Have completed the **Environment Setup** (00a — System Requirements and Setup)
- ✅ Have Eclipse IDE running and WildFly server installed
- ✅ Have basic knowledge of Java classes, objects, methods, and interfaces
- ✅ Understand the concept of a client-server architecture (client sends a request, server responds)

---

## Part 1 — Introduction to Jakarta Enterprise Beans

### 1.1 Real-World Story — What Problem Are We Solving?

Imagine you are the sole employee of a small shop. You do **everything**: greet customers, handle payments, manage inventory, call suppliers, track tax records, and lock up at night. When the shop has 10 customers a day, this works fine.

Now imagine the shop grows. Suddenly you have 10,000 customers a day. You cannot handle security checks manually. You cannot personally authorize every payment transaction. You cannot manage 50 simultaneous conversations. The shop collapses because it was not built for scale.

**This is exactly the problem that Jakarta Enterprise Beans (EJB) solves in software.**

In a traditional Java application, one program handles everything — security, transactions, data access, multi-user management, and business logic. This becomes impossible to maintain as the application grows. 

EJB is the solution: it separates the **business logic** (your actual application work) from the **infrastructure concerns** (security, transactions, concurrency) by letting the **application server (WildFly)** handle all the infrastructure automatically.

---

### 1.2 What Is Jakarta Enterprise Beans (EJB)?

```
DEFINITION:
Jakarta Enterprise Beans (EJB) is a server-side software component
that contains the BUSINESS LOGIC of an enterprise application.
It runs inside an EJB Container on an application server (like WildFly),
which automatically provides it with services like security,
transaction management, and concurrency control.
```

**Key terms broken down:**

| Term | What It Means in Plain English |
|---|---|
| **Server-side** | The code runs on the server, not on the user's computer |
| **Software component** | A self-contained, reusable piece of code |
| **Business logic** | The rules and operations specific to your application (e.g., "process a bank transfer", "check inventory levels") |
| **EJB Container** | The managed environment inside WildFly that hosts and supervises your beans |
| **Transaction management** | Automatically ensures that a series of operations either ALL succeed or ALL fail together (like a bank transfer: debit AND credit must both succeed) |

#### What Jakarta EE Is (The Big Picture)

Jakarta EE (previously called Java EE, and before that J2EE) is a **collection of APIs and standards** for building large-scale enterprise applications in Java.

EJB is just **one of many APIs** within Jakarta EE. Here are some of the key APIs in the Jakarta EE family:

```
JAKARTA EE — The Complete Ecosystem
│
├── Jakarta Enterprise Beans (EJB) ← OUR FOCUS
│   └── Business logic components
│
├── Jakarta Persistence API (JPA)
│   └── Database access and ORM
│
├── Jakarta Faces (JSF / Facelets)
│   └── Web user interface
│
├── Jakarta Messaging (JMS)
│   └── Asynchronous messaging between components
│
├── Jakarta RESTful Web Services (JAX-RS)
│   └── Building REST APIs
│
├── Jakarta Contexts and Dependency Injection (CDI)
│   └── Dependency injection framework
│
└── Jakarta Security
    └── Authentication and authorization
```

#### The Jakarta EE Runtime Choices

As mentioned in the textbook, you can run Jakarta EE applications on any **Jakarta EE-compatible application server**. Popular choices include:

| Server | Developer | Notes |
|---|---|---|
| **WildFly** | Red Hat | Free, open-source — our choice in this course |
| **Payara Server** | Payara Foundation | Free community edition available |
| **GlassFish** | Eclipse Foundation | The reference implementation |
| **Open Liberty** | IBM | Lightweight, cloud-native option |

We use **WildFly** because it is widely used in industry, well-documented, and free.

---

### 1.3 Architecture Diagram — How Jakarta EE Works

The following diagram shows the three-tier architecture of a Jakarta EE application and where EJBs live:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT TIER                               │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │  Web Browser  │  │ Java SE App   │  │ Mobile App    │       │
│  │ (HTML / JSF)  │  │ (Standalone)  │  │ (REST Client) │       │
│  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘       │
└──────────│──────────────────│──────────────────│──────────────-─┘
           │ HTTP              │ RMI/IIOP          │ REST
           ▼                  ▼                   ▼
┌─────────────────────────────────────────────────────────────────┐
│              APPLICATION SERVER (WildFly)                        │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    WEB CONTAINER                         │    │
│  │   Jakarta Faces (JSF)  │  Servlets  │  JSP / Facelets   │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                            │ calls                               │
│  ┌─────────────────────────▼──────────────────────────────-┐    │
│  │                    EJB CONTAINER ← OUR FOCUS             │    │
│  │                                                           │    │
│  │  ┌────────────────┐  ┌───────────────┐  ┌────────────┐  │    │
│  │  │ Session Beans  │  │ Entity Beans  │  │ MDB Beans  │  │    │
│  │  │ (Business Logic)│  │ (JPA Entities)│  │ (Messaging)│  │    │
│  │  └────────────────┘  └───────────────┘  └────────────┘  │    │
│  │                                                           │    │
│  │  Services provided automatically by the container:        │    │
│  │  ✅ Transaction Management  ✅ Security                   │    │
│  │  ✅ Concurrency Control      ✅ Connection Pooling         │    │
│  │  ✅ Life Cycle Management    ✅ Remote Access              │    │
│  └───────────────────────────────────────────────────────-──┘    │
└────────────────────────────┬────────────────────────────────────-┘
                              │ JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     DATA TIER (EIS TIER)                         │
│     ┌─────────────────┐          ┌──────────────────┐           │
│     │  PostgreSQL DB  │          │  Other Systems   │           │
│     │  (Your Data)    │          │  (ERP, CRM, etc) │           │
│     └─────────────────┘          └──────────────────┘           │
└─────────────────────────────────────────────────────────────────┘
```

**What this diagram shows:**
- Clients (browsers, apps) communicate with the **Web Container**
- The Web Container (JSF/Servlets) calls **Enterprise Beans** in the **EJB Container**
- The EJB Container automatically handles all infrastructure concerns (transactions, security, etc.)
- Enterprise Beans talk to the **Database** through JPA
- The developer only needs to write the **business logic** — everything else is managed by the container

---

### 1.4 What Is an Enterprise Bean?

An Enterprise Bean is a Java class that:
- Is written using standard Java
- Is annotated with EJB annotations (e.g., `@Stateless`, `@Stateful`, `@Singleton`)
- Is deployed inside an EJB container on an application server
- Contains the business logic of your application

#### How Enterprise Beans Are Packaged

Enterprise beans are packaged and deployed in different ways:

```
Enterprise Bean Files
│
├── As a standalone JAR file
│   └── myapp-ejb.jar
│       └── YourBean.class (annotated with @Stateless etc.)
│
├── Inside an Enterprise Archive (EAR) file — Most Common
│   └── myapp.ear
│       ├── myapp-ejb.jar   ← contains your EJB classes
│       ├── myapp-web.war   ← contains your web UI
│       └── lib/            ← shared library JARs
│
└── Inside a Web Archive (WAR) file — Embedded approach
    └── myapp.war
        ├── WEB-INF/classes/YourBean.class
        └── WEB-INF/lib/
```

> 💡 **Real-World Analogy**: Think of a JAR file like a box of chocolates. A WAR file is a gift bag that can contain multiple boxes. An EAR file is a large shipping crate that can contain multiple gift bags and boxes.

#### Essential Characteristics of an Enterprise Bean

The textbook lists these characteristics. Let's understand each one clearly:

**1. Business Logic** — An enterprise bean usually contains the business logic that works on the enterprise's data. The bean handles operations like `processLoanApplication()`, `transferFunds()`, or `checkInventory()`. It does NOT handle UI or database connection pooling.

**2. Container Management** — A container manages the lifecycle of each bean instance at runtime. You never create a bean with `new MyBean()` directly from your client code. The container creates, manages, pools, and destroys bean instances for you.

**3. Customizable at Deployment** — You can configure bean behavior differently for each deployment environment (Development, Testing, Production) by modifying environment entries — without changing source code.

**4. Declarative Service Information** — Services like transactions and security are specified using Java annotations (e.g., `@TransactionAttribute(REQUIRED)`, `@RolesAllowed("ADMIN")`) or in XML deployment descriptors, separate from business logic code.

**5. Portability** — An enterprise bean that uses only standard EJB APIs can be deployed on any compliant EJB container — WildFly, Payara, GlassFish, etc.

**6. Integration Without Recompilation** — Enterprise beans can be integrated into larger assembled applications without source code changes.

---

### 1.5 Why Do We Need Enterprise Beans?

The textbook states three main reasons. Let's understand each with a real-world comparison:

#### Reason 1: The Container Handles Infrastructure — So You Don't Have To

**Without EJB (The Hard Way):**
```java
// You have to manually handle transactions, security, threading yourself
public class BankService {
    
    public void transferFunds(int fromAccount, int toAccount, double amount) {
        // You manually start a database transaction
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        
        // You manually check security
        if (!currentUser.hasRole("TELLER")) {
            throw new SecurityException("Access denied");
        }
        
        // You manually handle concurrency with locks
        synchronized(this) {
            // You manually catch and roll back
            try {
                debit(fromAccount, amount, conn);
                credit(toAccount, amount, conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback(); // manually roll back
            }
        }
    }
}
```

**With EJB (The Smart Way):**
```java
@Stateless
@RolesAllowed("TELLER")             // ← Security handled by container
public class BankService {
    
    @TransactionAttribute(REQUIRED)  // ← Transaction handled by container
    public void transferFunds(int fromAccount, int toAccount, double amount) {
        // Just write your business logic!
        debit(fromAccount, amount);
        credit(toAccount, amount);
        // The container automatically commits or rolls back
        // The container automatically enforces security roles
        // The container automatically handles concurrency
    }
}
```

> **The container takes care of transaction management, security, and concurrency so the developer focuses ONLY on the business problem.**

#### Reason 2: Thin Clients — Clients Don't Need to Know Business Rules

When business logic lives inside EJBs on the server:
- **Client applications** (web browser, mobile app, desktop app) only need to display data and capture input
- They do NOT need to contain database access code, business rules, or security checks
- This makes clients smaller, simpler, and easier to maintain

> 💡 **Real-World Analogy**: Think of an ATM machine. The ATM (client) only collects your card details and amount. All the business logic (check your balance, verify PIN, debit your account, keep audit records) runs on the bank's server. The ATM itself contains no financial logic. This is a thin client.

#### Reason 3: Scalability, Transactions, and Remote Access

Use enterprise beans when your application needs:

| Requirement | How EJB Helps |
|---|---|
| **Scalability** | Beans run on multiple servers; clients can't tell the difference |
| **Transaction Integrity** | Transactions ensure simultaneous users don't corrupt shared data |
| **Wide Client Support** | Remote clients can access beans from different machines using just a few lines of code |

---

## Part 2 — Session Beans and Their Types

### 2.1 What Is a Session Bean?

> **DEFINITION**: A Session Bean is an enterprise bean that contains business logic to be executed on behalf of a client over a network. It can be local (same JVM) or remote (different JVM).

Session beans get their name from the concept of a "session" — a period of interaction between a client and the bean. Think of it like a phone call:

- You call a customer service number → the call is your **session**
- The representative helps you → they are executing **business logic**
- The call ends → the **session terminates**

**Key features of session beans:**
- They hold business logic, NOT persistent data (not stored in a database)
- They can be very lightweight and efficient
- They come in three types: Stateful, Stateless, and Singleton

---

### 2.2 The Three Types of Session Beans

```
SESSION BEANS
│
├── STATEFUL ──────── Remembers each client's conversation
│   @Stateful         (Like your personal bank account officer)
│
├── STATELESS ─────── Serves any client, forgets immediately
│   @Stateless        (Like a bank teller at a counter)
│
└── SINGLETON ─────── One single instance for the whole application
    @Singleton        (Like the bank manager — one, for everyone)
```

---

### 2.3 Stateful Session Beans — Deep Explanation

#### The Real-World Analogy

Think of ordering a custom birthday cake at a bakery. The baker (your session bean):
1. Takes your order — remembers your name and requirements ✅
2. Updates you when the cake is in the oven — still remembers your details ✅
3. Calls you when it's ready — still remembers your order ✅
4. You pick up the cake — the order is complete. The baker forgets your specific order ✅

The baker **maintained conversational state** with you throughout the entire process. A Stateful Session Bean works exactly the same way.

#### Technical Definition

A **Stateful Session Bean** maintains a **conversational state** between method calls for a single client. The instance variables of the bean store the client's state and persist across multiple method invocations until the client ends the session.

**Key Characteristics:**
- Serves **only one client at a time**
- Maintains state between method calls
- Can be **passivated** (temporarily stored to disk when idle) to free memory
- Can be **activated** (brought back from disk) when the client makes another call
- Destroyed when `@Remove` method is called or the session times out

#### Stateful Session Bean Lifecycle

```
                    ┌─────────────────┐
                    │   DOES NOT EXIST │
                    └────────┬────────┘
                             │ Container creates instance
                             │ @PostConstruct called
                             ▼
                    ┌─────────────────┐
                    │     READY       │◄──────────────────┐
                    │  (In Memory)    │                    │
                    └────────┬────────┘                    │
                             │ No client calls for          │ @Activate
                             │ a while                      │ (brought back)
                             ▼                             │
                    ┌─────────────────┐                    │
                    │   PASSIVATED    │────────────────────┘
                    │  (On Disk)      │
                    └────────┬────────┘
                             │ @Remove called or timeout
                             ▼
                    ┌─────────────────┐
                    │   DESTROYED     │
                    │ @PreDestroy     │
                    └─────────────────┘
```

#### Complete Stateful Bean Code Example

Let's build a simple Stateful Session Bean that adds numbers and keeps a running total (this requires state — it must remember previous results).

**Step 1: Create the Remote Interface**

The interface defines what methods are available to clients.

```java
package org.stateful.co;

import jakarta.ejb.Remote;

@Remote   // ← This annotation says: this interface can be accessed from remote clients (different JVMs)
public interface DemoExampleBeanRemote {
    
    // Method signature: takes two integers, returns their sum
    public int add(int a, int b);
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `package org.stateful.co;` | Declares the package (like a folder for organizing Java classes) |
| `import jakarta.ejb.Remote;` | Imports the @Remote annotation from the Jakarta EJB library |
| `@Remote` | Marks this interface as accessible by remote clients across the network |
| `public interface DemoExampleBeanRemote` | Defines a Java interface (a contract — it lists methods without implementations) |
| `public int add(int a, int b);` | Declares the `add` method — it accepts two ints and returns an int |

**Step 2: Create the Stateful Bean Class**

```java
package org.stateful.co;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateful;

@Stateful    // ← Tells the EJB container: "This is a Stateful Session Bean"
@LocalBean   // ← This bean also exposes itself directly (without a separate interface)
public class DemoExampleBean implements DemoExampleBeanRemote {
    
    // This method adds two numbers and returns the result
    public int add(int a, int b) {
        return a + b;
    }
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `@Stateful` | The most important annotation — tells the container this is a Stateful Session Bean, meaning the container will maintain conversational state per client |
| `@LocalBean` | Allows the bean to be accessed locally (without going through a remote interface) in addition to the remote interface |
| `public class DemoExampleBean implements DemoExampleBeanRemote` | The class implements the remote interface — it must provide real code for all methods declared in the interface |
| `public int add(int a, int b) { return a + b; }` | The actual business logic: adds two integers and returns the result |

**Step 3: Create an Application Client (Remote Client)**

```java
import org.stateful.co.DemoExampleBean;
import org.stateful.co.DemoExampleBeanRemote;

public class Main {
    
    // This variable will hold the reference to our remote bean
    public static DemoExampleBeanRemote DemoExampleBean;
    
    public static void main(String[] args) {
        
        // Create an instance of the bean (in a real remote scenario,
        // this would be done via JNDI lookup - covered in Session 3)
        DemoExampleBeanRemote DemoExampleBean = new DemoExampleBean();
        
        // Call the add() method on the bean and print the result
        // This calls the business logic inside the EJB
        System.out.println("Result:" + DemoExampleBean.add(4, 2));
        // Expected output: Result:6
    }
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `public static DemoExampleBeanRemote DemoExampleBean` | Declares a variable of type `DemoExampleBeanRemote` (the remote interface) — we use the interface type, not the concrete class |
| `new DemoExampleBean()` | Creates an instance of the bean (simplified for now; real clients use JNDI lookup) |
| `DemoExampleBean.add(4, 2)` | Calls the `add()` method remotely — the client sends `4` and `2` to the server, the bean computes `4+2=6`, and returns `6` |
| `System.out.println(...)` | Prints the result to the console: `Result:6` |

---

### 2.4 Stateless Session Beans — Deep Explanation

#### The Real-World Analogy

Think of a fast-food restaurant cashier:
- You walk up: "One burger, please." The cashier takes your order and processes it. Done. ✅
- The next customer walks up: "One fries, please." The same cashier serves them — with NO memory of you. ✅
- The cashier does not remember your name, your previous orders, or anything about you between transactions.

This is exactly a **Stateless Session Bean** — it serves each client's request completely, then is returned to a pool to serve any other client's request. **No memory, no state.**

#### Technical Definition

A **Stateless Session Bean** contains business logic but maintains **no conversational state** between method calls. After each method call completes, the container can return the bean instance to a pool and assign it to any other client.

**Key Characteristics:**
- Can serve **multiple clients** efficiently using a bean pool
- No state is maintained between method calls
- Can implement **Web service endpoints**
- Better scalability than Stateful beans (fewer instances needed to serve many clients)
- State only exists **during a single method call**

#### Stateless Session Bean Lifecycle

```
Container starts up
        │
        ▼
┌───────────────┐
│ BEAN POOL     │  ← Container pre-creates multiple instances
│ ┌──┐ ┌──┐    │
│ │B1│ │B2│ B3 │  ← These beans are IDLE, waiting
│ └──┘ └──┘    │
└───────┬───────┘
        │ Client request arrives
        │ Container picks any available bean from the pool
        ▼
┌───────────────┐
│   BUSY        │
│  (serving     │  ← Bean is processing the client's method call
│   client)     │  ← State exists ONLY during this method call
└───────┬───────┘
        │ Method returns (call complete)
        │ Bean forgets everything, returns to pool
        ▼
┌───────────────┐
│ BEAN POOL     │  ← Bean is ready for any new client
└───────────────┘
```

#### Complete Stateless Bean Code Example — Shopping Portal

```java
package org.stateless.ejb;

import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

/**
 * Session Bean implementation for a Shopping Portal Product Catalog
 * This is STATELESS - no state is maintained between client calls
 */
@Stateless   // ← Marks this as a Stateless Session Bean
@LocalBean
public class StateLess implements StateLessRemote {
    
    // NOTE: This list is re-created every time addProduct() is called!
    // That is the nature of stateless beans - state does not persist
    List<String> products;
    
    // Business method 1: Add a product to the list
    public void addProduct(String productName) {
        products = new ArrayList<String>();  // creates a fresh list each time
        products.add(productName);           // adds the product
    }
    
    // Business method 2: Get the list of products
    public List<String> getProducts() {
        return products;  // returns whatever is currently in the list
    }
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `@Stateless` | Marks this as a Stateless Session Bean. The container does NOT preserve instance variable values between separate client calls |
| `List<String> products;` | Instance variable — BUT since this is stateless, this variable is only valid during a single method call |
| `public void addProduct(String productName)` | Business method that adds a product. Returns `void` (nothing) |
| `products = new ArrayList<String>()` | Creates a fresh list every time the method is called — this shows the stateless nature |
| `public List<String> getProducts()` | Returns the current product list |

**The Remote Interface for StateLess:**

```java
package org.stateless.ejb;

import java.util.List;
import jakarta.ejb.Remote;

@Remote   // ← Accessible by remote clients
public interface StateLessRemote {
    
    void addProduct(String productName);  // adds a product
    
    List getProducts();  // returns all products
}
```

**The Remote Client:**

```java
import java.util.ArrayList;
import java.util.List;
import org.stateless.ejb.StateLessRemote;

public class Main {
    
    public static StateLessRemote StateLess;  // reference to the remote interface
    
    public static void main(String[] args) {
        
        // Create a bean instance (via JNDI lookup in real scenarios)
        StateLessRemote StateLess = new org.stateless.ejb.StateLess();
        
        List PList = new ArrayList();  // a list to hold our results
        
        StateLess.addProduct("MobilePhone");  // call the business method
        
        PList = StateLess.getProducts();  // retrieve the products list
        
        System.out.println(PList);  // prints: [MobilePhone]
    }
}
```

---

### 2.5 Singleton Session Beans — Deep Explanation

#### The Real-World Analogy

Think of a **bank manager**. There is **only one** bank manager in the branch. All bank tellers report to this one manager. All decisions about the bank (policy, limits, daily totals) go through the single manager. Even if 100 employees walk in, there is still ONE manager — shared by everyone.

That is a **Singleton Session Bean** — one instance, shared across the entire application, by all clients simultaneously.

#### Technical Definition

A **Singleton Session Bean** is instantiated **once per application** and that single instance is shared by all clients. It persists for the entire lifetime of the application.

**Key Characteristics:**
- **Only ONE instance** exists for the entire application
- Shared by **multiple clients simultaneously** (must handle concurrency carefully)
- Maintains state across all client interactions (since there is only one instance)
- Can use `@Startup` to be created when the application starts
- Used for: application-wide caches, counters, configuration data, initialization tasks

#### Singleton Session Bean Example — Audit Logger

```java
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;

@Singleton    // ← ONE instance only, shared by everyone
@Startup      // ← Create this bean when the application starts up (before any request arrives)
public class AuditLogger {
    
    private int totalTransactions = 0;  // shared counter — ALL clients increment this same variable
    
    @Lock(LockType.WRITE)  // ← Only one thread can write at a time
    public void logTransaction(String description) {
        totalTransactions++;  // increment the shared counter
        System.out.println("Transaction #" + totalTransactions + ": " + description);
    }
    
    @Lock(LockType.READ)   // ← Multiple threads can read simultaneously
    public int getTotalTransactions() {
        return totalTransactions;  // return the shared counter
    }
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `@Singleton` | Only one instance created for the whole application |
| `@Startup` | Creates this bean immediately when the application deploys (no client request needed) |
| `private int totalTransactions = 0` | This variable is shared across ALL clients — everyone adding to the same counter |
| `@Lock(LockType.WRITE)` | Thread safety: only ONE thread can execute this method at a time |
| `@Lock(LockType.READ)` | Multiple threads can call this at the same time (reading is safe) |

---

### 2.6 Comparison — Stateful vs Stateless vs Singleton

```
┌──────────────┬────────────────┬─────────────────┬─────────────────┐
│  Feature     │   STATEFUL     │   STATELESS     │   SINGLETON     │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Annotation   │ @Stateful      │ @Stateless      │ @Singleton      │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Instances    │ 1 per client   │ Pool (many)     │ 1 per app       │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ State        │ Maintained     │ Not maintained  │ Maintained      │
│              │ per client     │                 │ across all      │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Clients      │ One at a time  │ Many clients    │ Many clients    │
│              │                │ (from pool)     │ simultaneously  │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Web Services │ ❌ No          │ ✅ Yes          │ ✅ Yes          │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Use Case     │ Shopping cart  │ Product catalog │ App config      │
│ Example      │ Loan workflow  │ Currency convert│ Audit counter   │
│              │ Checkout flow  │ Product search  │ Exchange rates  │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Memory Use   │ Higher         │ Lower           │ Fixed (1 inst.) │
├──────────────┼────────────────┼─────────────────┼─────────────────┤
│ Passivation  │ ✅ Yes         │ ❌ No           │ ❌ No           │
│ (to disk)    │                │                 │                 │
└──────────────┴────────────────┴─────────────────┴─────────────────┘
```

---

### 2.7 Entity Beans

#### What Is an Entity Bean?

> **DEFINITION**: An Entity Bean represents a **persistent business object** — an object whose data is stored in a database and survives beyond a single session. In modern Jakarta EE, Entity Beans are implemented as **JPA Entities** using the `@Entity` annotation.

> 📝 **Historical Note**: In the old EJB 2.x days, Entity Beans were heavyweight container-managed persistence (CMP) objects. In modern Jakarta EE (EJB 3.0 onwards), the Java Persistence API (JPA) replaced the old Entity Bean approach. When the textbook says "Entity Bean" in the modern context, it means a **JPA Entity class**.

#### Entity Bean Analogy

Think of a library catalogue card. Each card represents a unique book:
- It has a unique identifier (the Dewey decimal number = **primary key**)
- It contains data about the book (title, author, year = **fields**)
- The card exists permanently in the catalogue, even when no one is looking at it (= **persistence**)
- Multiple people can look up the same card (= **shared access**)

#### Entity Bean Code Example

```java
import jakarta.persistence.Entity;   // marks this class as a JPA Entity
import jakarta.persistence.Id;        // marks the primary key field
import java.io.Serializable;

@Entity   // ← This class is mapped to a database table
public class Flight implements Serializable {
    
    // The primary key field — uniquely identifies each row in the Flight table
    Long NUM;
    
    @Id   // ← Marks NUM as the primary key
    public Long getNUM() { 
        return NUM; 
    }
    
    public void setNUM(Long id) { 
        this.NUM = id; 
    }
}
```

**What This Maps To In the Database:**

```
Java Class                         Database Table
──────────────────                 ──────────────────────
class Flight          →            TABLE: Flight
  Long NUM (primary key)  →        NUM (PRIMARY KEY, BIGINT)
  String destination      →        destination (VARCHAR)
  Date departureTime      →        departure_time (TIMESTAMP)
```

#### Components of an Entity Bean (Traditional View)

The textbook describes four components. Here they are explained clearly:

| Component | What It Is | Plain English |
|---|---|---|
| **Remote Component** | Interface listing all business methods | "What can clients call on this entity?" |
| **Home Interface** | Factory for creating/finding entities | "How do I create, find, or delete an entity?" |
| **Primary Key Class** | The unique identifier object | "What makes this entity unique in the database?" |
| **Bean Class** | The actual class with business logic | "The real object with all the code" |

---

### 2.8 Message-Driven Beans (MDB)

#### What Is a Message-Driven Bean?

> **DEFINITION**: A Message-Driven Bean (MDB) is an enterprise bean that processes **asynchronous messages** — it sits in the background and "listens" for messages, then executes business logic when a message arrives.

#### The Real-World Analogy

Think of an **email inbox with an auto-responder**:
- You send an email (a "message") to support@company.com
- You don't wait for someone to be at their desk
- The auto-responder (MDB) is always listening
- As soon as your email arrives, it automatically processes and replies
- This is **asynchronous** — you sent the message and moved on with your life; the processing happened independently

#### Why Use MDBs?

Consider a bank's fund transfer system. If 10,000 people click "Transfer Money" at the same time:
- **Synchronous approach**: All 10,000 people wait for their transfer to complete before they can do anything else → system becomes slow, unresponsive, or crashes.
- **Asynchronous approach with MDB**: All 10,000 transfer requests are placed into a **queue** (a waiting line). The user gets an immediate "Transfer submitted" response. MDB instances process queue items one by one in the background → system stays fast and responsive.

#### MDB Architecture Diagram

```
CLIENT                    JMS QUEUE                  MDB (Listener)
  │                           │                           │
  │─── sends message ────────►│                           │
  │    (Fund Transfer)        │                           │
  │◄── "Request Queued" ──────│                           │
  │    (immediate response)   │                           │
  │                           │                           │
  │                           │──── message arrives ─────►│
  │                           │                           │── processes transfer
  │                           │                           │── updates database
  │                           │                           │── sends email notification
  │                           │                           │   (all in the background)
```

#### MDB Code Example

```java
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

@MessageDriven(
    mappedName = "jms/FundTransferQueue"  // ← listens to this specific queue
)
public class FundTransferProcessor implements MessageListener {
    
    // This method is called automatically by the container
    // whenever a message arrives in the queue
    @Override
    public void onMessage(Message message) {
        
        try {
            // Cast the generic message to a TextMessage
            TextMessage textMessage = (TextMessage) message;
            
            // Get the actual text content of the message
            String transferDetails = textMessage.getText();
            
            // Process the fund transfer (business logic)
            System.out.println("Processing transfer: " + transferDetails);
            
            // In a real application, you would:
            // 1. Parse the transfer details (account numbers, amount)
            // 2. Update the database
            // 3. Send a confirmation email
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**Code Explanation — Line by Line:**

| Line | What It Does |
|---|---|
| `@MessageDriven` | Marks this as a Message-Driven Bean |
| `mappedName = "jms/FundTransferQueue"` | JNDI name of the queue this MDB will listen to |
| `implements MessageListener` | MDBs must implement the `MessageListener` interface from the JMS API |
| `public void onMessage(Message message)` | The container calls this method automatically when a new message arrives |
| `TextMessage textMessage = (TextMessage) message` | Casts the generic `Message` to a `TextMessage` (since we know we're sending text) |
| `textMessage.getText()` | Extracts the actual text content from the message |

#### Similarities Between MDBs and Stateless Session Beans

The textbook notes these important similarities:

| Feature | MDB | Stateless Session Bean |
|---|---|---|
| State maintained? | ❌ No | ❌ No |
| Multiple clients? | ✅ Yes (from pool) | ✅ Yes (from pool) |
| Called by? | JMS messages | Client method calls |
| Has interface? | ❌ No (no client interface) | ✅ Yes (@Remote or @Local) |

---

### 2.9 Message-Driven Controlled Delivery

This is an advanced MDB feature that lets you **control whether and when an MDB actively processes messages**.

#### Delivery Active

**`Delivery Active`** controls whether an MDB is currently accepting and processing messages.

- When `deliveryActive = true` → the MDB is **listening and processing** messages from the queue
- When `deliveryActive = false` → the MDB is **paused** — messages accumulate in the queue but are NOT processed until delivery is re-enabled

**Real-World Scenario**: Imagine a mail room that processes packages. During their lunch break (12:00 PM – 1:00 PM), packages keep arriving and pile up in the mailroom. When the workers return, they process all the accumulated packages. The mailroom controlled its "delivery active" status.

```java
// Example: MDB with delivery active annotation
// (WildFly-specific — behavior may differ per application server)
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(
            propertyName = "destination",
            propertyValue = "java:/jms/queue/FundTransferQueue"
        ),
        @ActivationConfigProperty(
            propertyName = "destinationType",
            propertyValue = "jakarta.jms.Queue"
        ),
        @ActivationConfigProperty(
            propertyName = "acknowledgeMode",
            propertyValue = "Auto-acknowledge"
        )
    }
)
public class FundTransferProcessor implements MessageListener {
    // ...bean code...
}
```

You can programmatically pause/resume delivery using the WildFly management CLI or by using deployment configuration.

#### Delivery Groups

**`Delivery Groups`** allow multiple MDBs to be grouped together and have their delivery activation state controlled collectively.

- A delivery group is a **named set of MDBs** that share a common on/off switch
- Controlling the group automatically affects all MDBs in it
- This is particularly useful for **coordinated maintenance or failover scenarios**

**Real-World Scenario**: A hospital has separate departments (Emergency, Surgery, Pharmacy) — each representing a "delivery group" of message processors. During a system upgrade, the IT team can pause just the "Pharmacy" delivery group while keeping "Emergency" active.

```
Delivery Group: "banking-processors"
  ├── FundTransferProcessor (MDB 1)
  ├── LoanApplicationProcessor (MDB 2)
  └── AuditEventProcessor (MDB 3)
          │
          ▼
  Control group "banking-processors" → pauses/resumes ALL three MDBs together
```

---

### 2.10 How Today's Concepts Connect to the Capstone Project

In our **GlobalBank Enterprise Platform**, we will use all the beans from this session:

```
GLOBALBANK APPLICATION
│
├── AccountService (@Stateless)
│   └── processDeposit(), processWithdrawal(), getAccountBalance()
│   └── WHY STATELESS: Each transaction is independent — no need to remember between calls
│
├── LoanApplicationService (@Stateful)
│   └── submitBasicInfo(), submitEmploymentDetails(), submitDocuments(), finalizeLoan()
│   └── WHY STATEFUL: A loan application has multiple steps — we must remember
│                      each step until the customer completes the application
│
├── AuditLogger (@Singleton + @Startup)
│   └── logEvent(), getTotalTransactionsToday()
│   └── WHY SINGLETON: One shared audit log for the whole application
│
├── FundTransferProcessor (@MessageDriven)
│   └── onMessage() ← processes transfer requests from the queue asynchronously
│   └── WHY MDB: Transfers should not make customers wait — queue them and process
│               them in the background
│
└── Account (@Entity)
    └── accountId (primary key), accountNumber, balance, accountHolder
    └── WHY ENTITY: Account data must persist in the database across sessions
```

---

### 2.11 Common Mistakes to Avoid

| ❌ Mistake | ✅ Correct Approach |
|---|---|
| Using `@Stateful` for a simple calculation that doesn't need memory | Use `@Stateless` for any method that doesn't need to remember previous calls |
| Creating beans with `new MyBean()` in client code | Use JNDI lookup or `@EJB` / `@Inject` injection to get bean references |
| Forgetting `@Remote` or `@Local` annotation on the interface | Always annotate your interface with `@Remote` (different JVM) or `@Local` (same JVM) |
| Assuming stateless beans retain data between calls | State in stateless beans only exists during one method call — do NOT rely on instance variables persisting |
| Using a Singleton without `@Lock` annotations | Always use `@Lock(WRITE)` for write operations and `@Lock(READ)` for reads in Singleton beans |
| Making an MDB implement multiple interfaces | MDBs implement only `MessageListener` from the JMS API |
| Forgetting `implements MessageListener` on an MDB | Without this, the container cannot invoke `onMessage()` automatically |

---

### 2.12 Knowledge Check Questions

Try answering these without looking at your notes:

1. What does EJB stand for, and which component of Jakarta EE is it?
2. Name three services that the EJB Container provides automatically to enterprise beans.
3. A customer logs into an online banking application and adds items to their loan application over multiple steps. Which type of session bean should be used, and why?
4. What is the difference between how a Stateless bean and a Stateful bean handle client state?
5. You need a shared counter that tracks total number of API calls made by all users. Which bean type is most appropriate?
6. Explain, in plain English, what a Message-Driven Bean does and give a real-world use case.
7. Why would you use MDB for fund transfers instead of calling the transfer method directly from the web page?
8. What annotation marks a class as a JPA Entity (modern Entity Bean)?

---

### 2.13 Summary

| Concept | Key Takeaway |
|---|---|
| **Jakarta EE / EJB** | A framework for building scalable, secure enterprise Java applications where the container handles infrastructure |
| **Enterprise Bean** | A Java class annotated with EJB annotations, containing business logic, managed by the EJB container |
| **Why EJBs?** | Container handles transactions, security, and concurrency — developers focus on business logic only |
| **Stateful Session Bean** | Remembers state per client across multiple method calls. One instance per client. |
| **Stateless Session Bean** | No state between calls. Served from a pool. Highly scalable. |
| **Singleton Session Bean** | One instance per application, shared by all clients. Good for app-wide data. |
| **Entity Bean (JPA Entity)** | Persistent business object mapped to a database table. Has a primary key. |
| **Message-Driven Bean** | Listens for JMS messages asynchronously. Like an email auto-responder for your application. |
| **Delivery Active** | Controls whether an MDB is actively processing messages (can be paused/resumed) |
| **Delivery Groups** | Named groups of MDBs whose delivery can be controlled collectively |

---

## 📺 Recommended Video Resources
To reinforce the concepts covered in this session, consider searching for the following video tutorials on YouTube:

1. **Channel:** Java Brains  
   **Video Title:** *Jakarta EE / Java EE Tutorial - Introduction to EJB*
2. **Channel:** Derek Banas  
   **Video Title:** *Java EE Tutorial*
3. **Channel:** Telusko  
   **Video Title:** *EJB (Enterprise Java Beans) Introduction*
