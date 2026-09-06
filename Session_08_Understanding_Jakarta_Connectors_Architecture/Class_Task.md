# Session 8 Class Task: Implementing an Enterprise Data Access Layer with JDBC

## 📌 Task Overview

In this hands-on lab, you will build an enterprise-grade database integration layer using **Java Database Connectivity (JDBC)** and **DAO (Data Access Object)** patterns:

1. **Configure a Maven Project** with JDBC drivers and connection pooling support.
2. **Implement `DatabaseConnectionFactory`:** Manages connection lifecycle and encapsulates driver configuration.
3. **Build `UserAccountDAO`:** Implements complete CRUD (Create, Read, Update, Delete) operations using:
   * **`Statement`** for table DDL creation.
   * **`PreparedStatement`** with parameter binding to prevent SQL injection.
   * Auto-generated primary key retrieval (`Statement.RETURN_GENERATED_KEYS`).
   * **`ResultSet`** cursor navigation and object mapping.
   * Robust cleanup using Java's **`try-with-resources`** pattern.
4. **Execute and Verify:** Test against an embedded relational database (H2) for zero-setup execution, with seamless switching to MySQL/PostgreSQL.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 (`JAVA_HOME` configured).
* **IDE:** IntelliJ IDEA (Ultimate or Community) OR Eclipse IDE for Enterprise Java.
* **Build Tool:** Apache Maven.

---

## 🏗️ Project Architecture

```
Session_08_JDBC_Lab/
├── pom.xml
└── src/main/java/com/connector/lab/
    ├── config/
    │   └── DatabaseConfig.java           (Connection strings & credentials)
    ├── model/
    │   └── UserAccount.java              (Data transfer model)
    ├── dao/
    │   ├── UserAccountDAO.java           (CRUD interface)
    │   └── JdbcUserAccountDAO.java       (JDBC implementation)
    └── AppMain.java                      (Interactive demonstration runner)
```

---

## 📋 STEP 1 — Create the Maven Project (`pom.xml`)

Create a Maven project named `Session_08_JDBC_Lab`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.connector.lab</groupId>
    <artifactId>Session_08_JDBC_Lab</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- H2 In-Memory Relational Database (Runs out-of-the-box anywhere) -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>

        <!-- MySQL Connector/J (For external MySQL servers) -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
    </dependencies>
</project>
```

---

## 📋 STEP 2 — Create the Configuration & Connection Manager

#### `src/main/java/com/connector/lab/config/DatabaseConfig.java`
```java
package com.connector.lab.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    // Default: Embedded H2 database in MySQL-compatibility mode
    // (To use MySQL instead, uncomment the MySQL connection string below)
    private static final String URL = "jdbc:h2:mem:enterprisedb;DB_CLOSE_DELAY=-1;MODE=MySQL";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /*
    // Example for local MySQL:
    private static final String URL = "jdbc:mysql://localhost:3306/enterprise_db?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    */

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

---

## 📋 STEP 3 — Create the Model Class

#### `src/main/java/com/connector/lab/model/UserAccount.java`
```java
package com.connector.lab.model;

import java.sql.Timestamp;

public class UserAccount {
    private int id;
    private String username;
    private String email;
    private String role;
    private Timestamp createdAt;

    public UserAccount() {}

    public UserAccount(String username, String email, String role) {
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public UserAccount(int id, String username, String email, String role, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("[User #%d] %-12s | %-22s | Role: %-10s | Joined: %s",
                id, username, email, role, createdAt);
    }
}
```

---

## 📋 STEP 4 — Implement the Data Access Object (DAO)

#### `src/main/java/com/connector/lab/dao/UserAccountDAO.java`
```java
package com.connector.lab.dao;

import com.connector.lab.model.UserAccount;
import java.util.List;
import java.util.Optional;

public interface UserAccountDAO {
    void initializeTable();
    UserAccount create(UserAccount user);
    Optional<UserAccount> findById(int id);
    List<UserAccount> findAll();
    boolean updateEmail(int id, String newEmail);
    boolean delete(int id);
}
```

