# Class Task — Session 1 & 2
## Introduction to Jakarta Enterprise Beans + Session Bean Types

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1
**Task Type:** In-Class (Done with Instructor)
**Estimated Time:** 45 – 60 minutes

---

## Objective

By the end of this class task, you will have:
- Created your **very first EJB project** in IntelliJ IDEA (or Eclipse)
- Written and deployed a **Stateless Session Bean**
- Written a client that calls the bean
- Seen the results running live on WildFly

This is your "Hello, World!" of Jakarta Enterprise Beans.

---

## What You Need Before Starting

Make sure all of these are ready:
- [ ] IntelliJ IDEA (or Eclipse IDE for Enterprise Java) is open
- [ ] WildFly server is installed and can be started
- [ ] Maven or `jakarta.jakartaee-api` dependency configured
- [ ] You have completed the Environment Setup guide (00a)

---

## Scenario

You are a junior developer at **ZenTech Solutions**. Your team leader has asked you to create a simple EJB prototype — a **Calculator Service** that adds, subtracts, multiplies, and divides two numbers. This will be the first EJB component your team deploys to the WildFly server. Your team leader will review it after class.

---

## Step-by-Step Instructions

### STEP 1 — Create a New EJB Project

#### Option A: In IntelliJ IDEA (Recommended)
1. Launch IntelliJ IDEA and click **New Project**.
2. Select **Jakarta EE** (or Maven archetype).
3. **Template:** Web application or EJB.
4. **Application Server:** Select your configured WildFly instance.
5. In your `pom.xml`, ensure `jakarta.jakartaee-api` is present with `<scope>provided</scope>`.

![IntelliJ IDEA WildFly Configuration](../assets/images/intellij_wildfly_setup.jpg)

#### Option B: In Eclipse IDE
**1a.** Launch Eclipse. On the top menu bar, click:
```
File → New → Project
```

**1b.** In the "New Project" dialog that opens, scroll down and look for:
```
EJB → EJB Project
```
Select **EJB Project** and click **Next**.

> ✅ **What you should see**: A dialog box titled "New EJB Project" with several input fields.

**1c.** Fill in the project details:

| Field | Value to Enter |
|---|---|
| **Project name** | `CalculatorEJB` |
| **Project location** | Leave as default (your Eclipse workspace) |
| **Target runtime** | Select `WildFly 26.1.2.Final` (your installed server) |
| **EJB module version** | `3.2` |
| **Configuration** | Leave as default |

**1d.** Click **Finish**.

> ✅ **Expected result**: A new project called `CalculatorEJB` appears in the **Project Explorer** panel on the left side of Eclipse.

---

### STEP 2 — Add the Jakarta EE API Jar to the Build Path

Before writing any EJB code, Eclipse needs to know where the Jakarta EJB annotations (like `@Stateless`) are defined.

**2a.** Right-click on your `CalculatorEJB` project in Project Explorer.

**2b.** Select: `Properties`

**2c.** In the Properties dialog, click on: `Java Build Path`

**2d.** Click the **Libraries** tab, then click **Add External JARs...**

**2e.** Navigate to where you saved `jakarta.jakartaee-api-9.0.0.jar` and select it.

**2f.** Click **Apply and Close**.

> ✅ **Expected result**: The JAR file appears under "Libraries" in the Java Build Path. Now Eclipse recognizes `@Stateless`, `@Remote`, and all other Jakarta EJB annotations.

---

### STEP 3 — Create the Remote Interface

The remote interface defines WHAT the bean can do (the contract). We create this first.

**3a.** In Project Explorer, expand `CalculatorEJB` → `ejbModule`

**3b.** Right-click on `ejbModule`, then select:
```
New → Interface
```

**3c.** Fill in:

| Field | Value |
|---|---|
| **Package** | `com.zentech.calculator` |
| **Name** | `CalculatorRemote` |

**3d.** Click **Finish**.

**3e.** Eclipse will open the new interface file. Replace everything inside it with the following code:

```java
package com.zentech.calculator;

import jakarta.ejb.Remote;

@Remote
public interface CalculatorRemote {
    
    int add(int a, int b);
    
    int subtract(int a, int b);
    
    int multiply(int a, int b);
    
    double divide(double a, double b);
}
```

**3f.** Save the file: `Ctrl + S` (Windows) or `Cmd + S` (macOS).

> ✅ **Expected result**: No red error underlines. The file is saved with the four method signatures.

---

### STEP 4 — Create the Stateless Session Bean Class

Now we create the actual bean — the class that IMPLEMENTS the interface.

