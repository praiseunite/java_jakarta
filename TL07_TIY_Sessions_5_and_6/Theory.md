# TL07 — Try It Yourself: Review of Sessions 5 & 6

**Manual Reference:** JAKARTA EE – TL7  
**Book Coverage:** Session 5 (Facelets) + Session 6 (Jakarta Local and Remote Clients)

This session is a guided review and consolidation of the topics covered in TL5 and TL6. Use this time to clarify doubts, revisit key concepts, and complete all hands-on Try It Yourself exercises from the textbook.

---

## 📚 Recap — Session 5: Facelets in Jakarta EE

### What are Facelets?
Facelets is the default view technology (templating system) for Jakarta Faces (JSF). It replaced JSP as the primary page declaration language.

### Facelets Lifecycle Review

| Phase | Description |
| :--- | :--- |
| **Restore View** | Build or restore the component tree from the XHTML template |
| **Apply Request Values** | Read incoming form values into components |
| **Process Validations** | Validate all form data; route to Render Response if errors exist |
| **Update Model Values** | Push validated data into managed bean fields |
| **Invoke Application** | Execute business logic (e.g., action methods on beans) |
| **Render Response** | Serialize the component tree back to HTML and send to client |

### Key Facelets Annotations & Tags

| Concept | Description |
| :--- | :--- |
| `ui:composition` | Defines the template or wraps reusable page regions |
| `ui:define` | Fills a named slot inside a template |
| `ui:insert` | Declares a named placeholder in the master template layout |
| `ui:include` | Embeds another XHTML file inline at that location |
| `cc:interface` | Declares a composite component's public API (attributes) |
| `cc:implementation` | Provides the HTML rendering for a composite component |
| `h:outputStylesheet`, `h:outputScript` | Declares web resources (CSS/JS) managed by JSF |
| `@FacesConverter` | Declares a converter for a specific type |
| `@FacesValidator` | Declares a component-level validator |

---

## 📚 Recap — Session 6: Jakarta Local and Remote Clients

### What is a Client?
A **client** is any component that calls a business method on an EJB. The type of client determines which interface the EJB must expose.

### Local vs. Remote Client Decision Matrix

| Factor | Local Client (`@Local`) | Remote Client (`@Remote`) |
| :--- | :--- | :--- |
| **Location** | Same JVM / same application module | Different JVM, machine, or application |
| **Call overhead** | Very low (direct memory reference) | High (serialization + network marshalling) |
| **Parameter passing** | Pass-by-reference (no copy) | Pass-by-value (full serialization copy) |
| **Interface required** | `@Local` or no annotation | `@Remote` |
| **Typical caller** | JSF Backing Bean, CDI bean in same WAR | Standalone Java client, another server |

### Object Lifecycle and Identity

* **Stateless SLSBs** — Container manages a pool. Clients never hold a specific instance reference.
* **Stateful SFSBs** — Client holds a specific session reference. Destroyed on `@Remove` or timeout.
* **Object Identity in Remote clients** — EJBObject references implement `equals()` based on the server-assigned identity key, not Java object identity.

---

## 🔁 TIY Exercise Reference Checklist

Before the session, ensure you have completed the following exercises from the textbook:

- [ ] **Session 5 TIY:** Build a JSF application with a Facelets master template, a `ui:composition` page, and one composite component.
- [ ] **Session 5 TIY:** Demonstrate all 6 JSF lifecycle phases by adding a PhaseListener.
- [ ] **Session 6 TIY:** Create a Stateless EJB with both `@Local` and `@Remote` interfaces. Call from both a JSF bean (local) and a standalone Java main class (remote).
- [ ] **Session 6 TIY:** Demonstrate how object identity differs between local and remote references.

---

## ❓ Key Concepts to Re-Test Yourself On

1. What is the difference between `ui:composition` and `ui:decorate`?
2. In which lifecycle phase is `#{bean.actionMethod()}` invoked?
3. Why does passing an object to a Remote EJB require it to be `Serializable`?
4. When would you choose `@Local` over `@Remote`?
5. What happens to a Stateful session bean's state when the client's HTTP session times out?