#### `src/main/java/com/connector/lab/dao/JdbcUserAccountDAO.java`
```java
package com.connector.lab.dao;

import com.connector.lab.config.DatabaseConfig;
import com.connector.lab.model.UserAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserAccountDAO implements UserAccountDAO {

    @Override
    public void initializeTable() {
        String ddl = "CREATE TABLE IF NOT EXISTS user_accounts ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "username VARCHAR(64) NOT NULL, "
                + "email VARCHAR(100) NOT NULL, "
                + "role VARCHAR(30) NOT NULL, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            System.out.println("[DAO] Table 'user_accounts' initialized successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database table", e);
        }
    }

    @Override
    public UserAccount create(UserAccount user) {
        String sql = "INSERT INTO user_accounts (username, email, role) VALUES (?, ?, ?)";

        // Request auto-generated ID from the database
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getRole());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
            return user;

        } catch (SQLException e) {
            System.err.println("[ERROR CREATE] " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserAccount> findById(int id) {
        String sql = "SELECT id, username, email, role, created_at FROM user_accounts WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR FIND] " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<UserAccount> findAll() {
        List<UserAccount> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role, created_at FROM user_accounts ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ERROR FIND ALL] " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean updateEmail(int id, String newEmail) {
        String sql = "UPDATE user_accounts SET email = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newEmail);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR UPDATE] " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM user_accounts WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERROR DELETE] " + e.getMessage());
            return false;
        }
    }

    private UserAccount mapResultSetToUser(ResultSet rs) throws SQLException {
        return new UserAccount(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getTimestamp("created_at")
        );
    }
}
```

---

## 📋 STEP 5 — Run the Application (`AppMain.java`)

#### `src/main/java/com/connector/lab/AppMain.java`
```java
package com.connector.lab;

import com.connector.lab.dao.JdbcUserAccountDAO;
import com.connector.lab.dao.UserAccountDAO;
import com.connector.lab.model.UserAccount;

import java.util.List;

public class AppMain {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   Enterprise JDBC Data Access Layer Demo        ");
        System.out.println("=================================================");

        UserAccountDAO dao = new JdbcUserAccountDAO();

        // 1. Initialize Table
        dao.initializeTable();

        // 2. CREATE
        System.out.println("\n--- Step 1: Inserting Accounts ---");
        UserAccount u1 = dao.create(new UserAccount("alice_admin", "alice@enterprise.com", "ADMIN"));
        UserAccount u2 = dao.create(new UserAccount("bob_teller", "bob@enterprise.com", "TELLER"));
        UserAccount u3 = dao.create(new UserAccount("charlie_dev", "charlie@enterprise.com", "DEVELOPER"));

        System.out.println("Inserted: " + u1);
        System.out.println("Inserted: " + u2);
        System.out.println("Inserted: " + u3);

        // 3. READ (Find All)
        System.out.println("\n--- Step 2: Listing All Accounts ---");
        List<UserAccount> accounts = dao.findAll();
        accounts.forEach(System.out::println);

        // 4. UPDATE
        System.out.println("\n--- Step 3: Updating Email for User #2 ---");
        boolean updated = dao.updateEmail(u2.getId(), "bob.teller.new@enterprise.com");
        System.out.println("Update successful: " + updated);
        dao.findById(u2.getId()).ifPresent(u -> System.out.println("Refreshed Record: " + u));

        // 5. DELETE
        System.out.println("\n--- Step 4: Deleting User #3 ---");
        boolean deleted = dao.delete(u3.getId());
        System.out.println("Deleted ID #" + u3.getId() + ": " + deleted);

        // 6. FINAL STATE
        System.out.println("\n--- Step 5: Final Database State ---");
        dao.findAll().forEach(System.out::println);

        System.out.println("\nJDBC Lab execution completed successfully!");
    }
}
```

---

## 🧪 Verification & Expected Output

Run `AppMain.java` inside **IntelliJ IDEA** or **Eclipse IDE**:

```
=================================================
   Enterprise JDBC Data Access Layer Demo        
=================================================
[DAO] Table 'user_accounts' initialized successfully.

--- Step 1: Inserting Accounts ---
Inserted: [User #1] alice_admin  | alice@enterprise.com   | Role: ADMIN      | Joined: null
Inserted: [User #2] bob_teller   | bob@enterprise.com     | Role: TELLER     | Joined: null
Inserted: [User #3] charlie_dev  | charlie@enterprise.com | Role: DEVELOPER  | Joined: null

--- Step 2: Listing All Accounts ---
[User #1] alice_admin  | alice@enterprise.com   | Role: ADMIN      | Joined: 2026-09-06 08:15:00.0
[User #2] bob_teller   | bob@enterprise.com     | Role: TELLER     | Joined: 2026-09-06 08:15:00.0
[User #3] charlie_dev  | charlie@enterprise.com | Role: DEVELOPER  | Joined: 2026-09-06 08:15:00.0

--- Step 3: Updating Email for User #2 ---
Update successful: true
Refreshed Record: [User #2] bob_teller   | bob.teller.new@enterprise.com | Role: TELLER     | Joined: 2026-09-06 08:15:00.0

--- Step 4: Deleting User #3 ---
Deleted ID #3: true

--- Step 5: Final Database State ---
[User #1] alice_admin  | alice@enterprise.com          | Role: ADMIN      | Joined: 2026-09-06 08:15:00.0
[User #2] bob_teller   | bob.teller.new@enterprise.com | Role: TELLER     | Joined: 2026-09-06 08:15:00.0

JDBC Lab execution completed successfully!
```
