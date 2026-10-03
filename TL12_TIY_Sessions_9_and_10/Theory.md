# TL12 — Try It Yourself: Review of Sessions 9 & 10

**Manual Reference:** JAKARTA EE – TL12  
**Book Coverage:** Session 9 (Bean Validation) + Session 10 (EJB Transactions & JNDI)

---

## 📚 Recap — Session 9: Bean Validation

### Core Concept
Bean Validation (`jakarta.validation`) allows constraint annotations to be placed directly on Java fields, method parameters, and return values. The container (or programmatic `Validator`) automatically verifies these rules.

### Built-in Constraints Quick Reference

| Annotation | Applicable Types | Validates |
|---|---|---|
| `@NotNull` | Any | Field is not `null` |
| `@NotBlank` | `String` | Not null AND contains non-whitespace |
| `@NotEmpty` | String, Collection | Not null AND not empty |
| `@Size(min, max)` | String, Collection | Size/length within bounds |
| `@Min(value)` / `@Max(value)` | Numbers | Numeric bounds |
| `@Email` | `String` | Valid email format |
| `@Pattern(regexp)` | `String` | Matches regex |
| `@Past` / `@Future` | Date/Time | Before or after today |
| `@Positive` / `@Negative` | Numbers | > 0 or < 0 |

### Creating a Custom Constraint
A custom constraint requires:
1. An annotation (`@interface`) meta-annotated with `@Constraint(validatedBy = MyValidator.class)`
2. A validator class implementing `ConstraintValidator<MyAnnotation, TargetType>`

### Repeating Annotations
Since Jakarta EE 8, constraints can be repeated on the same element:
```java
@Pattern(regexp = "^[A-Z].*", message = "Must start with uppercase")
@Pattern(regexp = ".*[0-9]$", message = "Must end with a digit")
private String code;
```

### Temporal Constraints with `ClockProvider`
The `ClockProvider` SPI allows `@Past`/`@Future` to use a custom clock (essential for unit testing time-sensitive validation).

---

## 📚 Recap — Session 10: EJB Transactions & JNDI

### Transaction Management Types

| Type | Annotation | Who controls `begin/commit/rollback`? |
|---|---|---|
| **Container-Managed (CMT)** | `@TransactionManagement(CONTAINER)` (default) | The EJB container — fully automatic |
| **Bean-Managed (BMT)** | `@TransactionManagement(BEAN)` | The developer via `UserTransaction` |

### CMT Propagation Attributes

| Attribute | New tx started? | Existing tx used? |
|---|---|---|
| `REQUIRED` (default) | Yes, if none exists | Yes, joins existing |
| `REQUIRES_NEW` | Always starts a new one | Suspends existing |
| `MANDATORY` | No | Yes — throws exception if no tx exists |
| `NOT_SUPPORTED` | No | Suspends any existing tx |
| `SUPPORTS` | No | Uses existing if present |
| `NEVER` | No | Throws exception if tx exists |

### JNDI Lookup
JNDI (`javax.naming`) allows runtime resource discovery. Standard portable names in Jakarta EE:

```java
InitialContext ctx = new InitialContext();
DataSource ds  = (DataSource) ctx.lookup("java:comp/env/jdbc/MyDS");
UserTransaction utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
```

---

## 🔁 TIY Exercise Checklist

- [ ] Create a `Product` entity with `@NotNull`, `@Size`, `@Positive`, and `@Email` on appropriate fields. Run programmatic validation and display violation messages.
- [ ] Create a custom `@ValidSKU` constraint that ensures SKU format matches `[A-Z]{3}-[0-9]{4}`.
- [ ] Build a BMT `@Stateless` EJB that transfers funds between two accounts. Demonstrate that a failed second operation triggers `utx.rollback()`, leaving the first operation also undone.
- [ ] Use JNDI to programmatically look up your DataSource and print the JDBC URL.

---

## ❓ Self-Test Questions

1. What is the difference between `@NotNull` and `@NotBlank`?
2. Why must a Validator class implement two generic type parameters?
3. In CMT, what happens when an unchecked (system) exception is thrown from an EJB method?
4. What does `@TransactionAttribute(REQUIRES_NEW)` do to an existing transaction?
5. What is the Java standard package for JNDI? (`javax.naming` — it is part of the JDK, NOT Jakarta EE!)
