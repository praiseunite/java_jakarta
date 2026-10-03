# Enterprise Application Development in Jakarta EE

> **Official Course Curriculum Alignment**  
> **Course Manual:** Enterprise Application Development in Jakarta EE (Issue Date: August 2024, Ver 1.0)  
> **Total Duration:** 40 Hours Theory/Lab (20 Sessions: TL1–TL20) + 8 Hours Self-Study (S1–S8)  
> **Platform Target:** Jakarta EE 10 / WildFly 30+ / Java 17+ / Maven 3.9+

---

## 🏛️ Course Overview

This repository provides the complete, production-ready curriculum materials for **Enterprise Application Development in Jakarta EE**. It is strictly organized according to the official Aptech Presenter's Manual and Course Guide, covering foundational architecture through modern cloud-native enterprise design.

The course features dual learning modalities:
1. **Markdown Session Guides** — Detailed session folders (`TL01` through `TL20`, plus Self-Study blocks) containing in-depth `Theory.md`, hands-on `Class_Task.md`, and evaluated `Assignment.md` modules.
2. **Interactive Offline Web Course** — Located in [`beginner-course/`](beginner-course/index.html), featuring interactive runnable code simulations, browser-based administrative terminals, live quizzes, and dark/light mode progress tracking.

---

## 📅 Complete Course Schedule & Session Mapping

The curriculum spans **5 weeks**, **20 direct instruction sessions (TL1–TL20)**, and **2 self-study assignment blocks (S1–S8)** covering all **14 Book Sessions**:

| Week | Session ID | Book Session | Topic & Coverage | Repository Directory | Interactive Web Unit |
|:---:|:---:|:---:|---|---|:---:|
| **Week 1** | **TL01** | Session 1 & 2 | Introduction to Jakarta EE Architecture, Component Model, and Session Beans (`@Stateless`, `@Stateful`, `@Singleton`) | [`TL01_Introduction_and_Session_Beans/`](TL01_Introduction_and_Session_Beans/) | `s01-02` |
| | **TL02** | Session 3 | Resource Creation in Jakarta Enterprise Beans, JNDI Naming (`java:global`, `java:app`, `java:module`), `@Resource` Injection | [`TL02_Resource_Creation_and_JNDI/`](TL02_Resource_Creation_and_JNDI/) | `s03` |
| | **TL03** | Session 4 | Working with Jakarta Enterprise Beans: Container-Managed Transactions (CMT), Concurrency Management (`@Lock`), and Declarative Security (`@RolesAllowed`) | [`TL03_Transactions_Concurrency_Security/`](TL03_Transactions_Concurrency_Security/) | `s04` |
| | **TL04** | Review (S1–S4) | **Try It Yourself:** Consolidation Lab covering Architecture, Lifecycle, Concurrency, and Transactions across Sessions 1 to 4 | [`TL04_TIY_Sessions_1_to_4/`](TL04_TIY_Sessions_1_to_4/) | `s04t` |
| **Week 2** | **TL05** | Session 5 | Facelets in Jakarta EE: Templating (`<ui:composition>`, `<ui:define>`), Custom Composite Components, and JSF Lifecycle Phases | [`TL05_Facelets_in_Jakarta_EE/`](TL05_Facelets_in_Jakarta_EE/) | `s05` |
| | **TL06** | Session 6 | Jakarta Local and Remote Clients: `@Local` vs `@Remote` interfaces, Pass-by-Reference vs Pass-by-Value serialization, JNDI lookup | [`TL06_Jakarta_Local_and_Remote_Clients/`](TL06_Jakarta_Local_and_Remote_Clients/) | `s06` |
| | **TL07** | Review (S5–S6) | **Try It Yourself:** Integration Lab connecting Facelets frontend templates to Local/Remote Enterprise Beans | [`TL07_TIY_Sessions_5_and_6/`](TL07_TIY_Sessions_5_and_6/) | `s07t` |
| | **TL08** | Session 7 | Jakarta Messaging Services (JMS): Point-to-Point (Queues), Publish/Subscribe (Topics), Message-Driven Beans (`@MessageDriven`) | [`TL08_Jakarta_Messaging_Services/`](TL08_Jakarta_Messaging_Services/) | `s07` |
| **Week 3** | **TL09** | Session 8 | Understanding Jakarta Connectors Architecture (JCA): Resource Adapters, System Contracts (Connection, Transaction, Work Management), EIS Integration | [`TL09_Jakarta_Connectors_Architecture/`](TL09_Jakarta_Connectors_Architecture/) | `s08` |
| | **TL10** | Review (S7–S8) | **Try It Yourself:** Messaging and Connectors Lab combining JMS Producer/Consumer flows with JCA JDBC DataSources | [`TL10_TIY_Sessions_7_and_8/`](TL10_TIY_Sessions_7_and_8/) | `s08t` |
| | **S1–S4** | Self-Study (S1–S8) | **Self-Study Block 1:** OnlineVarsity Work Assignments and Lab Exercises for Book Sessions 1 through 8 | [`Self_Study_S1_S4_Assignments_Sessions_1_to_8/`](Self_Study_S1_S4_Assignments_Sessions_1_to_8/) | — |
| | **TL11 (Part 1)** | Session 9 | Jakarta Bean Validation: Built-in Constraints (`@NotNull`, `@Size`, `@Pattern`), Custom Constraint Validators, Class-level validation | [`TL11_Part1_Bean_Validation/`](TL11_Part1_Bean_Validation/) | `s09` |
| | **TL11 (Part 2)** | Session 10 | Enterprise Beans Transactions & JNDI: Bean-Managed Transactions (BMT), `UserTransaction` demarcation, 2-Phase Commit | [`TL11_Part2_EJB_Transactions_and_JNDI/`](TL11_Part2_EJB_Transactions_and_JNDI/) | `s10` |
| | **TL12** | Review (S9–S10) | **Try It Yourself:** Validation & Transaction Integration Lab with rollback enforcement | [`TL12_TIY_Sessions_9_and_10/`](TL12_TIY_Sessions_9_and_10/) | `s10t` |
| **Week 4** | **TL13** | Session 11 | Jakarta Contexts and Dependency Injection (CDI) Part I: Core Injection (`@Inject`), Scopes (`@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`, `@ConversationScoped`) | [`TL13_CDI_Part_I/`](TL13_CDI_Part_I/) | `s11` |
| | **TL14** | Session 12 | Jakarta Contexts and Dependency Injection (CDI) Part II: Stereotypes, EL Resolution, Bean Discovery Modes, Lifecycle Callbacks | [`TL14_CDI_Part_II/`](TL14_CDI_Part_II/) | `s12` |
| | **TL15** | Review (S11–S12) | **Try It Yourself:** CDI Scopes, Lifecycles, and Web Integration Lab | [`TL15_TIY_Sessions_11_and_12/`](TL15_TIY_Sessions_11_and_12/) | `s12t` |
| | **TL16** | Session 13 (Part 1) | CDI Beans: Custom Qualifiers (`@Qualifier`), Producer Methods & Fields (`@Produces`), Interceptors (`@AroundInvoke`), and Events (`@Observes`) | [`TL16_CDI_Beans_Qualifiers_Producers_Interceptors_Events/`](TL16_CDI_Beans_Qualifiers_Producers_Interceptors_Events/) | `s13` |
| **Week 5** | **TL17** | Session 13 (Part 2) | CDI Beans & Jakarta Security: Declarative RBAC (`@RolesAllowed`), Programmatic `SecurityContext`, Identity Stores, Authentication Mechanisms | [`TL17_CDI_Beans_Security_and_Authentication/`](TL17_CDI_Beans_Security_and_Authentication/) | `s13` |
| | **TL18** | Session 14 (Part 1) | Packaging Enterprise Beans and Entities: Multi-module Maven architectures, EJB JARs, JPA Persistence Archives (`persistence.xml`), EAR assembly (`application.xml`) | [`TL18_Packaging_Enterprise_Beans_and_Entities/`](TL18_Packaging_Enterprise_Beans_and_Entities/) | `s14` |
| | **TL19** | Session 14 (Part 2) | Packaging Web Archives: Skinny WAR architectures, `beans.xml` discovery, Web Context Root customization, and Deployment Descriptors | [`TL19_Packaging_Web_Archives/`](TL19_Packaging_Web_Archives/) | `s14` |
| | **TL20** | Review (S13–S14) | **Try It Yourself:** Capstone Enterprise Integration Lab combining CDI, Security, and multi-tier Packaging | [`TL20_TIY_Sessions_13_and_14/`](TL20_TIY_Sessions_13_and_14/) | `s14t` |
| | **S5–S8** | Self-Study (S9–S14) | **Self-Study Block 2:** OnlineVarsity Work Assignments and Lab Exercises for Book Sessions 9 through 14 | [`Self_Study_S5_S8_Assignments_Sessions_9_to_14/`](Self_Study_S5_S8_Assignments_Sessions_9_to_14/) | — |

