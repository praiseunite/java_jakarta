# Practical 1 — Build the Core (`globalbank-core`)

A complete, working Jakarta EE application. Build it, deploy it, and drive a real
bank from your browser's address bar.

This is **stage one of three**. You will grow this same application across
Practical 2 (a web front door) and Practical 3 (security and packaging). Nothing
is thrown away.

You do not need to type any of this in by hand — every file is already here.

**Where this fits:** this is the runnable companion to
[Sessions 1–4 — enterprise beans](../../session-01-02-intro-and-beans.html),
[resource creation](../../session-03-resource-creation.html),
[working with beans](../../session-04-working-with-ejb.html) and the
[JPA bonus page](../../13-jpa-persistence.html). Read those first if the words
*bean*, *container*, *@Stateless* or *entity* are new. The full course contents
are in the [course index](../../index.html). The guided, step-by-step version of
this page is [Practical 1 — Build the Core](../../practical-01-core.html).

---

## What is in this folder

```
practical-1-core/
├── README.md                                     ← you are here
├── pom.xml                                       ← how to build the project
└── src/main/
    ├── java/com/globalbank/
    │   ├── entity/Account.java                   ← the bank account, a JPA entity
    │   ├── ejb/AccountService.java               ← open / deposit / withdraw / transfer
    │   ├── ejb/AccountNumberGenerator.java       ← one counter, @Singleton @Lock(WRITE)
    │   ├── ejb/InsufficientFundsException.java   ← a refusal that undoes the transaction
    │   ├── ejb/LedgerInitializer.java            ← seeds demo data at startup
    │   └── web/AccountServlet.java               ← a browser front door for the beans
    ├── resources/META-INF/persistence.xml        ← which database, which entities
    └── webapp/WEB-INF/beans.xml                  ← CDI marker (used properly in Practical 2)
```

Five classes, and every one of them maps onto something the course taught:

| Class | The course session it comes from |
|---|---|
| `Account` | Session 3 (persistence) + the JPA bonus page |
| `AccountService` | Sessions 1–3 (stateless beans, container-managed work) |
| `AccountNumberGenerator` | Session 4 (singletons and locked concurrency) |
| `LedgerInitializer` | Session 4 (bean lifecycle, `@Startup`, `@PostConstruct`) |
| `AccountServlet` | Sessions 1–2 (a client that calls a bean) |

---

## What you need first

1. **JDK 17** — check with `java -version`
2. **Maven** — check with `mvn -v` (IntelliJ bundles one, or install from maven.apache.org)
3. **WildFly 30+** — download from wildfly.org/downloads and unzip it somewhere stable, e.g. `C:\wildfly`

There is **no database to install**. This project is configured to use
`ExampleDS`, the in-memory database WildFly already ships and starts for you. The
data disappears when WildFly stops, which is exactly what you want while learning.

If any of those three are missing, work through
**[Set up your workshop](../../01-setup.html)** first — it installs all three, with
the exact command and what you should see after each one.

---

## Run it — eight steps

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

Wait for `BUILD SUCCESS`. You now have `target/globalbank-core.war`.

If you are curious, look inside `target/` — the war is just a zip file with your
classes and `persistence.xml` in it. That is all a Jakarta EE application is.

### 4. Deploy

Copy the war into WildFly's deployment folder:

```
copy target\globalbank-core.war  C:\wildfly\standalone\deployments\      (Windows)
cp target/globalbank-core.war    ~/wildfly/standalone/deployments/       (macOS / Linux)
```

WildFly watches that folder. Within a few seconds its console prints:

```
Deployed "globalbank-core.war"
```

If instead you see a red `WFLYSRV0021` or `Failed to deploy`, read the
**first** error line, not the wall of text after it.

### 5. Look at what the application seeded

Open your browser at:

```
http://localhost:8080/globalbank-core/account?action=all
```

You should see:

```
3 account(s) in the ledger:
  Account 1001 | Ada Lovelace | balance 1500.00 | @Version 0
  Account 1002 | Grace Hopper | balance 250.00 | @Version 0
  Account 1003 | Alan Turing | balance 0.00 | @Version 0
```

Nobody typed that SQL. `LedgerInitializer` ran once, at deploy time, because it
is a `@Singleton @Startup` bean with a `@PostConstruct` method.

### 6. Change some money

Deposit into Ada's account:

```
http://localhost:8080/globalbank-core/account?action=deposit&id=1001&amount=250.00
```

→ `Deposited. Now: Account 1001 | Ada Lovelace | balance 1750.00 | @Version 1`

Look closely at that last number. It was `0` a moment ago. **`@Version` went up
by one** — JPA incremented it, because the row changed. That counter is how it
will detect two people editing the same account at once.

### 7. Try to take out too much

```
http://localhost:8080/globalbank-core/account?action=withdraw&id=1003&amount=100.00
```

Alan Turing's account holds `0.00`, so the bank refuses:

```
FAILED: InsufficientFundsException: Account 1003 holds 0.00, so 100.00 cannot be withdrawn
```

Now check that nothing changed:

```
http://localhost:8080/globalbank-core/account?action=all
```

Ada still holds `1750.00`. The refusal left the ledger untouched — but not
because the code "did not get that far". It is `@ApplicationException(rollback = true)`
on the exception class doing that work.

### 8. Move money between two accounts

```
http://localhost:8080/globalbank-core/account?action=transfer&id=1001&to=1002&amount=100.00
```

