# Session 10 Assignment: Bean-Managed Transactions (BMT)

## 📝 Assignment Overview

This assignment evaluates your ability to implement programmatic transactions within Enterprise JavaBeans. Container-Managed Transactions (CMT) are easy, but batch processing or complex multi-step workflows often require precise manual control.

Based on the textbook **Try It Yourself** requirements, you will create a batch processing engine using **Bean-Managed Transactions (BMT)** and the `UserTransaction` API.

---

## 🏢 Business Scenario

Your enterprise application must process an incoming CSV file containing 10,000 employee salary updates. If an error occurs (e.g., malformed data) midway through, it is unacceptable to roll back the entire 10,000 updates. Instead, you must process the updates in **chunks of 100**. 

If a chunk fails, only that specific chunk should roll back; the previous successful chunks must remain securely committed to the database, and the system should continue attempting subsequent chunks.

---

## 📋 Specific Requirements

### 1. The EJB Configuration
* Create a Stateless session bean named `BatchSalaryService`.
* Annotate the bean with `@TransactionManagement(TransactionManagementType.BEAN)` to override the CMT default.

### 2. The `UserTransaction` Resource
* Inject the JTA `UserTransaction` object using the `@Resource` annotation.
* Alternatively, look it up via JNDI at `java:comp/UserTransaction`.

### 3. The Batch Processing Logic
* Create a method `public void processBatch(List<String> employeeRecords)`.
* Iterate over the records in chunks of 10. (Use 10 instead of 100 to make testing easier).
* **For each chunk:**
  * Begin a transaction (`utx.begin()`).
  * Process the 10 records (simulate processing with a simple `System.out.println`).
  * If the record equals the string `"ERROR"`, throw a simulated `RuntimeException`.
  * If the chunk completes successfully, commit the transaction (`utx.commit()`).
  * If an exception is caught during the chunk, roll back the transaction (`utx.rollback()`) and catch/log any rollback exceptions. The loop must continue to the next chunk!

### 4. Create the Test Client
* Create a standalone client or a Servlet that invokes the `processBatch` method.
* Provide a list of 25 records.
* Intentionally make the 14th record `"ERROR"`.
* Verify the output: 
  * Chunk 1 (Records 1-10) should COMMIT.
  * Chunk 2 (Records 11-20, containing the error) should ROLLBACK.
  * Chunk 3 (Records 21-25) should COMMIT.

---

## 📂 Expected Directory Structure

```
Session_10_Assignment/
├── pom.xml
└── src/main/java/com/enterprise/batch/
    ├── BatchSalaryService.java     (BMT Stateless Bean)
    └── BatchTester.java            (Client/Runner)
```

## 💯 Grading Criteria

1. **BMT Configuration (20%)**: Bean is correctly configured for Bean-Managed Transactions.
2. **Transaction Demarcation (40%)**: `utx.begin()`, `utx.commit()`, and `utx.rollback()` are used accurately in a chunked loop.
3. **Exception Isolation (40%)**: A failure in one chunk successfully triggers a rollback for *only* that chunk, while allowing subsequent chunks to process and commit normally.
