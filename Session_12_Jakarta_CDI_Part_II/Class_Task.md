# Session 12 Class Task: Implementing CDI Producers and Disposers

## 📌 Task Overview

In this hands-on lab, you will use CDI Producers to dynamically create configuration objects and connections that cannot be easily instantiated using standard bean constructors. You will also use CDI Disposers to safely clean up those resources when the request is over.

You will:
1. **Create a Configuration Bean** that dynamically reads environment variables.
2. **Implement a Producer Method (`@Produces`)** to act as a factory for an object.
3. **Implement a Disposer Method (`@Disposes`)** to clean up the object.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17.
* **Jakarta EE Application Server:** WildFly 30+ (or Payara/GlassFish).
* **IDE:** IntelliJ IDEA or Eclipse.

---

## 🏗️ Project Architecture

```
Session_12_Producers_Lab/
├── pom.xml
└── src/main/java/com/bank/producers/
    ├── LegacyAuditClient.java       (3rd-party unmodifiable class)
    ├── AuditClientFactory.java      (Producer and Disposer)
    └── BusinessService.java         (Uses the produced client)
```

---

## 📋 STEP 1 — The Legacy Client (Simulated 3rd Party)

Imagine `LegacyAuditClient` is a class from a JAR you cannot modify. It doesn't have a no-arg constructor, so CDI cannot instantiate it natively using `@Inject` without help.

```java
package com.bank.producers;

public class LegacyAuditClient {
    private String serverUrl;
    private boolean connected;

    public LegacyAuditClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.connected = false;
    }

    public void connect() {
        this.connected = true;
        System.out.println("Connected to Audit Server at: " + serverUrl);
    }

    public void disconnect() {
        this.connected = false;
        System.out.println("Disconnected from Audit Server.");
    }

    public void log(String message) {
        if (!connected) throw new IllegalStateException("Not connected!");
        System.out.println("[AUDIT LOG] " + message);
    }
}
```

---

## 📋 STEP 2 — Create the Factory (Producer/Disposer)

Use `@Produces` to tell CDI how to create the `LegacyAuditClient`, and `@Disposes` to tell CDI how to clean it up.

```java
package com.bank.producers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class AuditClientFactory {

    @Produces
    @RequestScoped
    public LegacyAuditClient createClient() {
        System.out.println("--> [CDI Factory] Producing LegacyAuditClient");
        // Read URL from environment or fallback
        String url = System.getProperty("audit.url", "tcp://audit-server:9999");
        
        LegacyAuditClient client = new LegacyAuditClient(url);
        client.connect();
        
        return client;
    }

    // CDI calls this automatically when the RequestScope ends!
    public void cleanupClient(@Disposes LegacyAuditClient client) {
        System.out.println("--> [CDI Factory] Disposing LegacyAuditClient");
        client.disconnect();
    }
}
```

---

## 📋 STEP 3 — Use the Produced Bean

Now you can inject the client anywhere as if it were a normal CDI bean!

```java
package com.bank.producers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class BusinessService {

    // CDI knows to call the @Produces method to get this!
    @Inject
    private LegacyAuditClient auditClient;

    public void performAction() {
        System.out.println("Business logic executing...");
        auditClient.log("Action performed by user.");
    }
}
```

---

## ✅ Expected Output

When a client hits the endpoint/backing bean that triggers `BusinessService.performAction()`, the server log should clearly show the lifecycle:

```text
--> [CDI Factory] Producing LegacyAuditClient
Connected to Audit Server at: tcp://audit-server:9999
Business logic executing...
[AUDIT LOG] Action performed by user.
--> [CDI Factory] Disposing LegacyAuditClient
Disconnected from Audit Server.
```
