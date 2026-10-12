# Practical 2 — The Front Door (`globalbank-web`)

Practical 1 built a bank only a URL could reach. This one gives it a **page a
person can use**, and adds the four things that only matter once something
outside your application is talking to it: a remote client, a message queue,
bean validation, and a transaction that has to survive a rollback.

This is **stage two of three**. It keeps every file from Practical 1 — the
entities, the service beans, the database — and adds to them.

You do not need to type any of this in by hand. Every file is already here.

**Where this fits:** the runnable companion to
[Facelets](../../session-05-facelets.html),
[local and remote clients](../../session-06-local-remote.html),
[messaging](../../session-07-messaging.html),
[connectors](../../session-08-connectors.html),
[bean validation](../../session-09-bean-validation.html) and
[transactions](../../session-10-ejb-transactions.html).
Read [Practical 1](../../practical-01-core.html) and
[Practical 2](../../practical-02-front-door.html) for the guided walkthrough, or
the [course index](../../index.html) for everything.

> **Do Practical 1 first.** This project starts from Practical 1's finished state,
> and the walkthrough assumes you have already seen a war deploy and a bean answer
> a request.

---

## What is in this folder

```
practical-2-web-messaging/
├── README.md                                          ← you are here
├── pom.xml                                            ← builds the war
├── remote-client/                                     ← NOT part of the war
│   └── src/com/globalbank/client/RemoteClient.java    ← a program outside WildFly
└── src/main/
    ├── java/com/globalbank/
    │   ├── entity/Account.java                        ← unchanged from Practical 1
    │   ├── entity/AuditEntry.java                     ← NEW: one line of audit log
    │   ├── ejb/AccountService.java                    ← CHANGED: see below
    │   ├── ejb/AccountServiceRemote.java              ← NEW: @Remote — the outside view
    │   ├── ejb/AccountNumberGenerator.java            ← unchanged
    │   ├── ejb/AuditService.java                      ← NEW: writes in its OWN transaction
    │   ├── ejb/BatchTransferBean.java                 ← NEW: transactions by hand (BMT)
    │   ├── ejb/InsufficientFundsException.java        ← unchanged
    │   ├── ejb/LedgerInitializer.java                 ← unchanged
    │   ├── messaging/TransferQueueSender.java         ← NEW: puts a transfer on a queue
    │   ├── messaging/TransferMDB.java                 ← NEW: picks it up, in the background
    │   ├── dao/RawAccountDao.java                     ← NEW: the same data, in raw SQL
    │   └── web/
    │       ├── AccountServlet.java                    ← CHANGED: gained ?action=raw
    │       ├── AccountsBean.java                      ← NEW: the bean behind the page
    │       └── TransferRequest.java                   ← NEW: the form, with its rules
    ├── resources/META-INF/persistence.xml             ← CHANGED: AuditEntry registered
    └── webapp/
        ├── accounts.xhtml                             ← NEW: the page itself
        └── WEB-INF/
            ├── beans.xml                              ← unchanged
            └── web.xml                                ← NEW: registers FacesServlet
```

| New file | The course session it comes from |
|---|---|
| `accounts.xhtml` + `AccountsBean` | Session 5 (Facelets, backing beans, the view) |
| `AccountServiceRemote` + `RemoteClient` | Session 6 (local vs remote, JNDI, stubs) |
| `TransferQueueSender` + `TransferMDB` | Session 7 (messaging, producers, MDBs) |
| `RawAccountDao` | Session 8 (JCA, JDBC, connection pools) |
| `TransferRequest` | Session 9 (bean validation) |
| `AuditService` + `BatchTransferBean` | Session 10 (transaction attributes, BMT) |

---

## What changed in Practical 1's files

Three files changed, in five small ways. Nothing was rewritten, and it is worth
knowing exactly what moved:

| File | Change | Why |
|---|---|---|
| `AccountService.java` | now `implements AccountServiceRemote` | To give the bean a remote view. The methods were already the right shape. |
| `AccountService.java` | `findAll()` wraps its result in `new ArrayList<>(…)` | A remote call has to serialise the list, and a JPA provider's `List` is not guaranteed to be serialisable. The copy makes it safe. |
| `AccountService.java` | new method `transferOrRefuse(…)` | A refusal that is *thrown* poisons the transaction. A message-driven bean needs one that is *returned* instead — step 7 explains why. |
| `AccountServlet.java` | new `?action=raw` case | So the same rows can be read a second way, with hand-written SQL. |
| `persistence.xml` | `<class>…AuditEntry</class>` added | JPA has to be told about the new table. |

