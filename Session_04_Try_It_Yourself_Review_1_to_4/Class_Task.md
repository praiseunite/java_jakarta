# Session 5 — Class Task (TIY - Try It Yourself Integration)
## GlobalBank Milestone 1: The Core Architecture

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 5)
**Duration:** 90 minutes (Extended Lab)

---

## 🎯 Task Objective

In this session, you will consolidate the "Try It Yourself" (TIY) exercises from Sessions 1 through 4 of the textbook. You will not write isolated examples anymore. Instead, you will combine them into **Milestone 1 of the GlobalBank Application**.

You will build:
1. A **Stateful EJB** that remembers a user's transaction history (Session 2).
2. A **Stateless EJB** that processes the transactions using **Transactions** and **Security** (Session 4).
3. A **Servlet Client** that uses **JNDI** to connect to these beans and output the result (Session 3).

---

## 🛠️ Step-by-Step Instructions

### Step 1: The Secure, Transactional EJB (Stateless)

Create the bean that will do the heavy lifting. It must require a transaction and require a specific security role.

1. Open IntelliJ IDEA.
2. In your EJB module, create `PaymentProcessor.java`.
3. Annotate it to be a **Stateless** bean.
4. Add the `@RolesAllowed("CUSTOMER")` annotation at the class level.
5. Create a method `public boolean process(double amount)` and ensure it uses the `REQUIRED` transaction attribute.

```java
package com.globalbank.ejb;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateless
@RolesAllowed("CUSTOMER") // From Session 4
public class PaymentProcessor {

    // From Session 4
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean process(double amount) {
        System.out.println("Processing secure payment of: $" + amount);
        // In a real app, this would use a DataSource (Session 3) to update the DB
        return true; 
    }
}
```

### Step 2: The Conversational EJB (Stateful)

Create a bean that remembers what the user has done during their session. It will use the `PaymentProcessor` internally.

1. Create `UserSessionCart.java`.
2. Annotate it to be a **Stateful** bean.
3. Use dependency injection (`@Inject` or `@EJB`) to bring in the `PaymentProcessor`.
4. Maintain a running total of the user's intended payments.

```java
package com.globalbank.ejb;

import jakarta.ejb.Stateful;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Stateful // From Session 2
public class UserSessionCart {

    private List<Double> pendingPayments = new ArrayList<>();
    private double total = 0.0;
    
    @Inject
    private PaymentProcessor processor; // Combining components!

    public void addPayment(double amount) {
        pendingPayments.add(amount);
        total += amount;
    }

    public double getTotal() {
        return total;
    }

    public String checkout() {
        for (Double amount : pendingPayments) {
            // This calls the Stateless bean, joining its Transaction and Security context
            processor.process(amount);
        }
        pendingPayments.clear();
        double paid = total;
        total = 0.0;
        return "Successfully checked out $" + paid;
    }
}
```

### Step 3: The Web Client (JNDI Lookup)

Create a Servlet that simulates a user visiting the bank website, adding payments to their cart, and checking out.

1. In your Web module, create a Servlet named `CheckoutServlet.java`.
2. Use **JNDI** (Session 3) to look up the `UserSessionCart` Stateful bean.
3. Call the methods and print the output to the browser.

```java
package com.globalbank.web;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.globalbank.ejb.UserSessionCart;

@WebServlet("/Checkout")
public class CheckoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
            
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        
        try {
            // Session 3 TIY Review: JNDI Lookup
            InitialContext ctx = new InitialContext();
            
            // NOTE: Update this JNDI name based on your WildFly deployment logs!
            UserSessionCart cart = (UserSessionCart) ctx.lookup(
                "java:global/GlobalBankApp/GlobalBankEJB/UserSessionCart!com.globalbank.ejb.UserSessionCart"
            );
            
            out.println("<h2>GlobalBank Checkout</h2>");
            
            cart.addPayment(50.0);
            cart.addPayment(120.0);
            
            out.println("<p>Current Total: $" + cart.getTotal() + "</p>");
            out.println("<p>Checking out...</p>");
            
            String result = cart.checkout();
            out.println("<h3>" + result + "</h3>");
            
        } catch (Exception e) {
            out.println("<h3 style='color:red;'>Error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }
}
```

### Step 4: Deployment and Observation in IntelliJ IDEA

![IntelliJ IDEA WildFly Configuration and Deployment](../assets/images/intellij_wildfly_setup.jpg)

#### IntelliJ Navigation Flow:
1. **Open Run Configurations:** At the top right of the IntelliJ toolbar, click the configuration dropdown (next to the green Run button) and choose **Edit Configurations...**
2. **Confirm Deployment Artifact:** Select **WildFly Server > Local**, switch to the **Deployment** tab, and verify that `GlobalBankApp:ear` or `GlobalBankWeb:war` is selected under **Deploy at server startup**.
3. **Run the Server:** Click the green **Play (Run)** icon (or press `Shift + F10`).
4. **Watch the Console:** Open the bottom **Services** tab:
   - Note the JNDI names printed in the console log. Update your Servlet if necessary.
   - Note if WildFly reports `@RolesAllowed` security warnings (if you haven't configured users in `mgmt-users.properties`, temporarily comment out `@RolesAllowed` to test transaction logic).
5. **Open Browser:** Visit:
   ```
   http://localhost:8080/GlobalBankWeb/Checkout
   ```

---

## 👩‍🏫 Instructor Verification

When your application successfully prints the checkout result to the browser window, call your instructor over. Be prepared to answer:

1. **Architecture:** Explain the flow of data from the Servlet → to the Stateful Bean → to the Stateless Bean.
2. **Statefulness:** If you refresh the browser page, what happens to the total? Why? *(Hint: Look closely at how the JNDI lookup is creating a NEW instance of the Stateful bean on every request. How would you fix this in a real app?)*
3. **Transactions:** If the `processor.process()` method threw a `RuntimeException` on the second payment, what would happen to the first payment?
