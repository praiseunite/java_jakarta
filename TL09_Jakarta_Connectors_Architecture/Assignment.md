# Session 8 Assignment: Enterprise Movie Database Management System (CRUD with JDBC)

## 📝 Assignment Overview

This assignment evaluates your mastery of Java Database Connectivity (JDBC), the Data Access Object (DAO) pattern, and database portability across multiple enterprise database engines (MySQL and Microsoft SQL Server).

Based on the textbook **Try It Yourself** requirement:
> *"Create a table named ‘Movies’ and perform Create, Retrieve, Update, and Delete (CRUD) operations on the table with the details of five movies using JDBC in a Jakarta based application program with MySQL as well as SQL Server databases."*

You will build an enterprise **Movie Management System** that persists, queries, updates, and deletes movie catalog records using clean, secure JDBC code.

---

## 🏢 Database Schema: `Movies` Table

Your application must create and interact with a table named `Movies` with the following columns:

```sql
CREATE TABLE Movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    director VARCHAR(100) NOT NULL,
    release_year INT NOT NULL,
    genre VARCHAR(50) NOT NULL,
    rating DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
*(Note for Microsoft SQL Server: use `IDENTITY(1,1)` instead of `AUTO_INCREMENT`).*

---

## 📋 Specific Requirements

### 1. The 5 Mandatory Initial Movie Records
Your application must populate the database with at least the following five movies:
1. **Title:** *Inception* | **Director:** *Christopher Nolan* | **Year:** *2010* | **Genre:** *Sci-Fi* | **Rating:** *8.8*
2. **Title:** *The Shawshank Redemption* | **Director:** *Frank Darabont* | **Year:** *1994* | **Genre:** *Drama* | **Rating:** *9.3*
3. **Title:** *The Matrix* | **Director:** *Lana & Lilly Wachowski* | **Year:** *1999* | **Genre:** *Sci-Fi* | **Rating:** *8.7*
4. **Title:** *The Godfather* | **Director:** *Francis Ford Coppola* | **Year:** *1972* | **Genre:** *Crime* | **Rating:** *9.2*
5. **Title:** *Spirited Away* | **Director:** *Hayao Miyazaki* | **Year:** *2001* | **Genre:** *Animation* | **Rating:** *8.6*

---

### 2. Complete CRUD Operation Workflow
Your application runner (`MovieApp.java`) must sequentially demonstrate:
1. **Table Creation (DDL):** Checks if `Movies` table exists; if not, creates it using `Statement.execute()`.
2. **CREATE (C):** Inserts the 5 movie records using `PreparedStatement` with parameterized values and auto-generated key retrieval.
3. **RETRIEVE (R):**
   * List all movies in the catalog formatted in a clean ASCII table.
   * Search for a specific movie by Title using a parameterized query.
4. **UPDATE (U):**
   * Update the rating or genre of a specific movie (e.g., update *The Matrix* rating from 8.7 to 8.9).
   * Retrieve the updated movie to verify the change.
5. **DELETE (D):**
   * Delete one movie record by ID (e.g., remove movie ID #5).
   * List remaining movies to prove the deletion was persisted.

---

### 3. Multi-Database Portability (MySQL & SQL Server)
Your application must support connecting to both **MySQL** and **Microsoft SQL Server** by switching a database configuration property (or properties file):

#### Maven Dependencies required in `pom.xml`:
```xml
<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
</dependency>

<!-- Microsoft SQL Server Driver -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.1.jre11</version>
</dependency>

<!-- Embedded H2 Driver (Optional fallback for zero-install testing) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

#### Connection Profiles:
* **MySQL URL:** `jdbc:mysql://localhost:3306/movie_db?useSSL=false&serverTimezone=UTC`
* **SQL Server URL:** `jdbc:sqlserver://localhost:1433;databaseName=movie_db;trustServerCertificate=true`

---

## 📂 Expected Directory Structure

```
Session_08_Assignment/
├── pom.xml
├── src/main/resources/
│   └── database.properties            (Configures DB engine: mysql or sqlserver)
└── src/main/java/com/assignment/movie/
    ├── config/
    │   └── ConnectionFactory.java     (Reads properties and returns Connection)
    ├── model/
    │   └── Movie.java                 (POJO representing a Movie)
    ├── dao/
    │   ├── MovieDAO.java              (Interface declaring CRUD methods)
    │   └── JdbcMovieDAO.java          (JDBC implementation)
    └── MovieApp.java                  (Main application demonstration)
```

---

## 🧪 Edge Cases & Best Practices

1. **SQL Injection Prevention:**
   * Never concatenate string variables directly into SQL statements. Always use `PreparedStatement` with positional `?` placeholders.
2. **Resource Leak Prevention:**
   * Every `Connection`, `Statement`, and `ResultSet` must be encapsulated in a `try-with-resources` block.
3. **Transaction Safety:**
   * When inserting multiple records, consider using batch updates (`pstmt.addBatch()` and `pstmt.executeBatch()`) for optimized network round-trips.

---

## 📊 Grading Rubric

| Criteria | Points | Description |
| :--- | :---: | :--- |
| **Database Connectivity & Portability** | **20 pts** | Clean `ConnectionFactory` supporting MySQL, SQL Server, and embedded options. |
| **Table DDL & Schema Management** | **15 pts** | Correct `Movies` table schema creation handling database-specific primary keys. |
| **CREATE & Read Operations** | **25 pts** | Inserts 5 movies with auto-generated IDs; retrieves all records and searches by title. |
| **UPDATE & DELETE Operations** | **20 pts** | Modifies existing record and deletes a record, confirming changes in the database. |
| **Code Architecture & Exception Safety** | **20 pts** | DAO design pattern, try-with-resources, prepared statements, and clear error logs. |
| **Total** | **100 pts** | |

---

## 📤 Submission Instructions

1. Export your project into a zip file named:
   `LastName_FirstName_Session08_Assignment.zip`
2. Include a **`README.md`** explaining:
   * How to switch between MySQL and SQL Server configurations.
   * How to run `MovieApp.java`.
3. Include screenshots showing:
   * Execution output with all 5 inserted movies.
   * The update confirmation.
   * The delete confirmation and final movie table.
4. Upload the archive to your student portal before the due date.
