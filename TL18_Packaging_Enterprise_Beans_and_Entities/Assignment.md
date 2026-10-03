# Session 14 Assignment: Skinny WAR Modernization

## 📝 Assignment Overview

This assignment evaluates your ability to modernize a legacy application package. While multi-module EAR files were standard in older Java EE versions, modern cloud-native Jakarta applications strongly favor the simpler "Skinny WAR" architecture.

Based on the textbook **Try It Yourself** requirement:
> *"Repackage an existing multi-tier EAR application into a single standalone Web Application Archive (WAR)."*

You will refactor the structure of a simulated legacy application into a streamlined WAR deployment.

---

## 🏢 Business Scenario

The DevOps team at Global Bank is migrating their architecture from heavy monolithic servers to lightweight Docker containers running WildFly. They have requested that developers stop deploying EAR files unless absolutely necessary.

Your task is to take the architecture from the Class Task (which used an EAR containing a JAR and a WAR) and flatten it into a single Skinny WAR that still performs identically.

---

## 📋 Specific Requirements

### 1. The Single Module Project
Create a new Maven project named `Session_14_Skinny_WAR`.
It must be a single module, not a parent with sub-modules. The packaging type in `pom.xml` must be `<packaging>war</packaging>`.

### 2. Combine the Code
Move the `GreetingBean` (from the EJB module) and the `GreetingServlet` (from the Web module) into the same `src/main/java` source tree.
* Ensure they retain their packages: `com.bank.ejb` and `com.bank.web`.

### 3. Simplify Configuration
* Delete `application.xml` entirely. It is not needed for a WAR.
* If you have `beans.xml`, place a single copy in `src/main/webapp/WEB-INF/beans.xml`.

### 4. Build and Deploy
* Run `mvn clean package`.
* Verify that `Session_14_Skinny_WAR.war` is generated.
* Deploy this WAR to WildFly.

---

## 📂 Expected Directory Structure (Before vs After)

**Legacy (EAR) - DO NOT SUBMIT THIS:**
```
legacy-app/
├── ejb-module/src/main/java/com/bank/ejb/GreetingBean.java
├── web-module/src/main/java/com/bank/web/GreetingServlet.java
└── ear-module/src/main/application/META-INF/application.xml
```

**Modern (Skinny WAR) - SUBMIT THIS STRUCTURE:**
```
Session_14_Skinny_WAR/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── com/bank/ejb/GreetingBean.java
    │   └── com/bank/web/GreetingServlet.java
    └── webapp/
        └── WEB-INF/
            └── beans.xml
```

## 💯 Grading Criteria

1. **Project Simplification (30%)**: The project is successfully consolidated into a single Maven WAR module without multi-module parent POMs.
2. **Artifact Packaging (40%)**: When built, the WAR file correctly contains the compiled classes for *both* the EJB and the Servlet inside `WEB-INF/classes`.
3. **Descriptor Cleanup (30%)**: The `application.xml` file is completely removed, and the server successfully infers the web context root from the WAR file name.
