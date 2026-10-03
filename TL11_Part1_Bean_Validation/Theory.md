# Session 9: Jakarta Bean Validation

Welcome to **Session 9: Jakarta Bean Validation**. In enterprise systems, data validation was historically duplicated across three layers: JavaScript in the browser, check methods in the EJB layer, and table constraints in the database. **Jakarta Bean Validation** solves this redundancy: you declare business constraints *once* on your domain classes, and the container automatically enforces them across the web tier (JSF), business tier (EJB), and database persistence tier (JPA).

---

## 🎯 Learning Objectives

By the end of this session, you will be able to:
1. **Define** the Jakarta Bean Validation architecture and explain "write once, enforce everywhere".
2. **Apply** standard built-in constraint annotations (`@NotNull`, `@Size`, `@Min`, `@Pattern`, `@Email`).
3. **Distinguish** between field, property (getter), and class-level constraint placement.
4. **Construct** custom validation annotations and implement `ConstraintValidator` logic.
5. **Explain** container integration: automatic JSF Phase 3 validation, EJB method validation, and JPA pre-persist hooks.
6. **Perform** programmatic validation using the `Validator` and `ConstraintViolation` API.

---

## 1. The "Validate Once, Enforce Everywhere" Paradigm

Without Bean Validation, an airport would force you to unpack your luggage at the front door, unpack it again at the check-in desk, unpack it at the boarding gate, and unpack it on the plane. Each agent writes their own rules. With **Jakarta Bean Validation**, there is one authoritative specification for allowed items. The luggage tag declares the constraints; every checkpoint (web form, business logic, persistence store) applies the exact same rules automatically.

Jakarta Bean Validation (implemented by reference providers like **Hibernate Validator**) attaches declarative constraint metadata to Java bean fields, getters, or classes:

| Application Layer | How Bean Validation Integrates | Failure Behavior |
| :--- | :--- | :--- |
| **Presentation Tier (JSF Facelets)** | JSF reads model constraints automatically during **Phase 3 (Process Validation)**. | Stops request; displays localized error messages in `<h:messages>` without hitting EJB. |
| **Business Tier (EJB / CDI)** | Container intercepts method calls with `@ValidateExecutable` and parameter `@Valid`. | Throws `ConstraintViolationException` before executing method body. |
| **Persistence Tier (JPA)** | JPA automatically runs the validator before executing SQL `INSERT` or `UPDATE`. | Aborts the transaction before sending invalid SQL to the database. |

---

## 2. Standard Built-in Constraints

The `jakarta.validation.constraints.*` package provides an extensive library of production-ready rules:

| Annotation | Supported Types | Description |
| :--- | :--- | :--- |
| `@NotNull` | Any Object | Value must not be `null` (allows empty strings). |
| `@NotEmpty` | String, Collection, Map, Array | Must not be `null` and size/length must be $> 0$. |
| `@NotBlank` | String | Must not be `null` and must contain at least one non-whitespace character. |
| `@Size(min=X, max=Y)` | String, Collection, Array | Ensures string length or collection size is within specified bounds. |
| `@Min(val)` / `@Max(val)` | Numeric types (int, long, BigDecimal) | Ensures numeric value is $\ge$ min or $\le$ max. |
| `@Positive` / `@PositiveOrZero` | Numeric types | Value must be strictly positive ($> 0$) or non-negative ($\ge 0$). |
| `@Email` | String | Validates RFC-compliant email address structure. |
| `@Pattern(regexp="...")` | String | Validates string against a standard regular expression. |
| `@Past` / `@Future` | Date, LocalDate, Instant | Ensures date is strictly in the past (e.g., date of birth) or future (e.g., card expiry). |

### Example Model: `CustomerRegistration.java`

```java
package com.globalbank.model;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;

public class CustomerRegistration implements Serializable {

    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\d{10}$", message = "Account number must be exactly 10 digits")
    private String accountNumber;

    @NotNull(message = "Date of birth required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Positive(message = "Opening deposit must be greater than zero")
    private double initialDeposit;

    // Getters and setters...
}
```

---

## 3. Creating Custom Constraints

When business requirements exceed standard annotations (such as verifying a bank routing number checksum or enforcing password complexity), you build a **Custom Constraint** in two straightforward steps:

### Step 1: Define the Custom Annotation

```java
package com.globalbank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AccountNumberValidator.class) // Links to validator class
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountNumber {

    // Mandatory standard Bean Validation attributes:
    String message() default "Invalid bank account number check-digit";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Step 2: Implement the `ConstraintValidator`

```java
package com.globalbank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {

    @Override
    public void initialize(ValidAccountNumber constraintAnnotation) {
        // Initialization if annotation has configurable attributes
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() != 10) {
            return false; // Null or incorrect length fails
        }

        // Luhn algorithm or bank check-digit verification:
        int sum = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isDigit(c)) return false;
            sum += Character.getNumericValue(c);
        }
        return (sum % 10 == 0); // Must satisfy check-digit rule
    }
}
```

---

## 4. Programmatic Validation with the `Validator` API

While the container validates web inputs and entities automatically, batch processors or standalone utilities can invoke the validation engine manually:

```java
package com.globalbank.validation;

import com.globalbank.model.CustomerRegistration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

public class ProgrammaticValidationDemo {

    public static void main(String[] args) {
        // 1. Build the default ValidatorFactory and retrieve Validator instance
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            // 2. Instantiate bean with deliberate constraint violations
            CustomerRegistration customer = new CustomerRegistration();
            customer.setEmail("invalid-email-address"); // Fails @Email
            customer.setInitialDeposit(-50.0);          // Fails @Positive

            // 3. Validate bean and inspect violation set
            Set<ConstraintViolation<CustomerRegistration>> violations = validator.validate(customer);

            if (violations.isEmpty()) {
                System.out.println("Customer data is valid!");
            } else {
                System.err.println("Validation failed with " + violations.size() + " error(s):");
                for (ConstraintViolation<CustomerRegistration> v : violations) {
                    System.err.println(" • Field '" + v.getPropertyPath() + "': " + v.getMessage());
                }
            }
        }
    }
}
```

---

## 5. Summary

* **Bean Validation:** Declarative constraint specification (`jakarta.validation.*`) enforced across all application tiers.
* **Core Constraints:** `@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`.
* **Custom Validator:** Annotated with `@Constraint` and backed by a `ConstraintValidator<A, T>` implementation.
* **Container Hooks:** JSF validates in Phase 3; EJB validates parameters; JPA validates on pre-persist/pre-update.
* **Programmatic API:** `Validation.buildDefaultValidatorFactory().getValidator().validate(bean)`.
