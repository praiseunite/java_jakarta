# Session 12 Assignment: Aspect-Oriented Interceptors and CDI Events

## 📝 Assignment Overview

This assignment evaluates your ability to implement cross-cutting concerns (Aspect-Oriented Programming) using CDI Interceptors and your ability to decouple architecture using CDI Events.

Based on the textbook **Try It Yourself** requirement:
> *"Create an application demonstrating the use of Interceptors for logging and CDI events for decoupled notification."*

You will build a **User Registration Flow** where security logging is abstracted away into an Interceptor, and a welcome email is triggered asynchronously via an Event.

---

## 🏢 Business Scenario

When a user registers on your application, three things must happen:
1. The method execution time and parameters must be audited securely.
2. The user must be saved to the database (simulated business logic).
3. A notification service must send them a Welcome Email.

To keep the architecture clean:
* The business method must **only** contain the database logic.
* An **Interceptor** will handle the auditing.
* An **Event Publisher / Observer** will handle the welcome email.

---

## 📋 Specific Requirements

### 1. Build the Audit Interceptor
* Create an Interceptor Binding annotation called `@Audited`.
* Create the `AuditInterceptor` class.
* Implement an `@AroundInvoke` method. It must log the name of the method being called and the timestamp before executing `context.proceed()`.

### 2. Implement the Business Service & Event Publisher
* Create a simple POJO `UserCreatedEvent` containing a `String username`.
* Create `RegistrationService`.
* Inject an `Event<UserCreatedEvent>`.
* Create a `registerUser(String username)` method.
* Annotate `registerUser` with your custom `@Audited` annotation.
* Inside `registerUser`, print `"Registering user: " + username`, then fire the `UserCreatedEvent`.

### 3. Implement the Independent Observer
* Create a new class `EmailNotificationService`.
* Write a method `sendWelcomeEmail` that listens for the event using the `@Observes` annotation on its parameter.
* The method should print: `"Sending welcome email to " + event.getUsername()`.

### 4. Create the Test Controller
* Create a JSF backing bean or standalone tester that injects `RegistrationService`.
* Call `registerUser("john_doe")`.

---

## 📂 Expected Directory Structure

```
Session_12_Assignment/
├── pom.xml
└── src/main/java/com/bank/events/
    ├── Audited.java                     (Interceptor Binding)
    ├── AuditInterceptor.java            (Interceptor Logic)
    ├── UserCreatedEvent.java            (Event Payload)
    ├── RegistrationService.java         (Business Logic & Publisher)
    ├── EmailNotificationService.java    (Event Observer)
    └── AppTester.java                   (Test Client)
```

## 💯 Grading Criteria

1. **Interceptor Implementation (35%)**: Binding is created correctly, `@AroundInvoke` intercepts the call, logs data, and correctly resumes execution using `proceed()`.
2. **Event Architecture (35%)**: The Event is fired without any direct coupling to the EmailNotificationService, and the Observer successfully catches it using `@Observes`.
3. **Clean Code & Execution (30%)**: The output exactly follows the flow: Interceptor Start $\to$ Business Logic $\to$ Event Notification $\to$ Interceptor End.
