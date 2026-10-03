# TL12 Class Task: Validation + Transaction Integration

## 📌 Task Overview

Build a single **Customer Registration Service** that integrates both Bean Validation and transactional persistence — both must succeed together or both must roll back.

---

## 📋 STEP 1 — The Validated Entity

```java
package com.bank.model;

import jakarta.validation.constraints.*;

public class Customer {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50)
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank
    private String email;

    @Positive(message = "Opening balance must be positive")
    private double openingBalance;

    // getters/setters
}
```

---

## 📋 STEP 2 — The Transactional Service with Programmatic Validation

```java
package com.bank.service;

import com.bank.model.Customer;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class CustomerRegistrationService {

    @Resource
    private UserTransaction utx;

    public void register(Customer customer) throws Exception {
        // Step 1: Validate programmatically BEFORE opening transaction
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        if (!violations.isEmpty()) {
            violations.forEach(v -> System.out.println("VIOLATION: " + v.getMessage()));
            throw new IllegalArgumentException("Customer data is invalid!");
        }

        // Step 2: Only proceed with transaction if validation passed
        utx.begin();
        try {
            System.out.println("Persisting customer: " + customer.getFullName());
            // em.persist(customer); // JPA persist call here
            utx.commit();
        } catch (Exception e) {
            utx.rollback();
            throw e;
        }
    }
}
```

---

## ✅ Test Scenarios

1. Call `register()` with a valid `Customer` → Should commit successfully.
2. Call `register()` with `email = "not-an-email"` → Should throw `IllegalArgumentException` before the transaction even starts.
3. Call `register()` with `openingBalance = -500` → Should fail validation and never open a transaction.
