# TL07 Assignment: Local vs Remote Client Demonstration

## 📝 Assignment Overview

Build a demonstration application proving the difference between a **Local** and a **Remote** EJB client in practice.

## 🏢 Business Scenario

Your bank has two applications on the same server: a customer-facing JSF web portal and a batch processing Java standalone application running in a separate JVM. Both need to call the same `LoanCalculatorBean`.

## 📋 Requirements

### 1. The EJB — Both Interfaces
Create `LoanCalculatorBean` with:
- A `@Local` interface `LoanCalculatorLocal`
- A `@Remote` interface `LoanCalculatorRemote`
- A single method: `double calculateMonthlyPayment(double principal, double annualRate, int months)`

### 2. The Local Client
- A JSF `@Named @RequestScoped` bean called `LoanFormController`
- Inject via `@EJB LoanCalculatorLocal` (local call — same WAR)
- Create an XHTML page with a form for the three inputs, display the result

### 3. The Remote Client
- A standalone Java class with a `main()` method
- Use JNDI `InitialContext` to look up the EJB via its remote interface
- Print the result to the console

## 💯 Grading Criteria

| Area | Marks |
|---|---|
| EJB with both `@Local` and `@Remote` interfaces correctly defined | 35% |
| JSF form calling via local injection working correctly | 35% |
| Standalone remote JNDI lookup compiles and connects | 30% |