**4a.** Right-click on the `com.zentech.calculator` package, then select:
```
New → Session Bean (EJB 3.x)
```
(If you don't see this option, select `New → Class` instead)

**4b.** Fill in:

| Field | Value |
|---|---|
| **Java package** | `com.zentech.calculator` |
| **Class name** | `CalculatorBean` |
| **Session type** | `Stateless` |
| **Remote business interface** | `com.zentech.calculator.CalculatorRemote` |

**4c.** Click **Finish**.

**4d.** Replace the entire contents of `CalculatorBean.java` with this code:

```java
package com.zentech.calculator;

import jakarta.ejb.Stateless;

/**
 * CalculatorBean - A Stateless Session Bean that performs arithmetic operations.
 *
 * WHY STATELESS?
 * - Each calculation is independent. We do not need to remember
 *   the results of previous calculations between method calls.
 * - Multiple users can use this bean simultaneously from the pool.
 */
@Stateless
public class CalculatorBean implements CalculatorRemote {

    /**
     * Adds two integers and returns their sum.
     * Example: add(10, 5) returns 15
     */
    @Override
    public int add(int a, int b) {
        System.out.println("[CalculatorBean] add() called with: " + a + " + " + b);
        return a + b;
    }

    /**
     * Subtracts b from a and returns the result.
     * Example: subtract(10, 5) returns 5
     */
    @Override
    public int subtract(int a, int b) {
        System.out.println("[CalculatorBean] subtract() called with: " + a + " - " + b);
        return a - b;
    }

    /**
     * Multiplies two integers and returns the product.
     * Example: multiply(10, 5) returns 50
     */
    @Override
    public int multiply(int a, int b) {
        System.out.println("[CalculatorBean] multiply() called with: " + a + " * " + b);
        return a * b;
    }

    /**
     * Divides a by b and returns the result as a double.
     * Example: divide(10, 4) returns 2.5
     *
     * NOTE: We check for division by zero to prevent ArithmeticException.
     */
    @Override
    public double divide(double a, double b) {
        System.out.println("[CalculatorBean] divide() called with: " + a + " / " + b);
        
        if (b == 0) {
            // Throw a meaningful error instead of crashing
            throw new ArithmeticException("Cannot divide by zero!");
        }
        
        return a / b;
    }
}
```

**4e.** Save the file: `Ctrl + S`.

> ✅ **Expected result**: No red error underlines. You have a working Stateless Session Bean.

---

### STEP 5 — Deploy the EJB to WildFly

**5a.** In the Servers tab (bottom of Eclipse), right-click on `WildFly 26.1.2.Final`.

**5b.** Select **Start** and wait for it to fully start (watch the Console tab).

**5c.** Right-click on `WildFly 26.1.2.Final` → **Add and Remove...**

**5d.** In the dialog, select `CalculatorEJB` from the left panel and click **Add >**, then **Finish**.

> ✅ **Expected result**: In the Console tab, you should see a line like:
> ```
> Deployed "CalculatorEJB.jar" (runtime-name : "CalculatorEJB.jar")
> ```

---

### STEP 6 — Create an Application Client to Test the Bean

**6a.** Create a new Java project (not EJB project) called `CalculatorClient`.

**6b.** Add the EJB JAR and the WildFly client JARs to its build path.

**6c.** Create a class `Main.java` with this code:

```java
import com.zentech.calculator.CalculatorRemote;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Client that looks up and uses the CalculatorBean remotely via JNDI.
 */
public class Main {

    public static void main(String[] args) throws NamingException {
        
        // Step 1: Look up the bean using JNDI (Java Naming and Directory Interface)
        // This is how clients find EJBs on the server without hardcoding server details
        // (Full JNDI will be covered in Session 3 - this is a simplified version)
        InitialContext ctx = new InitialContext();
        
        // The JNDI name follows this pattern:
        // ejb:/{app-name}/{module-name}/{bean-class-name}!{remote-interface-name}
        CalculatorRemote calculator = (CalculatorRemote) ctx.lookup(
            "ejb:/CalculatorEJB/CalculatorBean!com.zentech.calculator.CalculatorRemote"
        );
        
        // Step 2: Call the bean's business methods
        System.out.println("=== ZenTech Calculator Service ===");
        System.out.println("10 + 5 = " + calculator.add(10, 5));
        System.out.println("10 - 5 = " + calculator.subtract(10, 5));
        System.out.println("10 * 5 = " + calculator.multiply(10, 5));
        System.out.println("10 / 4 = " + calculator.divide(10, 4));
        System.out.println("================================");
    }
}
```

---

### STEP 7 — Verify Results

When you run the client, you should see:

```
=== ZenTech Calculator Service ===
10 + 5 = 15
10 - 5 = 5
10 * 5 = 50
10 / 4 = 2.5
================================
```

And in the WildFly Console (Eclipse console tab), you should see:
```
[CalculatorBean] add() called with: 10 + 5
[CalculatorBean] subtract() called with: 10 - 5
[CalculatorBean] multiply() called with: 10 * 5
[CalculatorBean] divide() called with: 10.0 / 4.0
```

> ✅ **If you see these outputs — CONGRATULATIONS! You have successfully built and called your first Enterprise Bean!**

---

## Discussion Questions (Instructor-Led, After the Task)

After completing the task, your instructor will lead a brief discussion. Think about your answers:

1. **Why** did we use `@Stateless` for the Calculator bean? What would happen if we used `@Stateful` instead?

2. Looking at the client code — the client never used `new CalculatorBean()`. Instead, it used JNDI lookup. **Why** can't the client just create the bean with `new`?

3. The `divide()` method checks for division by zero and throws an exception. What would happen to the client if we **didn't** have that check and the user tried to divide by zero?

4. The `@Remote` annotation on the interface allows remote access. If your client was running **in the same JVM** as the server (i.e., inside the same application), what annotation would you use on the interface instead?

5. Right now, the Calculator bean can only be used by one method call at a time from each bean instance. But we said stateless beans support "many clients". How does the EJB container achieve this?

---

## Troubleshooting Guide

If something is not working, check these common issues:

| Problem | What To Check |
|---|---|
| Red underlines on `@Stateless` or `@Remote` | Make sure `jakarta.jakartaee-api-9.0.0.jar` is in the build path |
| WildFly won't start | Check if another program is using port 8080 (close Skype, IIS, etc.) |
| "Deployment failed" in console | Right-click the project → Clean → then re-add to server |
| JNDI lookup fails | Make sure the JNDI name string is exactly correct (case-sensitive) |
| "ClassNotFoundException" in client | Make sure the EJB JAR is on the client's build path |
