# Self-Study Block S1–S4: Hands-On Practical Lab Projects

**Manual Reference:** S1–S4 Practical Revision Sessions (Sessions 1 to 8)  
**Objective:** Solidify your practical mastery through three end-to-end, multi-tier enterprise projects.

---

## 🛠️ Lab Project 1: Enterprise Multi-Tier Banking Core
**Concepts Tested:** `@Stateful`, `@Stateless`, `@Singleton`, `@Lock`, JNDI Injection, CMT Rollback.

### Scenario
A commercial bank requires a core transactional backend with three distinct enterprise services:
1. `CurrencyRateCache` (`@Singleton`): Maintains real-time exchange rates (USD/EUR/GBP). Needs high-concurrency read access and exclusive write locks for updates.
2. `CustomerSessionBean` (`@Stateful`): Maintains the logged-in customer's active session, cart of pending transfers, and verification token.
3. `TransferServiceBean` (`@Stateless`): Executes atomic debit/credit operations with Container-Managed Transactions (CMT). Must roll back both accounts if either debit or credit fails.

---

### Implementation Code

#### 1. Singleton Currency Exchange Cache
```java
package com.aptech.bank.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class CurrencyRateCache {

    private Map<String, BigDecimal> exchangeRates;

    @PostConstruct
    public void init() {
        exchangeRates = new HashMap<>();
        exchangeRates.put("USD_TO_EUR", new BigDecimal("0.92"));
        exchangeRates.put("USD_TO_GBP", new BigDecimal("0.78"));
        exchangeRates.put("USD_TO_NGN", new BigDecimal("1450.00"));
        System.out.println("[CurrencyRateCache] Initialized currency rates in memory.");
    }

    @Lock(LockType.READ)
    public BigDecimal getRate(String currencyPair) {
        return exchangeRates.getOrDefault(currencyPair, BigDecimal.ONE);
    }

    @Lock(LockType.WRITE)
    public void updateRate(String currencyPair, BigDecimal newRate) {
        exchangeRates.put(currencyPair, newRate);
        System.out.println("[CurrencyRateCache] Rate updated: " + currencyPair + " = " + newRate);
    }
}
```

#### 2. Stateful Customer Session
```java
package com.aptech.bank.session;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateful;
import jakarta.ejb.Remove;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateful
public class CustomerSessionBean implements Serializable {

    private String customerId;
    private List<String> pendingTransferIds;

    @PostConstruct
    public void init() {
        this.pendingTransferIds = new ArrayList<>();
    }

    public void login(String customerId) {
        this.customerId = customerId;
        System.out.println("[CustomerSession] Customer logged in: " + customerId);
    }

    public void queueTransfer(String transferId) {
        pendingTransferIds.add(transferId);
    }

    public List<String> getPendingTransfers() {
        return new ArrayList<>(pendingTransferIds);
    }

    @Remove
    public void logout() {
        System.out.println("[CustomerSession] Logging out customer: " + customerId);
        pendingTransferIds.clear();
    }
}
```

#### 3. Stateless Transactional Transfer Service
```java
package com.aptech.bank.service;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.math.BigDecimal;

@Stateless
public class TransferServiceBean {

    @Resource
    private SessionContext sessionContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void executeTransfer(String fromAccount, String toAccount, BigDecimal amount) {
        System.out.println("[TransferService] Initiating transfer of " + amount + " from " + fromAccount + " to " + toAccount);

        // Step 1: Debit source account
        debitAccount(fromAccount, amount);

        // Simulated validation check for demonstration
        if (amount.compareTo(new BigDecimal("10000.00")) > 0) {
            System.err.println("[TransferService] Transaction exceeds limit! Forcing rollback.");
            throw new IllegalArgumentException("Single transfer cannot exceed $10,000.00");
        }

        // Step 2: Credit target account
        creditAccount(toAccount, amount);

        System.out.println("[TransferService] Transfer completed successfully.");
    }

    private void debitAccount(String accountId, BigDecimal amount) {
        System.out.println(" -> Debited " + amount + " from " + accountId);
    }

    private void creditAccount(String accountId, BigDecimal amount) {
        System.out.println(" -> Credited " + amount + " to " + accountId);
    }
}
```

---

## 🛠️ Lab Project 2: Facelets Templated Portal with EJB Integration
**Concepts Tested:** Facelets master template, composite components, `@Local` EJB integration.

