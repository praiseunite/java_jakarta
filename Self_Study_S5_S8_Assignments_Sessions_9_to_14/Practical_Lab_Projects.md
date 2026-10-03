# Self-Study Block S5–S8: Hands-On Practical Lab Projects

**Manual Reference:** S5–S8 Practical Revision Sessions (Sessions 9 to 14)  
**Objective:** Master enterprise data integrity, programmatic transactions, decoupled CDI architectures, and modular packaging through three complete projects.

---

## 🛠️ Lab Project 1: Validated Hospital Patient Triage Service
**Concepts Tested:** Custom Bean Validation annotations, class-level validators, Bean-Managed Transactions (`UserTransaction`), manual rollback.

### Scenario
A hospital admission intake system must enforce strict medical data constraints:
1. Patient National Health ID must adhere to the format `NHID-XXXX-YYYY` (enforced via a custom constraint `@ValidHealthId`).
2. Blood pressure readings must validate that Systolic is strictly greater than Diastolic (enforced via a class-level validator `@ValidBloodPressure`).
3. The admission transaction is executed with **Bean-Managed Transactions (BMT)**. If either bed assignment or insurance verification fails, the admission is rolled back using `utx.rollback()`.

---

### Implementation Code

#### 1. Custom Constraint Annotation & Validator
```java
package com.aptech.hospital.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidHealthId.HealthIdValidator.class)
@Documented
public @interface ValidHealthId {
    String message() default "Invalid National Health ID format. Must match 'NHID-XXXX-YYYY'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class HealthIdValidator implements ConstraintValidator<ValidHealthId, String> {
        private static final String PATTERN = "^NHID-\\d{4}-[A-Z]{4}$";

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) return false;
            return value.matches(PATTERN);
        }
    }
}
```

#### 2. Validated Patient Admission Model
```java
package com.aptech.hospital.model;

import com.aptech.hospital.validation.ValidHealthId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PatientAdmissionDTO {

    @NotBlank(message = "Patient name is required")
    private String fullName;

    @ValidHealthId
    private String healthId;

    @NotNull
    @Min(value = 60, message = "Systolic BP below safe human threshold")
    @Max(value = 250, message = "Systolic BP above measurable threshold")
    private Integer systolicBP;

    @NotNull
    @Min(value = 40, message = "Diastolic BP below safe human threshold")
    @Max(value = 150, message = "Diastolic BP above measurable threshold")
    private Integer diastolicBP;

    public PatientAdmissionDTO(String fullName, String healthId, Integer systolicBP, Integer diastolicBP) {
        this.fullName = fullName;
        this.healthId = healthId;
        this.systolicBP = systolicBP;
        this.diastolicBP = diastolicBP;
    }

    // Getters and setters
    public String getFullName() { return fullName; }
    public String getHealthId() { return healthId; }
    public Integer getSystolicBP() { return systolicBP; }
    public Integer getDiastolicBP() { return diastolicBP; }
}
```

#### 3. BMT Transactional Admission Service
```java
package com.aptech.hospital.service;

import com.aptech.hospital.model.PatientAdmissionDTO;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.transaction.UserTransaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class PatientAdmissionServiceBean {

    @Resource
    private UserTransaction utx;

    public boolean processAdmission(PatientAdmissionDTO dto) {
        // Step 1: Validate payload using Bean Validation API
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<PatientAdmissionDTO>> violations = validator.validate(dto);

        if (!violations.isEmpty()) {
            System.err.println("[AdmissionService] Validation failed for: " + dto.getFullName());
            for (ConstraintViolation<PatientAdmissionDTO> v : violations) {
                System.err.println(" -> " + v.getPropertyPath() + ": " + v.getMessage());
            }
            return false;
        }

        // Step 2: Execute programmatic BMT transaction
        try {
            utx.begin();
            System.out.println("[AdmissionService] Started UserTransaction for: " + dto.getHealthId());

            allocateBed(dto);
            verifyInsurance(dto);

            utx.commit();
            System.out.println("[AdmissionService] Admission committed successfully.");
            return true;
        } catch (Exception ex) {
            System.err.println("[AdmissionService] Error occurred: " + ex.getMessage() + ". Rolling back.");
            try {
                utx.rollback();
            } catch (Exception rbEx) {
                System.err.println("Rollback error: " + rbEx.getMessage());
            }
            return false;
        }
    }

    private void allocateBed(PatientAdmissionDTO dto) {
        System.out.println(" -> Allocated bed in Ward 3 for " + dto.getFullName());
    }

    private void verifyInsurance(PatientAdmissionDTO dto) {
        if (dto.getHealthId().endsWith("FAIL")) {
            throw new IllegalStateException("Insurance authorization rejected by payer network");
        }
        System.out.println(" -> Insurance pre-authorization approved.");
    }
}
```

---

