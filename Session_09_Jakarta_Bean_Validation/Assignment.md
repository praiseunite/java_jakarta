# Session 9 Assignment: Advanced Validation and Custom Constraints

## 📝 Assignment Overview

This assignment evaluates your ability to apply Jakarta Bean Validation in a real-world scenario by combining standard constraints, developing custom constraints, and verifying them programmatically.

Based on the textbook **Try It Yourself** requirement:
> *"Write a program to create a custom constraint in Bean Validation to check a product SKU format."*

You will implement a **Product Inventory Validator** for an e-commerce platform.

---

## 🏢 Business Scenario

The warehouse management system processes incoming product records. To ensure data integrity, every product must strictly adhere to the company's validation rules before being inserted into the database. You will create the `Product` model, apply constraints, and write a custom validator specifically for the company's Stock Keeping Unit (SKU) format.

### Custom SKU Rule:
The company SKU must start with the prefix `PROD-`, followed by exactly 4 alphanumeric characters (uppercase letters and digits only). 
*Valid examples:* `PROD-X9A2`, `PROD-1234`, `PROD-ABCD`
*Invalid examples:* `PROD-xyz`, `PRD-1234`, `PROD-12345`

---

## 📋 Specific Requirements

### 1. Build the Custom Constraint `@ValidProductSKU`
* Define an annotation `@ValidProductSKU` targeting `ElementType.FIELD`.
* Implement a `ProductSKUValidator` class that implements `ConstraintValidator<ValidProductSKU, String>`.
* Use regular expressions or custom logic to enforce the rule: starts with `PROD-` followed by exactly 4 uppercase alphanumeric characters.

### 2. Create the `Product` Model
Create a `Product` class with the following fields and constraints:
* `String sku`: Must be annotated with `@ValidProductSKU` and cannot be null.
* `String name`: Must not be blank, and must be between 5 and 100 characters in length.
* `String category`: Must be a valid format. Ensure it only contains uppercase letters (e.g., `ELECTRONICS`, `FURNITURE`) using the `@Pattern` constraint (e.g., `^[A-Z]+$`).
* `double price`: Must be positive (`@Positive`).
* `int stockQuantity`: Must be positive or zero (`@PositiveOrZero`).

### 3. Create the Validation Tester
Create an executable `ProductValidatorTest` class with a `main` method.
* Retrieve a `Validator` instance using `Validation.buildDefaultValidatorFactory()`.
* Create at least two `Product` objects:
  * **Product A:** Completely valid.
  * **Product B:** Invalid (violates multiple rules, including an invalid SKU format).
* Run validation on both products.
* Iterate through the `ConstraintViolation` set and print the validation error messages to the console.

---

## 📂 Expected Directory Structure

Ensure your project is structured as follows before submission:

```
Session_09_Assignment/
├── pom.xml
└── src/main/java/com/ecommerce/inventory/
    ├── Product.java                (Model)
    ├── ValidProductSKU.java        (Annotation)
    ├── ProductSKUValidator.java    (Validator logic)
    └── ProductValidatorTest.java   (Main test class)
```

## 💯 Grading Criteria

1. **Custom Constraint (40%)**: The custom annotation and validator correctly identify valid and invalid SKU formats.
2. **Model Setup (30%)**: Correct standard annotations applied to all model fields.
3. **Execution & Testing (30%)**: The programmatic validation correctly initializes, detects, and prints out the specific violation messages.
