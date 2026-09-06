# Assignment — Session 1 & 2
## Introduction to Jakarta Enterprise Beans + Session Bean Types

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1
**Task Type:** Independent Assignment (Done Outside Class)
**Submission Deadline:** As specified by your instructor

---

## Submission Instructions

- Submit the following:
  1. A **ZIP file** of your Eclipse workspace project folder(s)
  2. A **short written report** (1–2 pages) answering the theory questions in Part A
  3. **Screenshots** of your running output for each practical task in Part B
- Name your ZIP file: `[YourName]_Session1_2_Assignment.zip`
- Upload to the course portal or hand in as directed by your instructor

---

## Objectives Being Tested

This assignment tests whether you can:
- Explain core EJB and Jakarta EE concepts in your own words
- Identify which bean type to use for a given business scenario
- Build and deploy both Stateful and Stateless Session Beans independently
- Write a remote interface and a client that accesses the bean
- Understand Message-Driven Bean characteristics

---

## Part A — Theory Questions (30 Marks)

Answer the following questions in full sentences. Do NOT copy from the textbook — use your own words.

**Question 1** (5 marks)
Explain the difference between a **traditional Java application** and a **Jakarta EE enterprise application**. Why would a company choose to build an enterprise application using EJB over building everything from scratch?

**Question 2** (5 marks)
In your own words, describe what the **EJB Container** is and list **four specific services** it provides to enterprise beans automatically. For each service, give a brief real-world example of why that service matters.

**Question 3** (6 marks)
You are designing a system for an online university. For each scenario below, state which type of session bean you would use (**Stateful**, **Stateless**, or **Singleton**) and explain WHY:

- **Scenario A**: A service that looks up and returns a student's grade for a given course
- **Scenario B**: A multi-step student registration process that collects personal info, course selection, and payment across three separate web pages
- **Scenario C**: A service that keeps a running count of how many students are currently logged into the university portal

**Question 4** (4 marks)
What is a **Message-Driven Bean**? Explain how it differs from a Session Bean. Give a real-world use case where an MDB would be the right choice.

**Question 5** (5 marks)
Look at the following check-your-progress questions from the textbook and answer them, providing a brief explanation for your choice:

a. The architecture of the EJB specification does NOT define ________.
   Options: Distributed object components / Client-side security and encryption / Server-side components / Transactional components

b. Which middleware services are provided by EJB?
   Options: Transaction Management / Security / Both a and b / None of these

c. Which of the following is/are true about stateless session beans?
   Options: Multiple users can access stateful session beans at the same time / Preserving any state across method calls is not performed by stateless session beans / Both / None of these

d. Which session beans maintain state between client invocations but are not required to maintain state during server crashes?
   Options: Stateful / Stateless / Singleton / None

e. A JAR file and a WAR file are packed as a JAR file with the extension EAR.
   Options: True / False

**Question 6** (5 marks) — The ZenTaxCore Case Study
Read the ZenTaxCore scenario from the textbook (Section 1.7, Try It Yourself). Answer the following:

- Which Jakarta EE technology would you recommend for ZenTaxCore's portal application, and why?
- List at least **three Jakarta EE APIs** that would be involved in building the ZenTaxCore portal and explain what role each would play.
- From the desired features listed in the case study, identify which feature would require a **Message-Driven Bean** and explain why.

---

## Part B — Practical Tasks (70 Marks)

### Task 1 — Stateful Session Bean: Math Operation Tracker (25 marks)

**Scenario**: Build a Stateful Session Bean for a student studying mathematics. The student wants to perform several operations in a single session and see a running history of all operations performed.

**Requirements**:
1. Create an EJB project called `MathTrackerEJB`
2. Create a **Stateful Session Bean** called `MathTrackerBean` that:
   - Adds two numbers and stores the result in a history list
   - Subtracts two numbers and stores the result in a history list
   - Multiplies two numbers and stores the result in a history list
   - Divides two numbers and stores the result in a history list (handle divide by zero)
   - Has a method `getHistory()` that returns all operations performed in this session
   - Has a method `clearHistory()` annotated with `@Remove` that clears the history and ends the session
3. Create a **Remote Interface** `MathTrackerRemote`
4. Create a **client** that:
   - Calls `add(10, 5)` → stores "10 + 5 = 15"
   - Calls `subtract(20, 7)` → stores "20 - 7 = 13"
   - Calls `multiply(4, 6)` → stores "4 × 6 = 24"
   - Calls `divide(15, 4)` → stores "15 ÷ 4 = 3.75"
   - Calls `getHistory()` and prints all four operations
   - Calls `clearHistory()` to end the session

**Expected Console Output**:
```
=== Math Operation History ===
10 + 5 = 15
20 - 7 = 13
4 × 6 = 24
15 ÷ 4 = 3.75
Session ended and history cleared.
```

