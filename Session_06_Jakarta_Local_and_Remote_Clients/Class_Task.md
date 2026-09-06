# Session 6 Class Task: Implementing Jakarta Local and Remote Clients

## 📌 Task Overview

In this practical lab, you will build and test an enterprise application with both **Local** and **Remote** clients running on **WildFly 26+**:

1. **Enterprise Server Module (`client-lab-server`):**
   * A Stateless Session Bean with a `@Local` interface: `PrimeCalculatorBean` (calculates prime numbers up to a specified limit).
   * A Stateless Session Bean with a `@Remote` interface: `StringReverseBean` (reverses a client-provided string).
2. **Local Client (`client-lab-web` or co-located servlet):**
   * A Web Servlet running in the same deployment/JVM that calls `PrimeCalculatorLocal` directly using `@EJB` dependency injection.
3. **Remote Client (`client-lab-remote-app`):**
   * A standalone Java console application running in a completely separate JVM process that looks up `StringReverseRemote` over JNDI / HTTP-Remoting and invokes it across the network.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 installed and configured (`JAVA_HOME`).
* **Application Server:** WildFly 26+ installed and running on `http://localhost:8080`.
* **IDE:** IntelliJ IDEA (Ultimate or Community) OR Eclipse IDE for Enterprise Java.
* **Build Tool:** Maven.

---

## 🏗️ Project Architecture

```
Session_06_Lab/
├── server-ejb/                      <-- EJB Module (Deployed to WildFly)
│   ├── pom.xml
│   └── src/main/java/com/clientlab/
│       ├── local/
│       │   ├── PrimeCalculatorLocal.java
│       │   └── PrimeCalculatorBean.java
│       ├── remote/
│       │   ├── StringReverseRemote.java
│       │   └── StringReverseBean.java
│       └── web/
│           └── LocalClientServlet.java
└── remote-client/                   <-- Standalone Client (Separate JVM)
    ├── pom.xml
    └── src/main/java/com/clientlab/client/
        └── RemoteClientApp.java
```

---

## 📋 STEP 1 — Create the Server Module (`server-ejb`)

