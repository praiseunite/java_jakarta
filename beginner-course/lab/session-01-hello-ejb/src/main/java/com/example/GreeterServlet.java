package com.example;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A tiny web page, so you can SEE the bean working in a browser.
 *
 * A servlet is just a Java class that answers web requests. We use one here
 * purely as a window onto the bean — the interesting part is GreeterBean.
 *
 * @WebServlet("/greet") means: when someone opens /greet on this application,
 * run the code below.
 */
@WebServlet("/greet")
public class GreeterServlet extends HttpServlet {

    /**
     * THE most important line in this whole project.
     *
     * @EJB means: "Container, please find me a ready-to-use GreeterBean and
     * put it in this field before my code ever runs."
     *
     * We never write `new GreeterBean()`. We never know (or care) which
     * instance we got. The container decides. This is called injection.
     */
    @EJB
    private GreeterBean greeter;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Read the ?name=... part of the URL. If it is absent this is null,
        // and GreeterBean.greet() already handles null for us.
        String name = request.getParameter("name");

        // Tell the browser to show the result as plain text.
        response.setContentType("text/plain");

        // Ask the bean to do the work, and write its answer into the response.
        response.getWriter().println(greeter.greet(name));
    }
}