### Structure
```
src/main/webapp/
├── templates/
│   └── masterLayout.xhtml     # Master layout (<ui:insert name="content">)
├── resources/
│   └── components/
│       └── balanceWidget.xhtml # Composite Component
└── portal.xhtml               # Child view (<ui:composition>)
```

#### 1. Master Template (`masterLayout.xhtml`)
```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="jakarta.faces.html"
      xmlns:ui="jakarta.faces.facelets">
<h:head>
    <title><ui:insert name="title">Enterprise Banking Portal</ui:insert></title>
    <h:outputStylesheet name="css/portal.css" />
</h:head>
<h:body>
    <header class="portal-header">
        <h2>Enterprise Commercial Bank</h2>
        <nav>
            <a href="portal.xhtml">Home</a> | <a href="transfers.xhtml">Transfers</a>
        </nav>
    </header>

    <main class="portal-content">
        <ui:insert name="content">Default content placeholder</ui:insert>
    </main>

    <footer class="portal-footer">
        <p>&copy; 2026 Aptech Jakarta EE Module. Secure Enterprise Banking.</p>
    </footer>
</h:body>
</html>
```

#### 2. Composite Component (`resources/components/balanceWidget.xhtml`)
```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:cc="jakarta.faces.composite"
      xmlns:h="jakarta.faces.html">
<cc:interface>
    <cc:attribute name="accountNumber" required="true" />
    <cc:attribute name="balance" required="true" />
    <cc:attribute name="currency" default="USD" />
</cc:interface>

<cc:implementation>
    <div class="balance-card">
        <span class="acc-num">Account: #{cc.attrs.accountNumber}</span>
        <h3 class="acc-bal">#{cc.attrs.currency} #{cc.attrs.balance}</h3>
    </div>
</cc:implementation>
</html>
```

---

## 🛠️ Lab Project 3: Asynchronous JMS Auditing with JCA DataSource
**Concepts Tested:** JMS 3.0 Queue, Message-Driven Bean (`@MessageDriven`), JDBC DataSource via JCA.

### Implementation Code

#### 1. JMS Audit Message Producer
```java
package com.aptech.audit;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

@Stateless
public class AuditMessageProducer {

    @Resource(lookup = "java:/jms/queue/AuditQueue")
    private Queue auditQueue;

    @Inject
    private JMSContext jmsContext;

    public void sendAuditLog(String action, String user, String ipAddress) {
        String payload = String.format("ACTION=%s;USER=%s;IP=%s;TIMESTAMP=%d", action, user, ipAddress, System.currentTimeMillis());
        jmsContext.createProducer().send(auditQueue, payload);
        System.out.println("[AuditProducer] Sent audit message: " + payload);
    }
}
```

#### 2. Message-Driven Bean (MDB) with JCA DataSource Persistence
```java
package com.aptech.audit;

import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/queue/AuditQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")
})
public class AuditLogConsumerMDB implements MessageListener {

    @Resource(lookup = "java:jboss/datasources/ExampleDS")
    private DataSource dataSource;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String payload = ((TextMessage) message).getText();
                System.out.println("[AuditMDB] Received async log: " + payload);
                persistToDatabase(payload);
            } catch (JMSException e) {
                System.err.println("[AuditMDB] JMS error: " + e.getMessage());
            }
        }
    }

    private void persistToDatabase(String logEntry) {
        String sql = "INSERT INTO audit_logs (log_entry, created_at) VALUES (?, CURRENT_TIMESTAMP)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, logEntry);
            ps.executeUpdate();
            System.out.println("[AuditMDB] Successfully stored log in database using JCA pool.");
        } catch (SQLException e) {
            System.err.println("[AuditMDB] SQL error persisting log: " + e.getMessage());
        }
    }
}
```

---

## 📋 Verification & Expected WildFly Log Output

When deploying and testing the three practical projects, your `server.log` should output:

```log
[CurrencyRateCache] Initialized currency rates in memory.
[CustomerSession] Customer logged in: CUST-88310
[TransferService] Initiating transfer of 250.00 from ACC-101 to ACC-102
 -> Debited 250.00 from ACC-101
 -> Credited 250.00 to ACC-102
[TransferService] Transfer completed successfully.
[AuditProducer] Sent audit message: ACTION=LOGIN;USER=CUST-88310;IP=192.168.1.15;TIMESTAMP=1727958100000
[AuditMDB] Received async log: ACTION=LOGIN;USER=CUST-88310;IP=192.168.1.15;TIMESTAMP=1727958100000
[AuditMDB] Successfully stored log in database using JCA pool.
```
