# TL19 Assignment: Customise Context Root and Secure a WAR

## 📝 Assignment Overview

Build and deploy a WAR with a custom context root and URL-level security constraints configured entirely via `web.xml`.

## 📋 Requirements

1. **Create a Skinny WAR** called `employee-portal` containing a `@Stateless` `EmployeeService` EJB and a `@WebServlet("/profile")` servlet.

2. **Custom Context Root:** Override the default context root to `/hr` using `WEB-INF/jboss-web.xml`:
   ```xml
   <jboss-web>
       <context-root>/hr</context-root>
   </jboss-web>
   ```
   After deployment, the servlet must be accessible at `http://localhost:8080/hr/profile`.

3. **Security Constraint in `web.xml`:** Protect `/hr/admin/*` so only users with the `HR_ADMIN` role can access it.

4. **Session Config:** Set session timeout to 15 minutes in `web.xml`.

5. **Welcome File:** Configure `index.xhtml` as the default welcome file.

## 💯 Grading

| Area | Marks |
|---|---|
| `jboss-web.xml` sets context root to `/hr` correctly | 25% |
| Security constraint protects `/hr/admin/*` for `HR_ADMIN` | 35% |
| Session timeout and welcome file configured in `web.xml` | 20% |
| WAR deploys successfully and `/hr/profile` returns a response | 20% |
