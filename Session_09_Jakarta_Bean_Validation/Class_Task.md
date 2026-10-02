# Session 9 Class Task: Implementing and Testing Bean Validation

## 📌 Task Overview

In this hands-on lab, you will apply Jakarta Bean Validation to ensure that incoming data meets business rules before it reaches your core application logic.

You will:
1. **Apply standard constraints** to a Java Bean.
2. **Create a custom constraint** and corresponding validator.
3. **Write a standalone test client** using the Programmatic `Validator` API to test both valid and invalid scenarios.

---

## 🛠️ Prerequisites & Environment Setup

* **Java JDK:** Version 11 or 17 installed and configured (`JAVA_HOME`).
* **IDE:** IntelliJ IDEA (Ultimate or Community) OR Eclipse IDE for Enterprise Java.
* **Build Tool:** Apache Maven (bundled in both IntelliJ and Eclipse).

---

## 🏗️ Project Architecture

```
Session_09_Validation_Lab/
├── pom.xml
└── src/main/java/com/bank/validation/
    ├── EmployeeRegistration.java      (Model class with standard constraints)
    ├── ValidDepartmentCode.java       (Custom constraint annotation)
    ├── DepartmentCodeValidator.java   (Custom validator implementation)
    └── ValidationAppMain.java         (Programmatic testing client)
```

---

## 📋 STEP 1 — Create the Maven Project (`pom.xml`)

Create a standard Maven project named `Session_09_Validation_Lab`. Add the required dependencies for Hibernate Validator (the reference implementation for Jakarta Bean Validation) and the Jakarta EL expression language.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bank.validation</groupId>
    <artifactId>Session_09_Validation_Lab</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- Jakarta Bean Validation API -->
        <dependency>
            <groupId>jakarta.validation</groupId>
            <artifactId>jakarta.validation-api</artifactId>
            <version>3.0.2</version>
        </dependency>
        
        <!-- Hibernate Validator (Implementation) -->
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>8.0.1.Final</version>
        </dependency>
        
        <!-- Jakarta Expression Language (Required by Hibernate Validator for message interpolation) -->
        <dependency>
            <groupId>org.glassfish.expressly</groupId>
            <artifactId>expressly</artifactId>
            <version>5.0.0</version>
        </dependency>
    </dependencies>
</project>
```

---

## 📋 STEP 2 — Implement the Custom Constraint

Before creating the main model, we need to create a custom constraint that checks if a department code is valid. Let's assume valid codes must start with "DEPT-" followed by exactly 3 digits.

### 1. Create the Annotation (`ValidDepartmentCode.java`)

```java
package com.bank.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DepartmentCodeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDepartmentCode {
    String message() default "Invalid Department Code format. Expected format: DEPT-XXX";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 2. Create the Validator (`DepartmentCodeValidator.java`)

```java
package com.bank.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DepartmentCodeValidator implements ConstraintValidator<ValidDepartmentCode, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return value.matches("^DEPT-\\d{3}$");
    }
}
```

---

## 📋 STEP 3 — Create the Model Class with Constraints

Create `EmployeeRegistration.java` and apply both standard and our custom constraints.

```java
package com.bank.validation;

import jakarta.validation.constraints.*;

public class EmployeeRegistration {

    @NotBlank(message = "Employee name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Provide a valid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Min(value = 18, message = "Employee must be at least 18 years old")
    @Max(value = 65, message = "Employee age must not exceed 65")
    private int age;

    @ValidDepartmentCode
    private String departmentCode;

    // Getters and Setters...
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
}
```

---

## 📋 STEP 4 — Implement the Programmatic Validation Client

Create `ValidationAppMain.java` to test the constraints using the `Validator` API.

```java
package com.bank.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class ValidationAppMain {
    public static void main(String[] args) {
        
        // 1. Initialize Validator
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            // 2. Create an INVALID employee record
            System.out.println("--- Testing Invalid Employee ---");
            EmployeeRegistration invalidEmp = new EmployeeRegistration();
            invalidEmp.setName("J");                     // Fails @Size
            invalidEmp.setEmail("not-an-email");         // Fails @Email
            invalidEmp.setAge(16);                       // Fails @Min
            invalidEmp.setDepartmentCode("HR-123");      // Fails @ValidDepartmentCode

            Set<ConstraintViolation<EmployeeRegistration>> violations = validator.validate(invalidEmp);
            
            for (ConstraintViolation<EmployeeRegistration> v : violations) {
                System.out.println("Property [" + v.getPropertyPath() + "]: " + v.getMessage());
            }

            // 3. Create a VALID employee record
            System.out.println("\n--- Testing Valid Employee ---");
            EmployeeRegistration validEmp = new EmployeeRegistration();
            validEmp.setName("Alice Smith");
            validEmp.setEmail("alice@bank.com");
            validEmp.setAge(30);
            validEmp.setDepartmentCode("DEPT-105");

            Set<ConstraintViolation<EmployeeRegistration>> validViolations = validator.validate(validEmp);
            if (validViolations.isEmpty()) {
                System.out.println("Valid employee passed validation successfully!");
            }
        }
    }
}
```

---

## ✅ Expected Output

When you run `ValidationAppMain.java`, the output in your console should look like this:

```
--- Testing Invalid Employee ---
Property [age]: Employee must be at least 18 years old
Property [name]: Name must be between 2 and 100 characters
Property [email]: Provide a valid email address
Property [departmentCode]: Invalid Department Code format. Expected format: DEPT-XXX

--- Testing Valid Employee ---
Valid employee passed validation successfully!
```