## 🛠️ Lab Project 2: Pluggable Multi-Gateway Payment System
**Concepts Tested:** CDI Qualifiers, Interceptors (`@AroundInvoke`), CDI Async Events (`@ObservesAsync`).

### Architecture
```
              ┌───────────────────────────┐
              │    CheckoutController     │
              └─────────────┬─────────────┘
                            │ @Inject @Stripe
                            ▼
     ┌──────────────────────────────────────────────┐
     │  @Logged (AuditInterceptor intercepts call)  │
     │  StripePaymentGateway.processPayment()       │
     └──────────────────────┬───────────────────────┘
                            │ event.fireAsync(PaymentSuccessEvent)
                            ▼
         ┌──────────────────────────────────────┐
         │ @ObservesAsync onPaymentSuccess(...) │
         │ (Generates invoice & sends receipt)  │
         └──────────────────────────────────────┘
```

#### 1. Custom Qualifier & Interceptor Binding
```java
package com.aptech.payment.cdi;

import jakarta.inject.Qualifier;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Stripe {}

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Logged {}
```

#### 2. Logging & Performance Interceptor
```java
package com.aptech.payment.cdi;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Logged
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    @AroundInvoke
    public Object profileMethod(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        String methodName = ctx.getMethod().getName();
        System.out.println("[Interceptor] ENTER: " + methodName);
        try {
            return ctx.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            System.out.println("[Interceptor] EXIT: " + methodName + " executed in " + duration + "ms");
        }
    }
}
```

#### 3. Gateway Implementation with Asynchronous Event Dispatch
```java
package com.aptech.payment.service;

import com.aptech.payment.cdi.Logged;
import com.aptech.payment.cdi.Stripe;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import java.math.BigDecimal;

@Stripe
@ApplicationScoped
public class StripePaymentGateway {

    @Inject
    private Event<PaymentReceiptEvent> receiptEvent;

    @Logged
    public boolean processPayment(String orderId, BigDecimal amount) {
        System.out.println("[StripeGateway] Processing charge of $" + amount + " for order " + orderId);
        
        // Fire asynchronous decoupled receipt event
        receiptEvent.fireAsync(new PaymentReceiptEvent(orderId, amount, "STRIPE-TX-" + System.currentTimeMillis()));
        return true;
    }
}
```

#### 4. Decoupled Asynchronous Receipt Observer
```java
package com.aptech.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;

@ApplicationScoped
public class ReceiptNotificationObserver {

    public void onPaymentCompleted(@ObservesAsync PaymentReceiptEvent event) {
        System.out.println("[AsyncObserver] Dispatched PDF receipt for order: " + event.getOrderId() 
                + " (Transaction ID: " + event.getTransactionRef() + ")");
    }
}
```

---

## 🛠️ Lab Project 3: Secured Multi-Module Enterprise Skinny WAR
**Concepts Tested:** Skinny WAR structure, `beans.xml`, Role-Based Access Control (`@RolesAllowed`), `SecurityContext`.

### 1. Maven Project Layout (`pom.xml`)
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.aptech.enterprise</groupId>
    <artifactId>healthcare-portal</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>

    <dependencies>
        <dependency>
            <groupId>jakarta.platform</groupId>
            <artifactId>jakarta.jakartaee-api</artifactId>
            <version>10.0.0</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>healthcare-portal</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. Role-Secured Medical Record Bean
```java
package com.aptech.healthcare.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;

@Stateless
@DeclareRoles({"DOCTOR", "ADMIN", "NURSE"})
public class MedicalRecordServiceBean {

    @Inject
    private SecurityContext securityContext;

    @RolesAllowed({"DOCTOR", "ADMIN"})
    public String accessConfidentialDossier(String patientId) {
        String caller = securityContext.getCallerPrincipal().getName();
        System.out.println("[SecurityService] Authorized caller '" + caller + "' accessed records for " + patientId);
        return "DOSSIER-PATIENT-" + patientId + ": Diagnosis: Acute Hypertension.";
    }

    @RolesAllowed({"NURSE", "DOCTOR", "ADMIN"})
    public void recordVitals(String patientId, String vitals) {
        System.out.println("[SecurityService] Vitals recorded for " + patientId + " by " + securityContext.getCallerPrincipal().getName());
    }
}
```

---

## 📋 Verification & Expected WildFly Log Output

```log
[AdmissionService] Started UserTransaction for: NHID-4421-WXYZ
 -> Allocated bed in Ward 3 for Johnathan Vance
 -> Insurance pre-authorization approved.
[AdmissionService] Admission committed successfully.
[Interceptor] ENTER: processPayment
[StripeGateway] Processing charge of $450.00 for order ORD-9921
[Interceptor] EXIT: processPayment executed in 12ms
[AsyncObserver] Dispatched PDF receipt for order: ORD-9921 (Transaction ID: STRIPE-TX-1727958920112)
[SecurityService] Authorized caller 'dr_smith' accessed records for PATIENT-771
```
