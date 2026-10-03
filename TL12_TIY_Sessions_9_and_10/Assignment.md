# TL12 Assignment: Validated Batch Processor with Transaction Rollback

## 📝 Assignment Overview

Build a batch file processing service that validates each record and processes them in a single BMT transaction. Any invalid record should cause the entire batch to roll back.

## 📋 Requirements

1. **The `BatchRecord` class:** Fields — `productCode` (`@Pattern(regexp="[A-Z]{3}-[0-9]{4}")`), `quantity` (`@Positive`), `price` (`@DecimalMin("0.01")`).
2. **The `BatchProcessorService` EJB (`@Stateless`, `@TransactionManagement(BEAN)`):** Accept a `List<BatchRecord>`, validate each, begin one BMT transaction, "persist" all records (print them), commit at the end. If any record fails validation, rollback.
3. **Test data:** Mix of valid and invalid records to prove rollback works.

## 💯 Grading

| Area | Marks |
|---|---|
| Constraint annotations on `BatchRecord` are correct | 30% |
| BMT transaction begins before first persist and rolls back on any violation | 40% |
| Output clearly shows which record caused rollback | 30% |
