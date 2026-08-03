# Session 3 — Class Task (TIY - Try It Yourself)
## Resource Creation & JNDI in Jakarta EE

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 3)
**Duration:** 45 minutes

---

## 🎯 Task Objective

In this hands-on task, you will:
1. Build a stateless session EJB that performs a simple text conversion.
2. Build a Java Servlet (web component) to act as a client.
3. Use JNDI in the Servlet to look up the EJB and execute its method.
4. Deploy to WildFly and view the result in your browser.

This task is directly adapted from the **UpperCase JNDI Demo** in your textbook.

---

## 🛠️ Step-by-Step Instructions

### Step 1: Create the Remote Interface

First, we define an interface so the Servlet knows what methods it can call on our EJB.

1. Open your IDE (IntelliJ IDEA) and navigate to your `GlobalBank` project.
2. Create a new package called `demo.jndi`.
3. Create a new Java Interface named `UpperCaseRemote.java`.
4. Add the `@Remote` annotation and define the method:

```java
package demo.jndi;

import jakarta.ejb.Remote;

@Remote
public interface UpperCaseRemote {
    public StringBuffer sayHello();
}
```

### Step 2: Create the Stateless Session Bean

Now, we create the actual bean that implements the business logic (converting text).

1. In the same `demo.jndi` package, create a new Java Class named `UpperCase.java`.
2. Add the `@Stateless` annotation.
3. Implement the `UpperCaseRemote` interface.
4. Write the logic to convert lowercase letters to uppercase and vice versa:

```java
package demo.jndi;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;

@Stateless(mappedName = "Upper")
@LocalBean
public class UpperCase implements UpperCaseRemote {

    public UpperCase() {
        // Required empty constructor
    }

    @Override
    public StringBuffer sayHello() {
        // Hardcoded string for testing
        String string1 = "hello jndi";
        StringBuffer updatesString = new StringBuffer(string1);

        // Loop through each character
        for (int i = 0; i < string1.length(); i++) {
            if (Character.isLowerCase(string1.charAt(i))) {
                // Convert lower to upper
                updatesString.setCharAt(i, Character.toUpperCase(string1.charAt(i)));
            } else if (Character.isUpperCase(string1.charAt(i))) {
                // Convert upper to lower
                updatesString.setCharAt(i, Character.toLowerCase(string1.charAt(i)));
            }
        }

        // Print to the server console so we know it executed
        System.out.println("String processed in EJB : " + updatesString);
        return updatesString;
    }
}
```

### Step 3: Create the Servlet Client (The JNDI Consumer)

Now we will build a web page (Servlet) that looks up our bean using JNDI.

1. Create a new package called `uppercase.jndi` in your web module.
2. Create a new Java Class named `UppercaseDemo.java`.
3. Make it extend `HttpServlet`.
4. Use `InitialContext` to look up the bean!

```java
package uppercase.jndi;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.naming.InitialContext;
import jakarta.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import demo.jndi.UpperCaseRemote;

@WebServlet("/UppercaseDemo")
public class UppercaseDemo extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public UppercaseDemo() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // ① Connect to the JNDI directory
            InitialContext context = new InitialContext();

            // ② Look up the Bean!
            // Format: java:global/[app-name]/[module-name]/[bean-name]![interface]
            // Note: Replace "GlobalBankApp" and "GlobalBankEJB" with your actual project names if they differ.
            UpperCaseRemote remoteBn = (UpperCaseRemote) context.lookup(
                "java:global/GlobalBankApp/GlobalBankEJB/UpperCase!demo.jndi.UpperCaseRemote"
            );

            // ③ Execute the business logic
            StringBuffer str = remoteBn.sayHello();

            // ④ Send the result to the browser
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();
            out.print("<h1>JNDI Lookup Result</h1>");
            out.print("<p>Original: <b>hello jndi</b></p>");
            out.print("<p>Converted: <b>" + str + "</b></p>");

        } catch (NamingException e) {
            e.printStackTrace();
            response.getWriter().print("Error looking up bean: " + e.getMessage());
        }
    }
}
```

### Step 4: Deploy and Test

1. Build your project and deploy the EAR file to WildFly.
2. Open the WildFly server log. Ensure you see a line stating the JNDI bindings for `UpperCase` were successful. It should look something like:
   `java:global/GlobalBankApp/GlobalBankEJB/UpperCase!demo.jndi.UpperCaseRemote`
3. Open your web browser.
4. Navigate to: `http://localhost:8080/GlobalBankWeb/UppercaseDemo` *(Adjust URL if your web context root is different).*

**Expected Output on Screen:**
```
JNDI Lookup Result
Original: hello jndi
Converted: HELLO JNDI
```

---

## 👩‍🏫 Instructor Verification

Call your instructor over to verify your work. Be prepared to answer the following questions:

1. **Point to the code:** Show me exactly where the connection to the JNDI directory is opened.
2. **Explain:** Why do we have to cast `(UpperCaseRemote)` when calling `context.lookup()`?
3. **Troubleshoot:** If the `NamingException` catch block executes, what is the most likely mistake you made in your code?

---

## 🆘 Common Errors & Troubleshooting

- **Error:** `javax.naming.NameNotFoundException`
  - **Why:** The string inside `context.lookup("")` is incorrect.
  - **Fix:** Check your WildFly startup logs. WildFly prints the exact JNDI name it assigned to your bean. Copy and paste it exactly. Remember, JNDI names are CASE-SENSITIVE!

- **Error:** `ClassCastException`
  - **Why:** You looked up the right JNDI name, but tried to cast it to the wrong interface type.
  - **Fix:** Ensure the interface at the end of the JNDI name matches the cast `(UpperCaseRemote)`.