---

## What you need first

The same three things as Practical 1:

1. **JDK 17** — `java -version`
2. **Maven** — `mvn -v`
3. **WildFly 30+** — unzipped somewhere stable, e.g. `C:\wildfly`

Still **no database to install**: this project uses `ExampleDS` as well.

If any are missing, work through [Set up your workshop](../../01-setup.html).

---

## Run it — nine steps

### 1. Start WildFly

From WildFly's `bin` folder:

```
standalone.bat -c standalone-full.xml        (Windows)
./standalone.sh -c standalone-full.xml       (macOS / Linux)
```

Leave it running. Wait for `WildFly Full 30.0.1.Final started in …`.

### 2. Open a second terminal in THIS folder

(the one containing `pom.xml`)

### 3. Build and deploy

```
mvn clean package
```

Then copy the war in:

```
copy target\globalbank-web.war  C:\wildfly\standalone\deployments\      (Windows)
cp target/globalbank-web.war    ~/wildfly/standalone/deployments/       (macOS / Linux)
```

Watch the console. This time there is more to look for than `Deployed`: the queue
being created (from the `@JMSDestinationDefinition` in `TransferQueueSender`), and
JPA building **two** tables — `accounts` and `audit_log` — instead of one.

### 4. Open the front door

```
http://localhost:8080/globalbank-web/
```

The welcome file sends you to `accounts.xhtml`, and you should see the three
seeded accounts in a table, a transfer form, and an empty audit log.

Compare that with what you had at the end of Practical 1 — the same three rows,
in a page instead of a wall of text. **No SQL was written for either.** The page
asks `#{bank.accounts}`, that calls `AccountService.findAll()`, and JPA does the
rest.

### 5. Make a transfer, and watch `@Version`

Transfer `25` from `1001` to `1002` and click **Transfer now**.

The balances change, and the `@Version` column for **both** accounts goes up by
one. Same lesson as Practical 1, but now you can see it: one click, one
transaction, two rows written together.

### 6. Make the bank refuse, and find the refusal on record

Now try to move `99999` out of `1003`. The bank refuses and the page says so.

Now look at the **audit log** at the bottom. There is a line:

```
09 Oct 06:41:12 | TRANSFER | REFUSED | Account 1003 holds 0.00, so 99999 cannot be transferred
```

**That line should not exist.** The transfer was rolled back — everything it did
was undone. So how is the record of it still here?

Because `AuditService.record(…)` is annotated
`@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)`. It does not join
the transfer's transaction; it suspends it, runs in its own, commits, and resumes.
The transfer's rollback cannot touch work that was already committed in a
different transaction.

That is the entire point of `REQUIRES_NEW`, and it is why an audit log is the
textbook example of it. The most valuable line in a bank's log is the one
recording what somebody *tried* and was *refused* — precisely the line a normal
transaction throws away.

### 7. Queue a transfer instead

Fill the form in again (say `1001` to `1003` for `50`) and click **Queue it
instead**.

The page comes straight back, and if you look at the balances **nothing has
changed**. That is correct: all you did was put a message on a queue. Now reload
the page. The balances have moved, and there are two new audit lines whose Action
column reads `QUEUED-TRANSFER` — written by a bean that nobody called.

That bean is `TransferMDB`. The container watches
`java:/jms/queue/TransferQueue`, and when your message landed it called
`onMessage()` on a thread of its own, with no browser waiting.

**Now try the interesting one.** Queue `99999` out of `1003` — a transfer you know
will be refused — and reload. You get **one** audit line, not an infinite stream
of them.

That is not luck, and it is the sharpest lesson in this project. Read the comment
on `AccountService.transferOrRefuse`:

- `AccountService.transfer(…)` *throws* `InsufficientFundsException`, and that
  exception is marked `@ApplicationException(rollback = true)`. The instant it
  leaves a bean method, the container marks the whole transaction rollback-only —
  and **no catch block can undo that**. Catch it inside `onMessage()` and the
  transaction still rolls back, so the message is delivered again. And again. A
  refusal that can never succeed loops until the broker gives up.
- `transferOrRefuse(…)` refuses by *returning a sentence* instead. A returned
  value is a success, so the transaction commits and the message is acknowledged.

