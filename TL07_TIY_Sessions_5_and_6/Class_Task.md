# TL07 Class Task: Facelets Template + Local EJB Integration

## 📌 Task Overview

This class task consolidates your understanding of both Session 5 (Facelets) and Session 6 (Local/Remote Clients) by building a single integrated mini-application.

You will:
1. Build a **Facelets master template** layout.
2. Create a JSF page that uses the template and calls a **Local EJB** to load data.
3. Observe lifecycle phases using a PhaseListener.

---

## 🏗️ Project Structure

```
TL07_Integrated_Lab/
└── src/main/
    ├── java/com/bank/
    │   ├── ejb/AccountSummaryBean.java   (@Stateless @Local)
    │   ├── web/DashboardController.java  (JSF @Named @RequestScoped)
    │   └── listener/LifecycleLogger.java (@PhaseListener)
    └── webapp/
        ├── WEB-INF/
        │   └── templates/
        │       └── masterLayout.xhtml    (Facelets Template)
        └── dashboard.xhtml               (Page using template)
```

---

## 📋 STEP 1 — The Local EJB

```java
package com.bank.ejb;

import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.Arrays;

@Stateless
@Local
public class AccountSummaryBean {
    public List<String> getRecentTransactions() {
        return Arrays.asList(
            "Transfer to Alice: -$250.00",
            "Salary Credit: +$3,500.00",
            "Utility Bill: -$120.00"
        );
    }

    public double getCurrentBalance() {
        return 12_430.55;
    }
}
```

---

## 📋 STEP 2 — The Facelets Master Template

```xml
<!-- webapp/WEB-INF/templates/masterLayout.xhtml -->
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="jakarta.faces.facelets"
      xmlns:h="jakarta.faces.html">
<h:head>
    <title><ui:insert name="pageTitle">Default Title</ui:insert></title>
</h:head>
<h:body>
    <header><h1>Global Bank Portal</h1></header>
    <main>
        <ui:insert name="content"><!-- Page content here --></ui:insert>
    </main>
    <footer><p>© 2024 Global Bank</p></footer>
</h:body>
</html>
```

---

## 📋 STEP 3 — The Dashboard Page

```xml
<!-- webapp/dashboard.xhtml -->
<ui:composition template="/WEB-INF/templates/masterLayout.xhtml"
                xmlns:ui="jakarta.faces.facelets"
                xmlns:h="jakarta.faces.html">

    <ui:define name="pageTitle">Account Dashboard</ui:define>

    <ui:define name="content">
        <h2>Welcome, #{dashboardController.username}</h2>
        <p>Balance: $#{dashboardController.balance}</p>
        <h3>Recent Transactions</h3>
        <ul>
            <ui:repeat value="#{dashboardController.transactions}" var="tx">
                <li>#{tx}</li>
            </ui:repeat>
        </ul>
    </ui:define>

</ui:composition>
```

---

## 📋 STEP 4 — The JSF Backing Bean (Local EJB Injection)

```java
package com.bank.web;

import com.bank.ejb.AccountSummaryBean;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class DashboardController {

    @EJB  // Local injection - same JVM
    private AccountSummaryBean accountBean;

    public String getUsername() { return "john_doe"; }
    public double getBalance() { return accountBean.getCurrentBalance(); }
    public List<String> getTransactions() { return accountBean.getRecentTransactions(); }
}
```

---

## ✅ Verification

1. Deploy and open `http://localhost:8080/app/dashboard.xhtml`.
2. Verify the master template header/footer is rendering.
3. Verify the balance and transactions are loaded from the **Local EJB**.
4. Add a `PhaseListener` as a stretch goal and observe lifecycle phase names in the server log.
