package com.example;

import jakarta.ejb.Stateless;

/**
 * Your first enterprise bean.
 *
 * Everything special about this class is in ONE line: the @Stateless annotation.
 * Without it, this is an ordinary Java class. With it, the application server
 * takes ownership of the object — it creates it, hands it out, and destroys it.
 *
 * Notice what is NOT here:
 *   - no new GreeterBean() anywhere
 *   - no thread handling
 *   - no security checks
 *   - no connection management
 * You write the business rule. The container supplies the rest.
 */
@Stateless
public class GreeterBean {

    /**
     * The business logic. Takes a name, returns a greeting.
     *
     * This method has no memory of previous calls. Ask it twice with the same
     * name and you get the same answer both times — that is what "stateless"
     * means, and it is why this bean can be shared by thousands of users.
     */
    public String greet(String name) {

        // Defensive coding: a URL like ?name= gives us null. Handle it rather
        // than crashing with a NullPointerException.
        if (name == null || name.isBlank()) {
            name = "stranger";
        }

        return "Hello, " + name + "! (said by a container-managed bean)";
    }
}
