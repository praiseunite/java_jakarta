# Session 7 Class Task: Building a Jakarta Messaging (JMS) Application

## 📌 Task Overview

In this hands-on lab, you will build and test a complete Point-to-Point Jakarta Messaging (JMS) application:

1. **Configure a Maven Project** with modern Jakarta Messaging and Apache ActiveMQ / Artemis client dependencies.
2. **Implement an Embedded / Standalone Message Broker** so you can develop and test messaging without complex external installations.
3. **Build `MessageSender`:** Creates and dispatches text messages to a Point-to-Point destination (`MESSAGE_QUEUE`).
4. **Build `MessageReceiver`:** Demonstrates both **synchronous blocking consumption** (`receive()`) and **asynchronous event-driven consumption** using a `MessageListener`.
5. **Inspect Message Metadata:** Inspect headers (`JMSMessageID`, `JMSTimestamp`, `JMSDestination`) and custom properties.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 installed and configured (`JAVA_HOME`).
* **IDE:** IntelliJ IDEA (Ultimate or Community) OR Eclipse IDE for Enterprise Java.
* **Build Tool:** Apache Maven (bundled in both IntelliJ and Eclipse).

---

## 🏗️ Project Architecture

```
Session_07_JMS_Lab/
├── pom.xml
└── src/main/java/com/jms/lab/
    ├── broker/
    │   └── EmbeddedBrokerManager.java  (Embedded broker for zero-config testing)
    ├── producer/
    │   └── OrderMessageProducer.java   (Sends orders to queue)
    ├── consumer/
    │   ├── SyncOrderConsumer.java      (Synchronous receive())
    │   └── AsyncOrderListener.java     (Asynchronous MessageListener)
    └── AppMain.java                    (Launches broker, producer, and consumer)
```

---

## 📋 STEP 1 — Create the Maven Project (`pom.xml`)