**Marking Rubric — Task 1**:

| Criteria | Marks |
|---|---|
| EJB project created and compiles without errors | 3 |
| `@Stateful` annotation correctly applied | 3 |
| Remote interface defined with all four math methods + `getHistory()` + `clearHistory()` | 5 |
| All four math methods implemented and produce correct results | 6 |
| `getHistory()` returns the correct history list with formatted strings | 4 |
| `@Remove` correctly applied to `clearHistory()` | 2 |
| Client connects and shows correct output | 2 |

---

### Task 2 — Stateless Session Bean: String Vowel Checker (20 marks)

**Scenario**: Build a Stateless Session Bean for a spelling assistant application. The bean checks whether a given string contains vowels and counts them.

**Requirements**:
1. Create an EJB project called `VowelCheckerEJB`
2. Create a **Stateless Session Bean** called `VowelCheckerBean` that:
   - Has a method `containsVowels(String input)` that returns `true` if the string contains at least one vowel, and `false` otherwise
   - Has a method `countVowels(String input)` that returns the number of vowels (a, e, i, o, u — case insensitive)
   - Has a method `getVowelsFound(String input)` that returns a string listing which vowels were found (e.g., "Vowels found: a, e, o")
3. Create a **Remote Interface** `VowelCheckerRemote`
4. Create a client that accepts input from the user (using `Scanner`) and calls all three methods

**Acceptance criteria**:
- `containsVowels("Jakarta Enterprise Beans")` → `true`
- `countVowels("Jakarta Enterprise Beans")` → `9`  (Jakarta: a,a,a = 3 · Enterprise: E,e,i,e = 4 · Beans: e,a = 2 → 3+4+2 = 9)
- `containsVowels("rhythm")` → `false`
- `countVowels("AEiou")` → `5`

**Marking Rubric — Task 2**:

| Criteria | Marks |
|---|---|
| `@Stateless` annotation correctly applied | 3 |
| Remote interface defined with all three methods | 4 |
| `containsVowels()` returns correct true/false | 4 |
| `countVowels()` returns accurate count (case insensitive) | 5 |
| `getVowelsFound()` returns properly formatted string | 2 |
| Client uses Scanner for user input | 2 |

---

### Task 3 — Stateless Session Bean: Average Calculator (15 marks)

**Requirements**:
1. Create an EJB project called `AverageCalculatorEJB`
2. Create a **Stateless Session Bean** called `AverageBean` that:
   - Has a method `calculateAverage(int[] numbers)` that accepts an array of integers and returns their average as a `double`
   - Has a method `calculateAverage(List<Integer> numbers)` — overloaded version that accepts a List
   - Handles the edge case where an empty array/list is passed (return `0.0` and print a warning)
3. Create the Remote Interface and client
4. The client should test with:
   - An array of 5 numbers
   - A list of 10 numbers
   - An empty array

**Marking Rubric — Task 3**:

| Criteria | Marks |
|---|---|
| Bean compiles and deploys | 2 |
| `calculateAverage(int[])` produces correct result | 5 |
| `calculateAverage(List<Integer>)` produces correct result | 5 |
| Empty array/list edge case handled correctly | 3 |

---

### Task 4 — Written Reflection (10 marks)

In 300–500 words, write a reflection covering:

1. **What surprised you** when building your first EJB project? What was harder than expected?
2. **The difference you noticed** between the Stateful bean (Task 1) and the Stateless bean (Tasks 2 & 3) in terms of how you had to structure the code.
3. **One thing you are still confused about** after this session, and your best attempt at explaining it based on what you have learned so far.
4. **How you would explain** the concept of a "Stateless Session Bean" to a non-programmer friend in 3–4 simple sentences using an analogy from everyday life.

---

## Extension Challenge — For High Achievers (Bonus: +10 marks)

**ZenTaxCore Prototype**: Based on the ZenTaxCore case study in the textbook, create a **Singleton Session Bean** called `DocumentQueueManager` that:

- Maintains a queue of pending document uploads (`Queue<String>` in Java)
- Has a method `enqueueDocument(String filename)` to add a document to the queue
- Has a method `processNextDocument()` that removes and returns the next document in the queue
- Has a method `getQueueSize()` that returns how many documents are waiting
- Uses `@Startup` so the queue is initialized when the application starts
- Uses proper `@Lock` annotations for thread safety

Create a client that enqueues 5 documents, then processes them one by one, printing a message for each.

---

## Final Reminder

> ⚠️ Your code must compile and run without errors. Include screenshots of your console output as proof of execution.
>
> ⚠️ Do NOT copy-paste the Class Task code and submit it as your Assignment. Your Assignment tasks are different from the Class Task and test independent problem-solving.
>
> ✅ You are encouraged to use comments in your code to explain what each section does — this shows deeper understanding and will earn you additional marks.
