# Session 6 Assignment: Jakarta Local and Remote Clients

## 📝 Assignment Overview

This assignment assesses your ability to architect, develop, and test enterprise applications utilizing both **Remote** and **Local** client communication models in Jakarta EE with WildFly.

You are required to implement two distinct enterprise services based on the textbook's **Try It Yourself** exercises.

---

## 📋 Exercise 1: Number Swap Service (EJB Remote Client)

### Objective
Develop an EJB Stateless Session Bean with a `@Remote` interface that swaps the values of two integers using a third temporary variable. Then, create a standalone console client running in a separate JVM that prompts the user for two numbers, invokes the remote EJB across the network, and displays the result.

### Functional Requirements
1. **Package:** `com.assignment.swap`
2. **Remote Interface (`NumberSwapRemote`):**
   * Define a method:
     ```java
     SwapResult swapNumbers(int a, int b);
     ```
   * Or use a data-transfer object (DTO) `SwapResult` containing `int originalA`, `int originalB`, `int swappedA`, `int swappedB`.
   * **Note:** Since this is a Remote EJB, all custom classes exchanged (such as `SwapResult`) **must implement `java.io.Serializable`**!
3. **Stateless Session Bean (`NumberSwapBean`):**
   * Must implement `NumberSwapRemote`.
   * Swapping logic must explicitly use a third temporary variable:
     ```java
     int temp = a;
     a = b;
     b = temp;
     ```
4. **Standalone Remote Client (`NumberSwapClientApp`):**
   * Configures JNDI with WildFly's initial context factory (`org.wildfly.naming.client.WildFlyInitialContextFactory`) and provider URL (`remote+http://localhost:8080`).
   * Performs JNDI lookup of `NumberSwapBean`.
   * Takes user input from `Scanner(System.in)`.
   * Displays the swapped values returned by the server.

---

## 📋 Exercise 2: Palindrome Verification Service (EJB Local Client)

### Objective
Develop an EJB Stateless Session Bean with a `@Local` interface that verifies whether an input string or integer is a palindrome. Create a Local Web Client (Servlet or JSF page) running in the same JVM that consumes the bean via `@EJB` dependency injection.

### Functional Requirements
1. **Package:** `com.assignment.palindrome`
2. **Local Interface (`PalindromeCheckerLocal`):**
   * Declare overloaded or dedicated methods:
     ```java
     boolean isStringPalindrome(String input);
     boolean isNumberPalindrome(long number);
     ```
3. **Stateless Session Bean (`PalindromeCheckerBean`):**
   * Must implement `PalindromeCheckerLocal`.
   * Must ignore case and strip spaces/punctuation for string palindromes (e.g., `"Race car"` or `"Madam"` should be recognized as palindromes).
   * For numbers, negative numbers cannot be palindromes (e.g., `-121` is `false`, `1221` is `true`).
4. **Local Web Client (`PalindromeServlet`):**
   * Uses `@EJB` to inject `PalindromeCheckerLocal`.
   * Exposes an HTML form with an input field and submit button.
   * On `POST` or `GET`, calls the injected local bean and displays:
     * Original input.
     * Palindrome status (`"Yes, it is a Palindrome!"` or `"No, it is not a Palindrome."`).

---

## 📂 Expected Directory Structure

```
Session_06_Assignment/
├── swap-service-ear-or-war/           <-- Deployed to WildFly
│   └── src/main/java/com/assignment/
│       ├── swap/
│       │   ├── SwapResult.java       (Implements Serializable)
│       │   ├── NumberSwapRemote.java (@Remote)
│       │   └── NumberSwapBean.java   (@Stateless)
│       └── palindrome/
│           ├── PalindromeCheckerLocal.java (@Local)
│           ├── PalindromeCheckerBean.java  (@Stateless)
│           └── PalindromeServlet.java      (@WebServlet)
└── swap-remote-client/                <-- Standalone Client Project
    └── src/main/java/com/assignment/client/
        └── NumberSwapClientApp.java   (Main class / JNDI Lookup)
```

---

## 🧪 Edge Cases to Handle

1. **Remote Call Serialization:**
   * Verify that your DTO `SwapResult` implements `Serializable`, with a defined `serialVersionUID`. Missing this will result in a runtime `java.io.NotSerializableException` during RMI dispatch.
2. **Empty or Null Inputs in Palindrome:**
   * An empty string `""` or single character `"a"` should be handled gracefully (both are valid palindromes).
   * `null` should return `false`.
3. **Network Failure Simulation:**
   * Run the standalone remote client while WildFly is stopped. Verify that your client catches `NamingException` or `RemoteException` and prints a clear, user-friendly error message rather than crashing unhandled.

---

## 📊 Grading Rubric

| Criteria | Points | Description |
| :--- | :---: | :--- |
| **Exercise 1: Remote Architecture** | **35 pts** | Correct `@Remote` interface, serializable DTO, and WildFly JNDI lookup client in an independent JVM. |
| **Exercise 1: Number Swap Logic** | **15 pts** | Correct logic using a third temporary variable; interactive terminal input and formatted output. |
| **Exercise 2: Local Architecture** | **25 pts** | Correct `@Local` interface and `@EJB` injection into a co-located Servlet or JSF managed bean. |
| **Exercise 2: Palindrome Logic** | **15 pts** | Robust palindrome algorithms for both strings (case/space insensitive) and integers. |
| **Code Quality & Documentation** | **10 pts** | Clean packaging, proper comments, meaningful variable names, and error handling. |
| **Total** | **100 pts** | |

---

## 📤 Submission Instructions

1. Export or zip your Maven projects into a single archive named:
   `LastName_FirstName_Session06_Assignment.zip`
2. Include screenshots demonstrating:
   * **Screenshot 1:** WildFly console confirming deployment of both beans with their JNDI bindings.
   * **Screenshot 2:** Remote console client successfully swapping two user-entered numbers.
   * **Screenshot 3:** Web browser displaying the Palindrome Servlet verification results.
3. Submit the `.zip` file to your course portal before the deadline.
