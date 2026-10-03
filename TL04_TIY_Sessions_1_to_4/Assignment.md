# Session 5 — Assignment
## Project Defense and Code Review (Sessions 1-4)

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 5)

---

## 📝 Part 1: Architecture Diagram (Written/Drawn)

In the Class Task, you built the first milestone of the GlobalBank application using concepts from the "Try It Yourself" exercises of the first four sessions.

**Your Task:**
Draw (or use a tool like draw.io/Lucidchart) an architecture diagram of the code you just wrote in the Class Task. 
Your diagram MUST include and clearly label:
1. The Web Browser
2. `CheckoutServlet`
3. `UserSessionCart` (Stateful)
4. `PaymentProcessor` (Stateless)
5. Where JNDI is used.
6. Where the Transaction Boundary begins and ends.
7. Where Security checks are applied.

Submit your diagram as an image or PDF.

---

## 🗣️ Part 2: Project Defense (Live or Recorded)

As stated in the course requirements, you must be able to **defend your live project**. 

**Your Task:**
Prepare a 3 to 5-minute presentation demonstrating the code you wrote in the Session 5 Class Task. You will present this to your instructor.

You must cover the following points during your defense:
1. **The Code:** Walk through `UserSessionCart.java` and `PaymentProcessor.java`. Explain the difference between `@Stateful` and `@Stateless` using your code as the example.
2. **The JNDI Lookup:** Show your Servlet code. Point to the `InitialContext.lookup()` line and explain exactly what the `java:global` string means and how WildFly uses it.
3. **The Transaction:** Point to `@TransactionAttribute(REQUIRED)` and explain what the EJB container is doing behind the scenes when `process()` is called. What happens if the database crashes during this method?
4. **The Weakness:** In the Class Task, the Servlet performs a new JNDI lookup every time the page refreshes. Explain why this breaks the intended functionality of a "Stateful" shopping cart (which is supposed to remember the user across multiple clicks). *How should a web application actually store a reference to a Stateful bean so it isn't lost on page refresh?*

---

## 💡 Instructor Grading Rubric for Defense

- **Code Understanding (40%):** Does the student accurately explain the annotations and Java code they wrote?
- **Concept Integration (30%):** Can the student explain how the Servlet, Stateful Bean, and Stateless Bean communicate?
- **Troubleshooting Knowledge (30%):** Can the student accurately identify the weakness in the JNDI lookup approach for Stateful beans in a web environment (the requirement to store the bean in the `HttpSession`)?
