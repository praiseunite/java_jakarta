# Session 6 — Assignment
## Advanced Facelets and String Manipulation

**Course:** Enterprise Application Development in Jakarta EE
**TL Session:** TL1 (Session 6)

---

## 📝 Part 1: Conceptual Questions (Written)

Answer the following questions in your own words to verify your understanding of the JSF lifecycle and Facelets.

1. **The Lifecycle:** Explain what happens during the "Update Model Values" phase of the JSF lifecycle. Why must the "Process Validation" phase happen *before* this phase?
2. **Tag Libraries:** What is the difference between `ui:include` and `ui:insert` in Facelets templating?
3. **Web Resources:** If you have an image located at `webapp/resources/theme/logo.png`, write the exact `<h:graphicImage>` tag you would use to render this image in Facelets.

---

## 💻 Part 2: Practical Coding (Try It Yourself)

Fulfill the second textbook "Try It Yourself" requirement:
*"Write a Jakarta Server Faces xhtml page which will display the vowels present in a given String."*

**Requirements:**
1. **The Bean (`VowelAnalyzerBean.java`):**
   - Create a `@Named` backing bean.
   - It should have a String field for user input (e.g., `inputText`).
   - It should have a String field for the output result (e.g., `extractedVowels`).
   - Write an action method `analyzeVowels()` that iterates through the input string, finds all the vowels (a, e, i, o, u), and sets them to the `extractedVowels` string.

2. **The View (`vowel_analyzer.xhtml`):**
   - Create a form with an `<h:inputText>` bound to the user input field.
   - Add an `<h:commandButton>` that calls the `analyzeVowels()` action.
   - Use an `<h:outputText>` to display the extracted vowels below the form.

**Example Behavior:**
- User types: `"Jakarta Enterprise Beans"`
- User clicks Submit.
- Page displays: `"Vowels found: a, a, a, E, e, i, e, e, a"`

### Submission:
Submit your `VowelAnalyzerBean.java` and `vowel_analyzer.xhtml` code snippets to your instructor for grading. Ensure your project deploys successfully to WildFly without exceptions.
