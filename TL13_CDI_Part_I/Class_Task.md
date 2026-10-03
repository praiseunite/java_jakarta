# Session 11 Class Task: Implementing CDI Scopes and Injection

## 📌 Task Overview

In this hands-on lab, you will configure a CDI application and observe how different CDI scopes behave during a user session. You will build a simple web application with multiple beans mapped to different lifecycle scopes, injecting them into a frontend controller.

You will:
1. **Create the `beans.xml`** file to enable CDI discovery.
2. **Implement beans with different scopes** (`@ApplicationScoped`, `@SessionScoped`, `@RequestScoped`).
3. **Use `@Inject`** to wire the beans into a JSF Backing Bean and observe when new instances are created versus reused.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 installed.
* **Jakarta EE Application Server:** WildFly 30+ (or Payara/GlassFish).
* **IDE:** IntelliJ IDEA or Eclipse (configured for a Web/WAR project).

---

## 🏗️ Project Architecture

```
Session_11_CDI_Lab/
├── pom.xml
└── src/main/
    ├── java/com/cdi/demo/
    │   ├── GlobalCounter.java     (@ApplicationScoped)
    │   ├── UserCart.java          (@SessionScoped)
    │   ├── RequestTracker.java    (@RequestScoped)
    │   └── StoreController.java   (JSF Backing Bean)
    └── webapp/
        ├── WEB-INF/beans.xml      (CDI Configuration)
        └── index.xhtml            (JSF View)
```

---

## 📋 STEP 1 — Configure CDI (`beans.xml`)

Create the `WEB-INF/beans.xml` file to enable CDI in your WAR project. Set the discovery mode to `annotated`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                           https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0"
       bean-discovery-mode="annotated">
</beans>
```

---

## 📋 STEP 2 — Create the Scoped Beans

Create the three beans that track a simple counter. Each will have a different lifecycle scope.

### 1. The Application Scoped Bean
This bean lives for the entire server uptime and is shared among all users.

```java
package com.cdi.demo;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalCounter {
    private int count = 0;
    
    public void increment() { count++; }
    public int getCount() { return count; }
}
```

### 2. The Session Scoped Bean
This bean lives for the duration of a specific user's HTTP session. **It must be Serializable.**

```java
package com.cdi.demo;

import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;

@SessionScoped
public class UserCart implements Serializable {
    private int items = 0;
    
    public void addItem() { items++; }
    public int getItems() { return items; }
}
```

### 3. The Request Scoped Bean
This bean is created fresh for every single HTTP request.

```java
package com.cdi.demo;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class RequestTracker {
    private int reqId = (int) (Math.random() * 1000);
    
    public int getReqId() { return reqId; }
}
```

---

## 📋 STEP 3 — Create the Controller and View

Create a JSF backing bean to inject all three scopes, and a simple view to display them.

### The JSF Controller (`StoreController.java`)

```java
package com.cdi.demo;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class StoreController {

    @Inject
    private GlobalCounter globalCounter;

    @Inject
    private UserCart userCart;

    @Inject
    private RequestTracker requestTracker;

    public void action() {
        globalCounter.increment();
        userCart.addItem();
    }

    public GlobalCounter getGlobalCounter() { return globalCounter; }
    public UserCart getUserCart() { return userCart; }
    public RequestTracker getRequestTracker() { return requestTracker; }
}
```

### The JSF View (`index.xhtml`)

```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html">
<h:head>
    <title>CDI Scopes Lab</title>
</h:head>
<h:body>
    <h2>CDI Scopes Demonstration</h2>
    
    <h:form>
        <p><strong>Request Tracker ID (@RequestScoped):</strong> #{storeController.requestTracker.reqId}</p>
        <p><strong>User Cart Items (@SessionScoped):</strong> #{storeController.userCart.items}</p>
        <p><strong>Global Total Actions (@ApplicationScoped):</strong> #{storeController.globalCounter.count}</p>
        
        <h:commandButton value="Perform Action" action="#{storeController.action}" />
    </h:form>
</h:body>
</html>
```

---

## ✅ Execution & Verification

1. Deploy the application and open `index.xhtml` in a browser.
2. Click the "Perform Action" button multiple times.
   * **Observe:** The Request Tracker ID changes every time (fresh instance).
   * **Observe:** The User Cart and Global Total Actions both increase.
3. Open a **different browser** (or Incognito mode) and access the page.
   * **Observe:** The Request Tracker ID is new.
   * **Observe:** The User Cart is back to `0` (new session instance).
   * **Observe:** The Global Total Actions continues from where the previous browser left off (shared instance).
