# Session 1 Lab — `hello-ejb`

A complete, working Jakarta EE project. Build it, deploy it, and watch a real
enterprise bean answer a real web request.

You do not need to type any of this in by hand — every file is already here.

**Where this fits:** this is the runnable companion to
[Sessions 1–2 — your first enterprise bean](../../session-01-02-intro-and-beans.html). Read that first
if the words *bean*, *container* or *@Stateless* are new. The full course contents are in the
[course index](../../index.html).

---

## What is in this folder

```
session-01-hello-ejb/
├── README.md                                  ← you are here
├── pom.xml                                    ← how to build the project
└── src/main/java/com/example/
    ├── GreeterBean.java                       ← the enterprise bean  (@Stateless)
    └── GreeterServlet.java                    ← a web page that calls the bean
```

---

## What you need first

1. **JDK 17** — check with `java -version`
2. **Maven** — check with `mvn -v` (IntelliJ bundles one, or install from maven.apache.org)
3. **WildFly 30+** — download from wildfly.org/downloads and unzip it somewhere stable, e.g. `C:\wildfly`

If any of those are missing, work through **[Set up your workshop](../../01-setup.html)** first — it
installs all three, with the exact command and what you should see after each one.

---

## Run it — seven steps

### 1. Start WildFly

Open a terminal in WildFly's `bin` folder and run:

```
standalone.bat        (Windows)
./standalone.sh       (macOS / Linux)
```

Leave it running. Wait for the line:

```
WildFly Full 30.0.1.Final started in 3512ms
```

### 2. Open a second terminal in THIS folder

(the one containing `pom.xml`)

### 3. Build

```
mvn clean package
```

Wait for `BUILD SUCCESS`. You now have `target/hello-ejb.war`.

### 4. Deploy

Copy the war into WildFly's deployment folder:

```
copy target\hello-ejb.war  C:\wildfly\standalone\deployments\      (Windows)
cp target/hello-ejb.war    ~/wildfly/standalone/deployments/       (macOS / Linux)
```

WildFly watches that folder. Within a couple of seconds its console prints:

```
Deployed "hello-ejb.war"
```

### 5. Use it

Open your browser at:

```
http://localhost:8080/hello-ejb/greet?name=Ada
```

You should see:

```
Hello, Ada! (said by a container-managed bean)
```

### 6. Prove the bean is doing the work

Change the name in the URL:

```
http://localhost:8080/hello-ejb/greet?name=YourName
```

Leave the name off entirely:

```
http://localhost:8080/hello-ejb/greet
```

→ `Hello, stranger! (said by a container-managed bean)`

That is `GreeterBean.greet()` handling the `null` case you coded.

### 7. Look at the bean, not the servlet

Open `GreeterBean.java`. Ask yourself:

- Where is `new GreeterBean()`? **Nowhere** — the container created it.
- Where is the thread handling? **Nowhere** — the container did it.
- Where is the security check? **Nowhere** — we did not ask for one.

The servlet only asks; the bean only answers. Everything else was free.

---

## If something goes wrong

| What you see | What it means | What to do |
|---|---|---|
| `mvn: command not found` | Maven is not on your PATH | Use IntelliJ's bundled Maven, or add Maven's `bin` to PATH |
| `BUILD FAILURE … release version 17 not supported` | Your JDK is older than 17 | Install Temurin JDK 17 and point `JAVA_HOME` at it |
| Red underlines under `@Stateless` / `@EJB` | Maven has not downloaded the API yet | Right-click `pom.xml` → Maven → Reload project |
| `404 Not Found` in the browser | Wrong URL, or the war is not deployed yet | Check the WildFly console for `Deployed`; confirm `/hello-ejb/greet` |
| `Connection refused` | WildFly is not running | Start it (step 1) and wait for the `started in …ms` line |
| Port 8080 already in use | Another program owns the port | Close it, or start WildFly with `-Djboss.socket.binding.port-offset=100` and use port 8180 |
| `Failed to deploy` in the console | Compile error, or a stale build | Run `mvn clean package` again and read the first error line |

---

## Going further (optional challenges)

1. **Add a method.** Give `GreeterBean` a `String shout(String name)` that returns
   the greeting in capitals. Add a `/shout` servlet that calls it.
2. **Break it on purpose.** Delete `@Stateless` from `GreeterBean`, rebuild and
   redeploy. What happens, and why? (Redeploy the fixed version afterwards.)
3. **Add a second bean.** Create `ClockBean` with `@Stateless` and a method
   `String now()` returning the current time. Inject it into the servlet alongside
   `greeter` and print both.

---

## Why this project looks like this

| Thing you see | Why it is there |
|---|---|
| `@Stateless` on `GreeterBean` | Tells the server "manage this class as an enterprise bean" |
| `@EJB` on the servlet field | Asks the container to inject a ready bean — the alternative to `new` |
| `@WebServlet("/greet")` | Gives the servlet a URL, so a browser can reach it |
| `scope provided` in `pom.xml` | The server already has the Jakarta EE API; do not ship a second copy |
| `finalName` `hello-ejb` | Becomes the URL folder: `/hello-ejb/` |
