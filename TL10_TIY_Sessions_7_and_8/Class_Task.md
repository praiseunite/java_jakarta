# TL10 Class Task: JMS Pub/Sub with MDB + JDBC DataSource

## 📌 Task Overview

Build an application that demonstrates both a **Publish/Subscribe JMS topic** with an MDB listener, and a **JDBC DataSource** connection using JCA.

---

## 📋 STEP 1 — Publish to a JMS Topic

```java
package com.bank.jms;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Topic;

@Named
@RequestScoped
public class NotificationPublisher {

    @Resource(lookup = "java:/ConnectionFactory")
    private ConnectionFactory cf;

    @Resource(lookup = "java:/jms/topic/BankAlerts")
    private Topic bankAlerts;

    public void sendAlert(String message) {
        try (JMSContext ctx = cf.createContext()) {
            ctx.createProducer().send(bankAlerts, message);
            System.out.println("Published alert: " + message);
        }
    }
}
```

---

## 📋 STEP 2 — MDB Subscriber

```java
package com.bank.jms;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/topic/BankAlerts")
})
public class AlertLoggerMDB implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage txt) {
                System.out.println("[MDB AlertLogger] Received: " + txt.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## 📋 STEP 3 — Use the JDBC DataSource (JCA)

```java
package com.bank.db;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Stateless
public class CustomerRepository {

    @Resource(lookup = "java:/jdbc/GlobalBankDB")
    private DataSource dataSource;

    public int countCustomers() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
```

---

## ✅ Verification

1. Configure a JMS Topic named `BankAlerts` and a JDBC DataSource `GlobalBankDB` in WildFly Admin Console.
2. Deploy and trigger `NotificationPublisher.sendAlert("Fraud Alert!")`.
3. Verify the MDB prints the received message in the server log.
4. Verify `CustomerRepository.countCustomers()` returns a valid number from your database.