→

```
Transferred 100.00 from 1001 (Ada Lovelace) to 1002 (Grace Hopper)

Now: Account 1001 | Ada Lovelace | balance 1650.00 | @Version 2
Now: Account 1002 | Grace Hopper | balance 350.00 | @Version 1
```

Two accounts changed, and **both `@Version` numbers went up**. One HTTP request,
one transaction, two writes that either both happened or neither did.

Now open a new account of your own:

```
http://localhost:8080/globalbank-core/account?action=open&holder=Your%20Name&balance=500.00
```

→ `Opened: Account 2001 | Your Name | balance 500.00 | @Version 0`

`2001` came from `AccountNumberGenerator`, the single application-wide counter.

---

## If something goes wrong

| What you see | What it means | What to do |
|---|---|---|
| `mvn: command not found` | Maven is not on your PATH | Use IntelliJ's bundled Maven, or add Maven's `bin` to PATH |
| `BUILD FAILURE … release version 17 not supported` | Your JDK is older than 17 | Install Temurin JDK 17 and point `JAVA_HOME` at it |
| Red underlines under `@Stateless` / `@Entity` | Maven has not downloaded the API yet | Right-click `pom.xml` → Maven → Reload project |
| `404 Not Found` in the browser | Wrong URL, or the war is not deployed yet | Check the console for `Deployed`; confirm `/globalbank-core/account` |
| `Connection refused` | WildFly is not running | Start it (step 1) and wait for the `started in …ms` line |
| `500` and `Table "ACCOUNTS" not found` | The schema was not generated | Check `persistence.xml` is inside the war (`target/globalbank-core/WEB-INF/classes/META-INF/`), then `mvn clean package` again |
| `500` and `JBAS011843` / `no persistence unit` | Wrong `persistence.xml` location | It must be at `src/main/resources/META-INF/persistence.xml` |
| `FAILED: IllegalArgumentException: … required` | You left a URL parameter off | Compare your URL with the ones above |
| Numbers have more decimals than you expect | You passed something like `0.1` | That is fine — money is a `BigDecimal` here, not a `double` |
| Port 8080 already in use | Another program owns the port | Close it, or start WildFly with `-Djboss.socket.binding.port-offset=100` and use port 8180 |
| Everything is `404` after a restart | `ExampleDS` is in memory | Restarting WildFly wipes the data — redeploy and start again from step 5 |

---

## Check yourself

Answer these before moving to Practical 2. If any answer is a guess, re-read the
class it is about.

1. Where is `new AccountService()`? *(Nowhere — the container made it.)*
2. Where is the `UPDATE accounts SET balance = …` SQL? *(Nowhere — JPA writes it at commit.)*
3. Why is the balance a `BigDecimal` and not a `double`?
4. Why does `AccountNumberGenerator` need `@Lock(LockType.WRITE)` when it holds one `int`?
5. Why is `InsufficientFundsException` annotated with `rollback = true`?
6. What would break if `LedgerInitializer` lost its `@Startup`?

---

## Going further (optional challenges)

1. **Add a method.** Give `AccountService` a `BigDecimal totalHeld()` that adds up
   every balance, using a JPQL query (`SELECT SUM(a.balance) FROM Account a`).
   Expose it as `?action=total`. What does JPQL return for an empty table?
2. **Break the transaction on purpose.** Remove `rollback = true` from
   `InsufficientFundsException`, rebuild, redeploy, and run the failed transfer in
   step 7 again. Check both accounts afterwards. Explain what you see.
3. **See the optimistic lock.** Open two browser tabs on `?action=find&id=1001`,
   then in one tab deposit money. There is nothing to see yet — but read
   `Account.getVersion()` and write down, in one sentence, when JPA would now
   refuse a write.
4. **Watch the SQL.** `persistence.xml` already sets `hibernate.show_sql=true`.
   Look at the WildFly console while you call `?action=transfer` and find the two
   `update accounts …` statements. Notice they appear *after* your Java method
   returned.

---

## Why this project looks like this

| Thing you see | Why it is there |
|---|---|
| `@Stateless` on `AccountService` | The container pools these and wraps each call in a transaction |
| `@Singleton` on `AccountNumberGenerator` | Exactly one counter must exist, or two customers get the same number |
| `@Startup` on `LedgerInitializer` | Seed at deploy time, not at first use |
| `@PersistenceContext` | Asks the container for a managed `EntityManager` — never `new` one |
| `@EJB` on the servlet's field | The explicit way to ask for another enterprise bean |
| `private int next` (not `AtomicInteger`) | So `@Lock(WRITE)` is doing the work, and you can see it |
| `@Version` on the entity | Optimistic locking: a write fails rather than silently overwriting |
| `BigDecimal` for money | A `double` cannot store `0.10` exactly; the errors accumulate |
| `<jta-data-source>ExampleDS` | Zero database setup — WildFly ships it |
| `drop-and-create` schema generation | Rebuild the tables on each deploy, so you always start clean |
| `scope provided` in `pom.xml` | The server already has the Jakarta EE API; do not ship a second copy |
| `finalName` `globalbank-core` | Becomes the URL folder: `/globalbank-core/` |

---

## What Practical 2 adds

Same application, same `AccountService` — plus a Facelets web page, a remote
client, a message queue for transfers, bean validation on the request, and a
second transaction that must run separately. Finish this one first: Practical 2
begins by copying these files.