Choosing which failures to throw and which to return is the whole skill of
writing a message-driven bean.

### 8. See what JPA was doing for you

```
http://localhost:8080/globalbank-web/account?action=all
http://localhost:8080/globalbank-web/account?action=raw
```

The same rows, twice. The second one is `RawAccountDao`, which opens a pooled
`DataSource`, writes `SELECT id, holder, balance, version FROM accounts`, and maps
the result by hand.

Put the two side by side. Everything JPA saves you is the difference between them
— and the one string it was hiding is
`java:jboss/datasources/ExampleDS`, which you can now see in
`RawAccountDao`'s `@Resource` as well as in `persistence.xml`.

### 9. Call the bank from outside WildFly

The servlet still works, the page still works — but both run *inside* the server.
This step is a separate program calling a bean in another process.

`RemoteClient.java` is not in the war and is never deployed. Build the war first
(step 3), then compile the client against its classes:

```
javac -cp "target/classes;C:\wildfly\bin\client\jboss-client.jar" -d remote-client/classes remote-client/src/com/globalbank/client/RemoteClient.java      (Windows)

javac -cp "target/classes:$WILDFLY_HOME/bin/client/jboss-client.jar" -d remote-client/classes remote-client/src/com/globalbank/client/RemoteClient.java  (macOS / Linux)
```

Then run it:

```
java -cp "remote-client/classes;target/classes;C:\wildfly\bin\client\jboss-client.jar" com.globalbank.client.RemoteClient      (Windows)

java -cp "remote-client/classes:target/classes:$WILDFLY_HOME/bin/client/jboss-client.jar" com.globalbank.client.RemoteClient  (macOS / Linux)
```

You should see something like this. (The balances are whatever steps 5–7 left
behind — so after a fresh redeploy you would see the original `1500.00`, `250.00`
and `0.00`. The point is not the numbers; it is where they are coming from.)

```
Connected to the bank running inside WildFly.

3 account(s), copied down the wire:
  Account 1001 | Ada Lovelace | balance 1500.00 | @Version 0
  Account 1002 | Grace Hopper | balance 250.00 | @Version 0
  Account 1003 | Alan Turing | balance 0.00 | @Version 0

Asking the server to move 25.00 from 1001 to 1002...
  Transferred 25.00 from 1001 (Ada Lovelace) to 1002 (Grace Hopper)

3 account(s), copied down the wire:
  ...

Now asking for something the bank will refuse:
The call reached the server, and the bank refused:
  com.globalbank.ejb.InsufficientFundsException
  Account 1003 holds 0.00, so 99999.00 cannot be transferred
```

Two things to notice.

First, those `Account` objects did not exist in this JVM a second ago. They were
serialised on the server, posted over the network, and rebuilt here. That is why
`Account` implements `Serializable` and has a `serialVersionUID`.

Second, two entries got written to the audit log on the web page — one `OK` and
one `REFUSED`. The same `AccountService` served a web page and a standalone
program, and neither knows the other exists.

---

## If something goes wrong

| What you see | What it means | What to do |
|---|---|---|
| `404` at `/globalbank-web/` | The war name or the welcome file | Confirm `finalName` is `globalbank-web`, and that `accounts.xhtml` is at `src/main/webapp/` |
| A page of raw `#{bank.accounts}` text | `FacesServlet` is not mapped | Check `web.xml` maps it to `*.xhtml` — the template must be processed, not served |
| `The page failed to find its bean` / blank tables | CDI did not discover `AccountsBean` | Check `WEB-INF/beans.xml` is in the war, and that `AccountsBean` has both `@Named` and a scope annotation |
| Tables render but every value is empty | Getter names do not match the page | `#{bank.message}` needs `getMessage()`; `#{a.holder}` needs `getHolder()` |
| `Unable to find queue java:/jms/queue/TransferQueue` | The queue was not created | Check the `@JMSDestinationDefinition` on `TransferQueueSender` survives into the war; look for the Artemis line at deploy time |
| Queueing does nothing, ever | The MDB is not listening | Look for `TransferMDB` starting in the console; confirm `destinationLookup` matches the `@Resource` lookup exactly |
| The same audit line appears 10 times | A refusal is being thrown, not returned | Use `transferOrRefuse`, not `transfer` — see step 7 |
| `ClassNotFoundException: org.wildfly.naming.client.WildFlyInitialContextFactory` | `jboss-client.jar` is not on the classpath | Recheck the `-cp` in step 9; the separator is `;` on Windows, `:` elsewhere |
| `NamingException` from the remote client | The bean was not found | Is WildFly running? Is `globalbank-web.war.deployed` in the deployments folder? Is the JNDI name exact? |
| `ConstraintViolationException` on a transfer | Validation rejected the request | Read the message — it names the field and the rule it broke |
| Everything is `404` after a restart | `ExampleDS` is in memory | Restarting wipes the data; redeploy and start again from step 4 |

