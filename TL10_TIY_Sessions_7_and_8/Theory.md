# TL10 — Try It Yourself: Review of Sessions 7 & 8

**Manual Reference:** JAKARTA EE – TL10  
**Book Coverage:** Session 7 (Jakarta Messaging Services / JMS) + Session 8 (Jakarta Connectors Architecture / JCA)

This is a review and consolidation session for JMS and JCA. Use this session to complete all textbook TIY exercises, address misconceptions, and reinforce hands-on lab practice.

---

## 📚 Recap — Session 7: Jakarta Messaging Services (JMS)

### What is JMS?
Jakarta Messaging (JMS) is a standard API for sending asynchronous messages between distributed enterprise components. It decouples producers from consumers — neither knows the other exists.

### JMS Domains

| Domain | Delivery Model | Destination Type | Consumers |
| :--- | :--- | :--- | :--- |
| **Point-to-Point (PTP)** | Each message consumed by **exactly one** consumer | `Queue` | One receiver |
| **Publish/Subscribe (Pub/Sub)** | Each message delivered to **all** active subscribers | `Topic` | Multiple subscribers |

### JMS API Objects — Simplified API

| Object | Role |
| :--- | :--- |
| `ConnectionFactory` | Looked up via JNDI; factory for `JMSContext` |
| `JMSContext` | Central unified object — connection + session in one |
| `JMSProducer` | Sends messages; created from `JMSContext.createProducer()` |
| `JMSConsumer` | Receives messages; created from `JMSContext.createConsumer(dest)` |
| `Queue` / `Topic` | The JNDI-registered destination |
| `TextMessage` | Message type wrapping a `String` payload |
| `ObjectMessage` | Message type wrapping a `Serializable` object |

### Message-Driven Beans (MDB)
MDBs are the enterprise listener model. The container invokes `onMessage()` automatically when a message arrives:

```java
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName="destinationType", propertyValue="jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName="destination", propertyValue="java:/jms/queue/OrderQueue")
})
public class OrderProcessorMDB implements MessageListener {
    public void onMessage(Message message) { /* process */ }
}
```

---

## 📚 Recap — Session 8: Jakarta Connectors Architecture (JCA)

### What is JCA?
Jakarta Connectors (JCA) provides a standard architecture for integrating enterprise information systems (EIS) — such as SAP, IBM CICS, mainframes, and proprietary databases — into a Jakarta EE environment.

### JCA Architecture Layers

| Layer | Component | Role |
|---|---|---|
| **Application Layer** | Application Component (EJB, Servlet) | Uses the resource adapter via the Common Client Interface (CCI) |
| **System Contracts Layer** | Resource Adapter (`.rar`) | Adapts the EIS protocol to Jakarta EE managed contracts |
| **EIS Layer** | External System | SAP, CICS, mainframe, legacy databases |

### Three System Contracts

| Contract | Purpose |
| :--- | :--- |
| **Connection Management** | Pools and manages physical connections to the EIS. |
| **Transaction Management** | Integrates EIS transactions with the Jakarta EE transaction manager (JTA). |
| **Security Management** | Propagates security principals and credentials from Jakarta EE to the EIS. |

### JDBC as a JCA Adapter
JDBC is the most common JCA Resource Adapter. When your code uses `@Resource DataSource ds`, it is transparently using the JCA Connection Management contract to get a pooled database connection.

---

## 🔁 TIY Exercise Reference Checklist

- [ ] **Session 7 TIY:** Create a Point-to-Point producer that sends 10 `TextMessage` objects to a Queue. Verify with a synchronous consumer.
- [ ] **Session 7 TIY:** Create an MDB that listens to a Topic. Subscribe from two separate browsers/consumers and verify both receive the same message.
- [ ] **Session 8 TIY:** Define a JDBC DataSource in WildFly Admin Console. Inject it using `@Resource` and list table names from the database.
- [ ] **Session 8 TIY:** Explain the three JCA system contracts with a diagram.

---

## ❓ Self-Test Questions

1. What is the difference between a Queue and a Topic?
2. Can an MDB be stateful? Why or why not?
3. How does the container know which destination to connect an MDB to?
4. What file inside a `.rar` archive describes the resource adapter's capabilities?
5. Why does JDBC not require you to manage connection pooling yourself in a Jakarta EE container?
