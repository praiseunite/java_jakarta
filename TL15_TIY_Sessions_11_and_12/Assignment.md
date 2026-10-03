# TL15 Assignment: CDI Shopping Cart System

## 📝 Assignment

Build a shopping cart using the correct CDI scopes and inject a tax calculator using custom qualifiers.

## 📋 Requirements

1. **`@SessionScoped ShoppingCart`** — Holds a `List<String>` of item names. Must be `Serializable`. Expose `addItem()`, `removeItem()`, `getItems()`.
2. **Two Tax Calculators:** `StandardTaxCalculator` (18% VAT, `@StandardTax`) and `PremiumTaxCalculator` (25% luxury tax, `@LuxuryTax`) — both implement `TaxCalculator` interface.
3. **`@RequestScoped CartController`** — Injects the `@SessionScoped ShoppingCart` AND both qualifiers. Method `getTotal(double subtotal)` uses `@StandardTax` by default.
4. **Prove scope isolation:** Open two browsers — each must have an independent `ShoppingCart`.

## 💯 Grading

| Area | Marks |
|---|---|
| Correct scope on `ShoppingCart` + `Serializable` | 25% |
| Both qualifiers defined and applied correctly | 35% |
| Session isolation demonstrated (two browsers) | 40% |
