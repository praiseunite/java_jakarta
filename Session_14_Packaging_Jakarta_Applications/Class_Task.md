# Session 14 Class Task: Building and Deploying an EAR

## 📌 Task Overview

In this hands-on lab, you will construct a traditional multi-module Enterprise Archive (EAR). You will create a separate EJB JAR module, a WAR module, and package them together using an `application.xml` descriptor. Finally, you will simulate deploying it to WildFly using deployment markers.

You will:
1. **Create an EJB JAR** containing a simple Stateless Session Bean.
2. **Create a WAR** containing a servlet that calls the EJB.
3. **Assemble the EAR** with `application.xml`.
4. **Deploy** to a local WildFly server using the `.dodeploy` marker.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17.
* **Jakarta EE Application Server:** WildFly 30+ downloaded and extracted.
* **IDE:** IntelliJ IDEA, Eclipse, or raw Maven from the CLI.

---

## 🏗️ Project Architecture (Maven Multi-Module)

```
Session_14_EAR_Lab/
├── pom.xml                      (Parent POM)
├── ejb-module/                  (The JAR)
│   ├── pom.xml
│   └── src/main/java/com/bank/ejb/
│       └── GreetingBean.java
├── web-module/                  (The WAR)
│   ├── pom.xml
│   └── src/main/java/com/bank/web/
│       └── GreetingServlet.java
└── ear-module/                  (The EAR)
    ├── pom.xml
    └── src/main/application/
        └── META-INF/
            └── application.xml
```

---

## 📋 STEP 1 — The EJB Module (JAR)

Create a standard Stateless EJB in the `ejb-module`.

```java
package com.bank.ejb;

import jakarta.ejb.Stateless;

@Stateless
public class GreetingBean {
    public String getGreeting() {
        return "Hello from the Enterprise Layer packaged inside a JAR!";
    }
}
```

---

## 📋 STEP 2 — The Web Module (WAR)

Create a Servlet in the `web-module` that injects and calls the EJB.

```java
package com.bank.web;

import com.bank.ejb.GreetingBean;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/greet")
public class GreetingServlet extends HttpServlet {

    @Inject
    private GreetingBean greetingBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().println(greetingBean.getGreeting());
    }
}
```

---

## 📋 STEP 3 — The EAR Assembly

Configure the `ear-module` to package both the JAR and the WAR. Define the context root for the web module inside `application.xml`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<application xmlns="https://jakarta.ee/xml/ns/jakartaee" version="10">
    <application-name>GreetingEnterpriseApp</application-name>

    <module>
        <ejb>ejb-module.jar</ejb>
    </module>
    
    <module>
        <web>
            <web-uri>web-module.war</web-uri>
            <context-root>/hello-app</context-root>
        </web>
    </module>
</application>
```

---

## 📋 STEP 4 — Build and Deploy

1. Build the multi-module project using Maven:
   ```bash
   mvn clean package
   ```
2. Locate the generated EAR file in `ear-module/target/GreetingEnterpriseApp.ear`.
3. Copy this file to your WildFly server:
   ```bash
   cp GreetingEnterpriseApp.ear $WILDFLY_HOME/standalone/deployments/
   ```
4. If WildFly is not set to auto-deploy, manually trigger it by creating the marker file:
   ```bash
   touch $WILDFLY_HOME/standalone/deployments/GreetingEnterpriseApp.ear.dodeploy
   ```

---

## ✅ Verification

1. Watch the WildFly server logs. You should see it detect the `.dodeploy` marker, parse `application.xml`, start the EJB subdeployment, and bind the web context.
2. Check the `deployments/` folder. Ensure `GreetingEnterpriseApp.ear.deployed` exists.
3. Open a browser and navigate to: `http://localhost:8080/hello-app/greet`.
4. Verify the response: "Hello from the Enterprise Layer packaged inside a JAR!"