---

## 📂 Repository Directory Layout

Each instructional session folder is structured uniformly:
```
TLxx_Session_Name/
├── Theory.md        # Comprehensive lecture notes, diagrams, container mechanisms, code samples
├── Class_Task.md    # Guided in-class lab with step-by-step instructions, expected outputs, and solution code
└── Assignment.md    # Graded take-home practical problem with business context and evaluation criteria
```

Self-Study folders (`Self_Study_S1_S4...` and `Self_Study_S5_S8...`) contain exhaustive checklists for all OnlineVarsity work assignments with acceptance rubrics.

---

## 🌐 Offline Interactive Web Course

To open the interactive browser companion:
1. Navigate to the [`beginner-course/`](beginner-course/) directory.
2. Open [`index.html`](beginner-course/index.html) in any modern web browser (Google Chrome, Firefox, Edge, Safari).
3. **Features:**
   - ⚡ **100% Offline** — No backend server, Node.js, or internet connection required.
   - 💻 **Simulated Container Shell** — Run simulated WildFly CLI commands (`jboss-cli.bat`) and Maven builds.
   - 🧪 **Interactive Code Demos** — Edit and test enterprise components directly in your browser.
   - 📊 **Progress & Quiz Tracking** — Scores and completed modules are stored locally in your browser.
   - 🌓 **Themes** — Full Dark / Light mode support with automatic OS preference detection.

---

## 🛠️ Prerequisites & Environment Setup

Refer to [`00_Environment_Setup/00a_System_Requirements_and_Setup.md`](00_Environment_Setup/00a_System_Requirements_and_Setup.md) for full installation instructions:
- **Java SE Development Kit (JDK):** Version 17 LTS or 21 LTS
- **Application Server:** WildFly 30.0.1.Final (or Eclipse GlassFish 7+)
- **Build Tool:** Apache Maven 3.9+
- **IDE:** VS Code (with Extension Pack for Java), IntelliJ IDEA Ultimate, or Apache NetBeans 20+
- **Database (Optional/Labs):** PostgreSQL 15+ or H2 Embedded Database
