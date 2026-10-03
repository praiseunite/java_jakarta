# TL19 — Packaging Web Archives (Session 14, Part 2)

**Manual Reference:** JAKARTA EE – TL19  
**Book Coverage:** Session 14 — Packaging Web Archives (WAR)

TL18 covered packaging Enterprise Beans (EJB JARs) and JPA Entities. TL19 focuses specifically on **Web Application Archives (WAR)** — their internal structure, deployment descriptors, context roots, the modern Skinny WAR pattern, and WildFly-specific WAR deployment.

---

## 🎯 Learning Objectives

1. **Describe** the internal layout of a Web Application Archive (`.war`).
2. **Elaborate** the structure of `web.xml` and `WEB-INF/` contents.
3. **Explain** context roots and how they map to application URLs.
4. **Contrast** the classic WAR-inside-EAR vs. the modern standalone Skinny WAR.
5. **Deploy** a WAR to WildFly using file markers and the CLI.

---

## 1. Anatomy of a WAR File

```
MyBankWeb.war
├── login.xhtml                   ← Public JSF page
├── dashboard.xhtml               ← Protected JSF page
├── css/styles.css                ← Static resources
├── WEB-INF/
│   ├── web.xml                   ← Web deployment descriptor
│   ├── beans.xml                 ← CDI discovery enabler
│   ├── faces-config.xml          ← JSF navigation config (optional)
│   ├── classes/                  ← Compiled Java classes
│   │   └── com/bank/web/
│   │       └── LoginController.class
│   └── lib/                      ← Third-party JARs used ONLY by this WAR
│       └── some-utility.jar
└── META-INF/
    └── MANIFEST.MF
```

> **Key Rule:** Files under `WEB-INF/` are **never directly accessible** to the browser. Only files at the WAR root or in mapped servlet paths are publicly accessible.

---

## 2. The Web Deployment Descriptor (`web.xml`)

The `web.xml` configures servlets, filters, security constraints, listeners, session timeouts, and welcome files:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
                             https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">

    <!-- JSF Servlet Registration -->
    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

    <!-- Session Timeout: 30 minutes -->
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>

    <!-- Welcome File -->
    <welcome-file-list>
        <welcome-file>index.xhtml</welcome-file>
    </welcome-file-list>

    <!-- Security Constraint -->
    <security-constraint>
        <web-resource-collection>
            <web-resource-name>Secure Area</web-resource-name>
            <url-pattern>/secured/*</url-pattern>
        </web-resource-collection>
        <auth-constraint>
            <role-name>TELLER</role-name>
        </auth-constraint>
    </security-constraint>
</web-app>
```

---

## 3. Context Roots — How URLs Map to WARs

The **context root** is the URL prefix that routes incoming HTTP requests to a specific WAR:

| Deployment | Context Root | Accessible At |
|---|---|---|
| `MyBankWeb.war` (filename default) | `/MyBankWeb` | `http://localhost:8080/MyBankWeb/` |
| Configured in `application.xml` (EAR) | `/globalbank` | `http://localhost:8080/globalbank/` |
| `jboss-web.xml` override | `/` | `http://localhost:8080/` (root app) |

**`WEB-INF/jboss-web.xml`** — WildFly-specific override:
```xml
<jboss-web>
    <context-root>/bank</context-root>
</jboss-web>
```

---

## 4. Skinny WAR — The Modern Cloud Pattern

Since EJB 3.1 (Java EE 6), EJBs can live directly inside a WAR under `WEB-INF/classes/`. This **eliminates the need for a separate EJB JAR and EAR**.

| Classical EAR Layout | Skinny WAR Layout |
|---|---|
| `MyApp.ear` contains `MyBiz.jar` + `MyWeb.war` | Single `MyApp.war` with EJBs in `WEB-INF/classes/` |
| Requires `application.xml` | No EAR descriptor needed |
| Complex Maven multi-module | Single Maven module |

### When MUST you still use an EAR?
Only when **multiple WARs share the same EJB JAR** — e.g., a Customer Portal WAR and an Admin Portal WAR both calling the same business JAR.

---

## 5. Deploying WARs to WildFly

Same deployment mechanisms as EAR files:

```bash
# CLI Deployment (Production)
$WILDFLY_HOME/bin/jboss-cli.sh --connect \
  --command="deploy /builds/MyBankWeb.war"

# File-System Marker Deployment
cp MyBankWeb.war $WILDFLY_HOME/standalone/deployments/
touch $WILDFLY_HOME/standalone/deployments/MyBankWeb.war.dodeploy

# Check status
ls $WILDFLY_HOME/standalone/deployments/*.deployed
# → MyBankWeb.war.deployed  (Success!)
```

---

## 6. Summary

* **WAR Layout:** Public resources at root; `WEB-INF/` is server-private (classes, lib, web.xml, beans.xml).
* **`web.xml`:** Configures Servlets, security constraints, session timeouts, and welcome files.
* **Context Root:** Determines the URL prefix — set by filename, `application.xml`, or `jboss-web.xml`.
* **Skinny WAR:** The modern approach — EJBs packaged inside a single WAR, no EAR needed.
* **WildFly Deployment:** Via CLI, Admin Console, or `standalone/deployments/` with `.dodeploy` marker.
