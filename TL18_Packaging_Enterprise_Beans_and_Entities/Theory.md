# Session 14: Packaging Jakarta Applications

Welcome to **Session 14: Packaging Jakarta Applications**. Writing code is only the first step. To run in production, enterprise components must be packaged into standardized archives and deployed to an application server like WildFly. Session 14 covers the anatomy of **JAR**, **WAR**, **RAR**, and **EAR** bundles, the modern **Skinny WAR** paradigm, deployment automation, and classloading architecture.

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Identify** the four standard Jakarta EE packaging archives: `.jar`, `.war`, `.rar`, and `.ear`.
2. **Deconstruct** the internal layout of an Enterprise Archive (`.ear`) and configure `application.xml`.
3. **Explain** the modern "Skinny WAR" architecture and when to choose a standalone WAR over an EAR.
4. **Deploy** applications to WildFly via CLI, Admin GUI, and file marker automation (`.dodeploy`).
5. **Master** classloading isolation, parent-first vs. child-first delegation, and `jboss-deployment-structure.xml`.

---

## 1. The Four Standard Jakarta EE Archive Types

| Archive | Full Name | Primary Contents | Typical Deployment Role |
| :--- | :--- | :--- | :--- |
| **`.jar`** | Java Archive | Compiled `.class` files, EJB session beans, JPA entities, `META-INF/beans.xml` | Reusable business logic libraries or standalone EJB modules. |
| **`.war`** | Web Application Archive | Servlets, JSF Facelets, static web assets, `WEB-INF/web.xml`, `WEB-INF/classes/` | Web user interface tier and RESTful API endpoints. |
| **`.rar`** | Resource Adapter Archive | JCA connector classes, native socket libraries, `META-INF/ra.xml` | Pluggable enterprise connectors (e.g., SAP, IBM CICS adapters). |
| **`.ear`** | Enterprise Application Archive | Contains multiple WARs, EJB JARs, RARs, shared `lib/`, and `META-INF/application.xml` | Top-level enterprise suite bundling web and business tiers into one deployment. |

---

## 2. The Enterprise Archive (`.ear`) & `application.xml`

An Enterprise Archive brings multiple tiers together under a single coordinated deployment context:

### Physical Structure of `GlobalBank.ear`
```
GlobalBank.ear
├── META-INF/
│   ├── application.xml        <!-- Master deployment descriptor -->
│   └── MANIFEST.MF
├── lib/                       <!-- Shared third-party libraries (accessible to all modules) -->
│   ├── commons-lang3.jar
│   └── postgresql-driver.jar
├── GlobalBank-EJB.jar         <!-- Business logic tier -->
│   ├── com/globalbank/ejb/AccountBean.class
│   └── META-INF/beans.xml
└── GlobalBank-Web.war         <!-- Presentation tier -->
    ├── login.xhtml
    ├── index.xhtml
    └── WEB-INF/
        ├── web.xml
        └── beans.xml
```

### `META-INF/application.xml` — Master Application Descriptor
```xml
<?xml version="1.0" encoding="UTF-8"?>
<application xmlns="https://jakarta.ee/xml/ns/jakartaee" version="10">
    <application-name>GlobalBankApp</application-name>

    <!-- 1. Web Module Definition with Context Root -->
    <module>
        <web>
            <web-uri>GlobalBank-Web.war</web-uri>
            <context-root>/globalbank</context-root>
        </web>
    </web>

    <!-- 2. EJB Business Module -->
    <module>
        <ejb>GlobalBank-EJB.jar</ejb>
    </module>

    <!-- 3. Shared Library Directory -->
    <library-directory>lib</library-directory>
</application>
```

---

## 3. The Modern "Skinny WAR" Paradigm

Since the introduction of EJB 3.1, **Enterprise Beans (EJBs) can be packaged directly inside a WAR**. You no longer need to build an EAR unless you have complex multi-module requirements:

| Architecture Model | Package Format | When to Choose |
| :--- | :--- | :--- |
| **All-in-One WAR** *(Modern)* | Single `.war` file containing EJBs, JSF, and REST in `WEB-INF/classes` | Ideal for modern cloud deployments, Docker containers, and microservice architectures. Simpler Maven build. |
| **Multi-Module EAR** *(Traditional)* | `.ear` containing separate `.war` and `.jar` modules | Necessary when multiple separate web frontends (e.g., Customer Portal WAR + Admin Portal WAR) share the exact same EJB business JAR. |

---

## 4. WildFly Deployment Mechanisms & Status Markers

WildFly supports three standard mechanisms for deploying archives:

1. **Management CLI (Production Best Practice):**
   `$WILDFLY_HOME/bin/jboss-cli.sh --connect --command="deploy /build/GlobalBank.ear"`
2. **Admin Web Console:** Upload via `http://localhost:9990/console` under *Deployments $\to$ Add*.
3. **File-System Auto-Deployment Scanner:** Copy the archive into `$WILDFLY_HOME/standalone/deployments/`.

### WildFly Deployment Marker Files
When using file-system deployment, WildFly creates empty marker files in the `deployments/` directory to report deployment status:

| Marker File | Status Meaning |
| :--- | :--- |
| `myapp.ear.dodeploy` | User creates this file to trigger WildFly to start deploying. |
| `myapp.ear.isdeploying` | WildFly is currently unpacking and parsing the archive. |
| `myapp.ear.deployed` | Deployment succeeded! Application is active and ready to accept traffic. |
| `myapp.ear.failed` | Deployment failed! Contains the stack trace describing why. |
| `myapp.ear.undeployed` | Application was successfully undeployed and stopped. |

---

## 5. Summary

* **JAR, WAR, RAR:** Module-level archives: JAR for EJBs, WAR for Web UI/REST, RAR for JCA adapters.
* **EAR Archive:** Top-level enterprise bundle containing WARs and JARs coordinated via `application.xml`.
* **Skinny WAR:** Modern practice packaging EJBs, REST, and JSF into one single deployable WAR.
* **Shared Libraries:** Stored inside the EAR's `lib/` folder and accessible across all packaged modules.
* **Deployment Markers:** WildFly uses `.dodeploy`, `.isdeploying`, `.deployed`, and `.failed` status flags.