Create a standard Maven project named `Session_07_JMS_Lab`.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.jms.lab</groupId>
    <artifactId>Session_07_JMS_Lab</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <activemq.version>5.18.3</activemq.version>
    </properties>

    <dependencies>
        <!-- Apache ActiveMQ All-in-One Client & Embedded Broker -->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-broker</artifactId>
            <version>${activemq.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-client</artifactId>
            <version>${activemq.version}</version>
        </dependency>

        <!-- Logging Framework -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.9</version>
        </dependency>
    </dependencies>
</project>
```

---

## 📋 STEP 2 — Create the Embedded Broker Manager

To eliminate the need for running an external binary or bat script during class exercises, we will create an embedded ActiveMQ broker that spins up automatically inside our application on `tcp://localhost:61616`.

#### `src/main/java/com/jms/lab/broker/EmbeddedBrokerManager.java`
```java
package com.jms.lab.broker;

import org.apache.activemq.broker.BrokerService;

public class EmbeddedBrokerManager {

    private static BrokerService broker;

    public static synchronized void startBroker() throws Exception {
        if (broker == null) {
            broker = new BrokerService();
            broker.setBrokerName("LocalEnterpriseBroker");
            broker.setPersistent(false); // In-memory for fast lab testing
            broker.setUseJmx(false);
            broker.addConnector("tcp://localhost:61616");
            broker.start();
            System.out.println(">> Embedded ActiveMQ Broker started on tcp://localhost:61616");
        }
    }

    public static synchronized void stopBroker() throws Exception {
        if (broker != null) {
            broker.stop();
            System.out.println(">> Embedded ActiveMQ Broker stopped.");
        }
    }
}
```

---

## 📋 STEP 3 — Create the Message Producer (`OrderMessageProducer.java`)

This class connects to the broker, creates a non-transacted session with `AUTO_ACKNOWLEDGE`, and dispatches messages with custom priority and properties.

#### `src/main/java/com/jms/lab/producer/OrderMessageProducer.java`
```java
package com.jms.lab.producer;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class OrderMessageProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "ORDER_FULFILLMENT_QUEUE";

    public void sendOrder(String orderId, double amount, String customerType) {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = null;

        try {
            // 1. Create connection and start delivery
            connection = connectionFactory.createConnection();
            connection.start();

            // 2. Create non-transacted session with automatic acknowledgement
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Define the destination queue
            Queue queue = session.createQueue(QUEUE_NAME);

            // 4. Create producer and build the message
            MessageProducer producer = session.createProducer(queue);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            TextMessage message = session.createTextMessage("Payload: Order #" + orderId + " for $" + amount);

            // Set JMS application properties (used for filtering/selectors)
            message.setStringProperty("OrderId", orderId);
            message.setDoubleProperty("Amount", amount);
            message.setStringProperty("CustomerType", customerType);

            // 5. Send message
            producer.send(message);

            System.out.println("[PRODUCER] Successfully dispatched: " + message.getText()
                    + " [JMSMessageID: " + message.getJMSMessageID() + "]");

            session.close();
        } catch (JMSException e) {
            System.err.println("[PRODUCER ERROR] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {}
            }
        }
    }
}
```

---

## 📋 STEP 4 — Create the Consumers

### 4.1 Asynchronous Event-Driven Consumer (`AsyncOrderListener.java`)

Using a `MessageListener` allows the consumer to react immediately whenever a new message arrives in the queue without blocking the application thread.

#### `src/main/java/com/jms/lab/consumer/AsyncOrderListener.java`
```java
package com.jms.lab.consumer;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class AsyncOrderListener implements MessageListener {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "ORDER_FULFILLMENT_QUEUE";
    private Connection connection;

    public void startListening() {
        try {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
            connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue(QUEUE_NAME);

            MessageConsumer consumer = session.createConsumer(queue);
            // Register this listener for async callback
            consumer.setMessageListener(this);

            System.out.println("[ASYNC LISTENER] Waiting for incoming messages on " + QUEUE_NAME + "...");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage textMsg = (TextMessage) message;
                String orderId = textMsg.getStringProperty("OrderId");
                String custType = textMsg.getStringProperty("CustomerType");

                System.out.println("[ASYNC CONSUMER RECEIVED] " + textMsg.getText()
                        + " | OrderId=" + orderId + " | CustomerType=" + custType);
            }
        } catch (JMSException e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    public void stop() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ignored) {}
        }
    }
}
```

---

## 📋 STEP 5 — Run the Application (`AppMain.java`)

#### `src/main/java/com/jms/lab/AppMain.java`
```java
package com.jms.lab;

import com.jms.lab.broker.EmbeddedBrokerManager;
import com.jms.lab.consumer.AsyncOrderListener;
import com.jms.lab.producer.OrderMessageProducer;

public class AppMain {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("   Jakarta Messaging (JMS) Lab Demonstration    ");
        System.out.println("=================================================");

        // 1. Start the embedded ActiveMQ broker
        EmbeddedBrokerManager.startBroker();
        Thread.sleep(1000);

        // 2. Start the asynchronous consumer listener
        AsyncOrderListener listener = new AsyncOrderListener();
        listener.startListening();
        Thread.sleep(500);

        // 3. Dispatch several orders through the producer
        OrderMessageProducer producer = new OrderMessageProducer();
        System.out.println("\n--- Dispatching Test Messages ---");
        producer.sendOrder("ORD-101", 149.99, "VIP");
        producer.sendOrder("ORD-102", 49.50, "REGULAR");
        producer.sendOrder("ORD-103", 890.00, "ENTERPRISE");

        // Allow consumer threads to process messages
        Thread.sleep(2000);

        // 4. Clean shutdown
        System.out.println("\n--- Shutting Down ---");
        listener.stop();
        EmbeddedBrokerManager.stopBroker();
        System.out.println("Lab run completed successfully.");
    }
}
```

---

## 🧪 Verification & Expected Output

### Running in IntelliJ IDEA:

![IntelliJ IDEA Execution and Project View](../assets/images/intellij_wildfly_setup.jpg)

#### IntelliJ Navigation Flow:
1. **Navigate to Main Class:** In the **Project** window on the left, expand `src/main/java/com/jms/lab/` and double-click `AppMain.java`.
2. **Execute:** Right-click inside `AppMain.java` (or click the green **Play** triangle in the left editor gutter next to line `public static void main`) and select **Run 'AppMain.main()'** (Keyboard shortcut: `Ctrl + Shift + F10` on Windows/Linux, `Ctrl + Shift + R` on macOS).
3. **Inspect Output:** Watch the bottom **Run** terminal output to observe the active embedded broker start up, the asynchronous listener register, and the messages get consumed in real-time.

```
=================================================
   Jakarta Messaging (JMS) Lab Demonstration    
=================================================
>> Embedded ActiveMQ Broker started on tcp://localhost:61616
[ASYNC LISTENER] Waiting for incoming messages on ORDER_FULFILLMENT_QUEUE...

--- Dispatching Test Messages ---
[PRODUCER] Successfully dispatched: Payload: Order #ORD-101 for $149.99 [JMSMessageID: ID:MacBook-...:1]
[ASYNC CONSUMER RECEIVED] Payload: Order #ORD-101 for $149.99 | OrderId=ORD-101 | CustomerType=VIP
[PRODUCER] Successfully dispatched: Payload: Order #ORD-102 for $49.50 [JMSMessageID: ID:MacBook-...:2]
[ASYNC CONSUMER RECEIVED] Payload: Order #ORD-102 for $49.50 | OrderId=ORD-102 | CustomerType=REGULAR
[PRODUCER] Successfully dispatched: Payload: Order #ORD-103 for $890.00 [JMSMessageID: ID:MacBook-...:3]
[ASYNC CONSUMER RECEIVED] Payload: Order #ORD-103 for $890.00 | OrderId=ORD-103 | CustomerType=ENTERPRISE

--- Shutting Down ---
>> Embedded ActiveMQ Broker stopped.
Lab run completed successfully.
```