---

## Check yourself

Answer these before moving to Practical 3.

1. Where is `new AccountsBean()`? *(Nowhere — the container made it, because of `@Named`.)*
2. Which is bigger, `@SessionScoped` or `@ViewScoped`, and which does this bean use?
3. Why does the audit log record a transfer that was refused?
4. Why can't `TransferMDB` simply catch `InsufficientFundsException` and carry on?
5. What is the difference between `java:jboss/exported/jms/queue/TransferQueue` and `java:/jms/queue/TransferQueue`?
6. Why does the remote client need the *interface* and not the bean class?
7. `Account` and `AccountService` both have no `serialVersionUID` problem any more. Why did that matter more in Practical 2 than in Practical 1?
8. In `RawAccountDao`, what is `close()` on a pooled connection actually doing?

---

## Going further (optional challenges)

1. **Make validation visible.** Submit the form with an amount of `0`, then with
   `10.999`, then with an empty reference. Each is a different constraint; the
   page shows a different message. Which class holds those messages?
2. **Watch it refuse before it starts.** Put a breakpoint on `AccountsBean.submit()`
   (or add a `System.out.println`) and submit `0`. It is never reached — validation
   runs in an earlier phase of the JSF lifecycle. Which phase?
3. **Break `REQUIRES_NEW`.** Change `AuditService.record` to the default
   `REQUIRED` and repeat step 6. The refusal line disappears, because now the
   audit write rolls back with the transfer. Put it back.
4. **Break the queue on purpose.** Change `transferOrRefuse` back to `transfer`
   inside `TransferMDB`, redeploy, and queue a refused transfer. Watch the audit
   log fill up with the same line, and find WildFly's redelivery messages in the
   console. This is what a poison message looks like.
5. **Do it all in one transaction.** `BatchTransferBean` moves money to several
   accounts, twice: `transferAllOrNothing` (one transaction) and
   `transferOneTransactionEach` (one per transfer). There is no UI for it yet —
   call it from a small servlet or a test and compare what survives a mid-batch
   refusal.

---

## Why this project looks like this

| Thing you see | Why it is there |
|---|---|
| `@Named` + `@ViewScoped` on `AccountsBean` | CDI finds the bean by name; view scope dies with the page |
| A cache in `getAccounts()` | JSF calls the getter several times per render; only a surviving bean can cache it |
| `h:dataTable` instead of a manual loop | It is JSF's loop: "for each item, one row" |
| `web.xml` registering `FacesServlet` | Faces is not self-annotating; it needs a mapping to `*.xhtml` |
| `beans.xml` | Kept because the course teaches it — though note that under CDI 4.0 a war containing `AccountsBean` is an *implicit* bean archive and would be discovered without it |
| `@Remote` on an interface, not the bean | The remote view is a contract; the bean is free to change behind it |
| `@JMSDestinationDefinition` | Declares the queue in code so the container creates it — no CLI step for you to run first |
| `destinationName` ≠ JNDI `name` | One is the broker's name, one is the lookup name; they are genuinely different |
| Text message, not `ObjectMessage` | Portable, readable in the broker console, no shared class needed |
| `transferOrRefuse` | A thrown refusal rolls the transaction back and redelivers the message forever |
| `REQUIRES_NEW` on `AuditService.record` | The audit line must outlive a rolled-back transfer |
| `@Resource DataSource` in `RawAccountDao` | The pool JPA has been using all along, finally visible |
| `?action=raw` | Same rows, no JPA — the whole argument for JPA, side by side |
| `@EJB` fields left non-`transient` | Conventional in JSF backing beans; `javac -Xlint:serial` warns, and every real project has that warning |

---

## What Practical 3 adds

Same application again — plus CDI producers, qualifiers, an interceptor and an
event; a login form backed by `@DatabaseIdentityStoreDefinition` with
`@RolesAllowed` on the beans; and a multi-module EAR to ship it in. Practical 3
begins by copying these files, so finish this one first.
