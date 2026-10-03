# TL19 Class Task: Build and Deploy a Skinny WAR

## 📌 Task Overview

Convert the multi-module EAR from TL18 into a **single Skinny WAR** containing both the EJB and the web tier. Deploy it to WildFly and verify via the context root URL.

---

## 🏗️ Project Structure (Single Maven Module)

```
TL19_SkinnyWAR/
├── pom.xml                           (<packaging>war</packaging>)
└── src/main/
    ├── java/
    │   ├── com/bank/ejb/
    │   │   └── AccountService.java   (@Stateless - lives inside WAR!)
    │   └── com/bank/web/
    │       └── AccountServlet.java   (@WebServlet)
    └── webapp/
        ├── index.xhtml
        └── WEB-INF/
            ├── web.xml
            └── beans.xml
```

---

## 📋 STEP 1 — The EJB (inside the WAR)

```java
package com.bank.ejb;

import jakarta.ejb.Stateless;

@Stateless
public class AccountService {
    public String getAccountHolder(int id) {
        return "Account #" + id + " — John Doe";
    }
}
```

---

## 📋 STEP 2 — The Servlet (same WAR)

```java
package com.bank.web;

import com.bank.ejb.AccountService;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/account")
public class AccountServlet extends HttpServlet {

    @EJB
    private AccountService accountService;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int id = Integer.parseInt(req.getParameter("id"));
        res.getWriter().println(accountService.getAccountHolder(id));
    }
}
```

---

## 📋 STEP 3 — Maven `pom.xml` (Key Sections)

```xml
<packaging>war</packaging>

<dependencies>
    <dependency>
        <groupId>jakarta.platform</groupId>
        <artifactId>jakarta.jakartaee-api</artifactId>
        <version>10.0.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <finalName>bank-app</finalName>
</build>
```

---

## 📋 STEP 4 — Deploy

```bash
mvn clean package
cp target/bank-app.war $WILDFLY_HOME/standalone/deployments/
touch $WILDFLY_HOME/standalone/deployments/bank-app.war.dodeploy
```

---

## ✅ Verification

1. Watch for `bank-app.war.deployed` in the `deployments/` folder.
2. Open: `http://localhost:8080/bank-app/account?id=101`
3. Expected response: `Account #101 — John Doe`
4. Confirm in server logs that the EJB was registered (JNDI bind line visible).