### 1.1 Maven Configuration (`server-ejb/pom.xml`)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.clientlab</groupId>
    <artifactId>server-ejb</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <jakartaee-api.version>9.1.0</jakartaee-api.version>
    </properties>

    <dependencies>
        <!-- Jakarta EE Web API (EJB + Servlet) -->
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-web-api</artifactId>
            <version>${jakartaee-api.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>server-ejb</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.2</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### 1.2 Implement the Local Bean (Prime Calculator)

#### `src/main/java/com/clientlab/local/PrimeCalculatorLocal.java`
```java
package com.clientlab.local;

import jakarta.ejb.Local;

@Local
public interface PrimeCalculatorLocal {
    String getPrimesUpTo(int limit);
}
```

#### `src/main/java/com/clientlab/local/PrimeCalculatorBean.java`
```java
package com.clientlab.local;

import jakarta.ejb.Stateless;

@Stateless
public class PrimeCalculatorBean implements PrimeCalculatorLocal {

    @Override
    public String getPrimesUpTo(int limit) {
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i <= limit; i++) {
            if (isPrime(i)) {
                sb.append(i).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private boolean isPrime(int n) {
        if (n <= 1) return false;
        for (int i = 2; i <= Math.sqrt(n); i++) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
```

---

### 1.3 Implement the Remote Bean (String Reversal)

#### `src/main/java/com/clientlab/remote/StringReverseRemote.java`
```java
package com.clientlab.remote;

import jakarta.ejb.Remote;

@Remote
public interface StringReverseRemote {
    String reverseString(String text);
}
```

#### `src/main/java/com/clientlab/remote/StringReverseBean.java`
```java
package com.clientlab.remote;

import jakarta.ejb.Stateless;

@Stateless
public class StringReverseBean implements StringReverseRemote {

    @Override
    public String reverseString(String text) {
        if (text == null) {
            return "";
        }
        return new StringBuilder(text).reverse().toString();
    }
}
```

---

### 1.4 Implement the Local Client (Servlet)

#### `src/main/java/com/clientlab/web/LocalClientServlet.java`
```java
package com.clientlab.web;

import com.clientlab.local.PrimeCalculatorLocal;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/local-primes")
public class LocalClientServlet extends HttpServlet {

    // Direct injection of the local EJB interface
    @EJB
    private PrimeCalculatorLocal primeCalculator;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String limitParam = req.getParameter("limit");
        int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 100;

        String primes = primeCalculator.getPrimesUpTo(limit);

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Local Client Result</title></head><body>");
        out.println("<h2>Jakarta Local Client - Prime Calculator</h2>");
        out.println("<p><strong>Communication Type:</strong> Local EJB Injection (@EJB / Pass-by-Reference)</p>");
        out.println("<p><strong>Primes up to " + limit + ":</strong></p>");
        out.println("<blockquote>" + primes + "</blockquote>");
        out.println("</body></html>");
    }
}
```

---

## 📋 STEP 2 — Build & Deploy the Server Application

### Using Maven Command Line:
```bash
cd server-ejb
mvn clean package
```
Copy `target/server-ejb.war` to `$WILDFLY_HOME/standalone/deployments/`.

### In IntelliJ IDEA:
1. Open the `server-ejb` project.
2. Go to **Run > Edit Configurations...**
3. Click `+` and select **JBoss / WildFly Server > Local**.
4. In the **Deployment** tab, click `+` and choose **server-ejb:war exploded** or **server-ejb:war**.
5. Click **Run**.
6. Check the WildFly server log for the JNDI registration bindings:
   ```
   java:global/server-ejb/StringReverseBean!com.clientlab.remote.StringReverseRemote
   java:app/server-ejb/StringReverseBean!com.clientlab.remote.StringReverseRemote
   ```

### In Eclipse IDE:
1. Import `server-ejb` as an **Existing Maven Project**.
2. Right-click project > **Run As > Run on Server**.
3. Choose your configured **WildFly 26+** server and click **Finish**.

### Test the Local Client:
Open your browser and navigate to:
```
http://localhost:8080/server-ejb/local-primes?limit=50
```
**Expected Output:**
> Prime numbers up to 50: `2 3 5 7 11 13 17 19 23 29 31 37 41 43 47`

---

## 📋 STEP 3 — Create the Remote Standalone Client (`remote-client`)

Now build a client running in an independent console process (different JVM).

### 3.1 Maven Configuration (`remote-client/pom.xml`)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.clientlab</groupId>
    <artifactId>remote-client</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <wildfly.version>26.1.2.Final</wildfly.version>
    </properties>

    <dependencies>
        <!-- Remote Interface Reference (or dependency on server-ejb) -->
        <dependency>
            <groupId>com.clientlab</groupId>
            <artifactId>server-ejb</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- WildFly EJB Client BOM for remote communication -->
        <dependency>
            <groupId>org.wildfly</groupId>
            <artifactId>wildfly-ejb-client-bom</artifactId>
            <version>${wildfly.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- WildFly Naming & Remoting Client -->
        <dependency>
            <groupId>org.wildfly</groupId>
            <artifactId>wildfly-naming-client</artifactId>
            <version>1.0.15.Final</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.ejb-client</groupId>
            <artifactId>jboss-ejb-client</artifactId>
            <version>4.0.44.Final</version>
        </dependency>
    </dependencies>
</project>
```

---

### 3.2 Implement the Standalone Remote Client

#### `src/main/java/com/clientlab/client/RemoteClientApp.java`
```java
package com.clientlab.client;

import com.clientlab.remote.StringReverseRemote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.Scanner;

public class RemoteClientApp {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("   WildFly Remote EJB Client Application           ");
        System.out.println("====================================================");

        try {
            // Step 1: Set up JNDI environment for remote invocation
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put(Context.PROVIDER_URL, "remote+http://localhost:8080");

            InitialContext ctx = new InitialContext(props);

            // Step 2: WildFly EJB JNDI lookup string pattern:
            // "ejb:<appName>/<moduleName>/<distinctName>/<beanName>!<interfaceName>"
            String jndiPath = "ejb:/server-ejb/StringReverseBean!" + StringReverseRemote.class.getName();

            System.out.println("Looking up EJB via JNDI: " + jndiPath);
            StringReverseRemote remoteBean = (StringReverseRemote) ctx.lookup(jndiPath);
            System.out.println(">> Remote Proxy successfully acquired from WildFly server!");

            // Step 3: Interactive invocation
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nEnter text to reverse (or type 'exit' to quit): ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }

                // Remote method call (serialized over network via RMI / HTTP-remoting)
                String reversed = remoteBean.reverseString(input);
                System.out.println("<< Server Response: " + reversed);
            }

            ctx.close();
            System.out.println("\nClient terminated.");

        } catch (NamingException e) {
            System.err.println("Failed to connect or lookup remote EJB: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
```

---

## 🧪 Verification & Execution Steps

1. **Verify WildFly is active:**
   * Open `http://localhost:9990` (Admin Console) or verify console logs show `WildFly Full 26.x.x started`.
2. **Execute the Remote Client:**
   * In your IDE (IntelliJ or Eclipse), right-click `RemoteClientApp.java` > **Run 'RemoteClientApp.main()'**.
   * Console prompt:
     ```
     Looking up EJB via JNDI: ejb:/server-ejb/StringReverseBean!com.clientlab.remote.StringReverseRemote
     >> Remote Proxy successfully acquired from WildFly server!
     
     Enter text to reverse (or type 'exit' to quit): Jakarta Enterprise Beans
     << Server Response: snaeB esirpretnE atrakaJ
     ```
3. **Compare Performance & Execution:**
   * Notice that the Remote Client runs in a distinct OS process (JVM PID), whereas the Servlet runs inside the WildFly server JVM.
   * Kill WildFly and type another input in the client: Observe the `java.rmi.RemoteException` or connection timeout!
