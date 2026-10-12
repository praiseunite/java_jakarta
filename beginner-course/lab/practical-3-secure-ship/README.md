# Practical 3 — Secure & Ship (`globalbank-web`)

Practicals 1 and 2 built a working bank. This one adds the two things that turn a
working application into one you could put in front of a customer: **who is
allowed to do what**, and **what else should happen automatically** when
something succeeds.

This is **stage three of three**. It keeps every file from Practical 2 — the
entities, the service beans, the page, the queue, the remote interface — and adds
to them.

You do not need to type any of this in by hand. Every file is already here.

**Where this fits:** the runnable companion to
[CDI part 1](../../session-11-cdi-part1.html),
[CDI part 2](../../session-12-cdi-part2.html),
[CDI beans and security](../../session-13-cdi-beans-security.html)
and [packaging](../../session-14-packaging.html).
Read [Practical 1](../../practical-01-core.html) and
[Practical 2](../../practical-02-front-door.html) for the earlier stages, and
[Practical 3](../../practical-03-secure-ship.html) for the guided walkthrough.

> **Do Practicals 1 and 2 first.** This project starts from Practical 2's
> finished state, and the walkthrough assumes you have already seen a war deploy,
> a page answer a request, and a refusal come back from the bank.

> **This war replaces the Practical 2 one.** It is called `globalbank-web.war`
> on purpose, and it deploys to the same URL. If Practical 2's war is still in
> the deployments folder, remove it first — two wars with the same name is a
> conflict, not an upgrade.

---

## What is new, in one table

Sixteen new Java files, two new Facelets, ten changed files. Nothing was rewritten.

| New file | What it adds | Session |
|---|---|---|
| `entity/BankUser.java` | The table of people who may sign in, with hashed passwords | 13 |
| `ejb/UserInitializer.java` | Creates that table's two demo users at deploy time | 13 |
| `security/ApplicationConfig.java` | Two annotations: how people sign in, and where their passwords are checked | 13 |
| `security/LoginBean.java` | The bean that actually calls `SecurityContext.authenticate(…)` | 13 |
| `webapp/login.xhtml` | The sign-in form | 13 |
| `webapp/login-error.xhtml` | The page named by `@LoginToContinue(errorPage = …)` | 13 |
| `qualifiers/Email.java`, `qualifiers/Sms.java` | Two labels that tell two `Notifier` beans apart | 12 |
| `qualifiers/BankName.java`, `qualifiers/AuditDepth.java` | Two labels for values that come from configuration | 11–12 |
| `config/BankConfiguration.java` | The `@Produces` field and `@Produces` method behind them | 11 |
| `notify/Notifier.java` + `EmailNotifier` + `SmsNotifier` | One interface, two implementations, chosen by qualifier | 12 |
| `interceptors/Logged.java` + `BankAuditInterceptor.java` | An interceptor binding and the code that wraps `@Logged` methods | 12 |
| `events/TransferCompletedEvent.java` + `events/TransferNotifier.java` | An event, and the observer that runs only after the commit | 12 |

| Changed file | The change | Why |
|---|---|---|
| `ejb/AccountService.java` | `@DeclareRoles`, `@RolesAllowed` on every public method, `@Logged`, `@Inject Event<…>` | This class is the security boundary and the announcement point |
| `ejb/AccountService.java` | New `private moveMoney(…)`, now shared by `transfer` and `transferOrRefuse` | So the security rule applies to one door and not the other — step 9 |
| `ejb/AuditService.java` | `record` is `@PermitAll`, `recent(int)` is `@RolesAllowed("BANK_MANAGER")` | The difference between an action and a view |
| `dao/RawAccountDao.java` | `@RolesAllowed("BANK_MANAGER")` on both public methods | Raw SQL over HTTP is a capability worth restricting |
| `web/AccountsBean.java` | `@Inject SecurityContext`, `@AuditDepth int`, `@BankName String`; `getAuditLines()` guarded | The page now depends on who is looking at it |
| `web/AccountServlet.java` | `@Inject SecurityContext`; `action=raw` asks for the manager role first | Enforce in the bean, explain at the door |
| `webapp/accounts.xhtml` | A "signed in as" header with a sign-out button; the audit section is manager-only | The visible payoff of a role |
| `webapp/WEB-INF/web.xml` | `<security-constraint>` on `/accounts.xhtml` and `/account`, `<security-role>` entries | Which URLs need a signed-in user is a web-application question, not a bean question |
| `resources/META-INF/persistence.xml` | `<class>…BankUser</class>` added | JPA creates the table the identity store queries |
| `webapp/WEB-INF/beans.xml` | Comment only — it is still empty, and step 10 explains why | The interceptor uses `@Priority`, so no `<interceptors>` XML |
| `ejb/LedgerInitializer.java` | Comment only — explaining why the seeder must keep its three `em.persist(…)` calls | `@Startup` runs before there is anybody to authorize, so it must never call `AccountService.open()`, which is now `@RolesAllowed("CUSTOMER")` |

---

## What is in this folder

```
practical-3-secure-ship/
├── README.md                                     ← you are here
└── app/                                          ← the runnable war
    ├── pom.xml                                   ← builds globalbank-web.war
    └── src/main/
        ├── java/com/globalbank/
        │   ├── config/BankConfiguration.java     ← NEW  @Produces field + method
        │   ├── dao/RawAccountDao.java            ← CHANGED  manager-only
        │   ├── ejb/AccountNumberGenerator.java   ← unchanged
        │   ├── ejb/AccountService.java           ← CHANGED  the security boundary
        │   ├── ejb/AccountServiceRemote.java     ← unchanged  (its methods are now guarded)
        │   ├── ejb/AuditService.java             ← CHANGED  write open, read restricted
        │   ├── ejb/BatchTransferBean.java        ← unchanged
        │   ├── ejb/InsufficientFundsException.java  ← unchanged
        │   ├── ejb/LedgerInitializer.java        ← CHANGED  comment only
        │   ├── ejb/UserInitializer.java          ← NEW  the two demo users
        │   ├── entity/Account.java               ← unchanged
        │   ├── entity/AuditEntry.java            ← unchanged
        │   ├── entity/BankUser.java              ← NEW  the users table
        │   ├── events/TransferCompletedEvent.java   ← NEW  the announcement
        │   ├── events/TransferNotifier.java      ← NEW  the observer
        │   ├── interceptors/Logged.java          ← NEW  the binding
        │   ├── interceptors/BankAuditInterceptor.java  ← NEW  the interceptor
        │   ├── messaging/TransferMDB.java        ← unchanged
        │   ├── messaging/TransferQueueSender.java   ← unchanged
        │   ├── notify/Notifier.java              ← NEW  one interface
        │   ├── notify/EmailNotifier.java         ← NEW  @Email
        │   ├── notify/SmsNotifier.java           ← NEW  @Sms
        │   ├── qualifiers/AuditDepth.java        ← NEW  @Qualifier
        │   ├── qualifiers/BankName.java          ← NEW  @Qualifier
        │   ├── qualifiers/Email.java             ← NEW  @Qualifier
        │   ├── qualifiers/Sms.java               ← NEW  @Qualifier
        │   ├── security/ApplicationConfig.java   ← NEW  the two definitions
        │   ├── security/LoginBean.java           ← NEW  the login itself
        │   ├── web/AccountServlet.java           ← CHANGED  role check on ?action=raw
        │   ├── web/AccountsBean.java             ← CHANGED  SecurityContext, @AuditDepth
        │   └── web/TransferRequest.java          ← unchanged
        ├── resources/META-INF/persistence.xml    ← CHANGED  BankUser registered
        └── webapp/
            ├── accounts.xhtml                    ← CHANGED  manager-only audit log
            ├── login.xhtml                       ← NEW  the sign-in form
            ├── login-error.xhtml                 ← NEW
            └── WEB-INF/
                ├── beans.xml                     ← comment only, still empty
                └── web.xml                       ← CHANGED  security-constraint
```

---

## What you need first

The same three things as Practicals 1 and 2:

1. **JDK 17** — `java -version`
2. **Maven** — `mvn -v`
3. **WildFly 30+** — unzipped somewhere stable, e.g. `C:\wildfly`

Still **no database to install** and still **no security realm to configure**:
users live in the same in-memory `ExampleDS` as the accounts.

---

## Run it — eleven steps

### 1. Remove Practical 2's war, then start WildFly

If `globalbank-web.war` is in `standalone/deployments/`, delete it (and its
`.deployed` marker) now. This project builds an artefact with the same name, and
the container will not run two of them.

Then, from WildFly's `bin` folder:

```
standalone.bat -c standalone-full.xml        (Windows)
./standalone.sh -c standalone-full.xml       (macOS / Linux)
```

Leave it running. Wait for `WildFly Full 30.0.1.Final started in …`.

### 2. Open a second terminal in `app/`

(the folder containing `pom.xml` — note it is one level down from this README)

### 3. Build and deploy

```
mvn clean package
```

Then copy the war in:

```
copy target\globalbank-web.war  C:\wildfly\standalone\deployments\      (Windows)
cp target/globalbank-web.war    ~/wildfly/standalone/deployments/       (macOS / Linux)
```

Watch the console. This time there are **three** new lines to find, and they are
the whole practical:

```
[Ledger] seeded 3 accounts: 1001, 1002, 1003
[Users]  seeded 2 users: ada@globalbank.test (CUSTOMER), manager@globalbank.test (BANK_MANAGER)
[Users]  created ada@globalbank.test in group CUSTOMER
[Users]  created manager@globalbank.test in group BANK_MANAGER
```

JPA builds **three** tables now — `accounts`, `audit_log` and `bank_users`.

### 4. Open the front door and be turned away

```
http://localhost:8080/globalbank-web/
```

You do **not** get the bank. You get the sign-in page.

That single redirect is the most important thing that has changed, so it is worth
knowing exactly who did it. `web.xml` now contains a `<security-constraint>`
naming `/accounts.xhtml`. An unauthenticated request for a protected URL is
handed to the authentication mechanism declared in `ApplicationConfig`, and
`@LoginToContinue(loginPage = "/login.xhtml")` is what turns "not allowed" into
"go here instead".

### 5. Sign in as the customer

```
ada@globalbank.test / secret
```

You land on the bank. Two things are different from Practical 2:

- The header says **Signed in as ada@globalbank.test · CUSTOMER**
- **There is no audit log.** Only a note saying it belongs to `BANK_MANAGER`

That absence is not a CSS problem. `AuditService.recent(…)` carries
`@RolesAllowed("BANK_MANAGER")`, and `AccountsBean.getAuditLines()` does not even
call it for a customer — because JSF evaluates the expression inside a
`rendered="false"` block anyway, so a call would have thrown into the middle of
the page render. The bean and the page check the same thing in the same words.

### 6. Move money, then read the server console

Transfer `25` from `1001` to `1002`, click **Transfer now**. The balances change.

Now look at the **WildFly console**, not the page:

```
GlobalBank: -> transfer
GlobalBank: <- transfer (7 ms)
[email] GlobalBank: 25 moved from account 1001 (Ada Lovelace) to account 1002 (Grace Hopper) (simulated — no mail server is configured)
[sms]   GlobalBank: 25 moved from account 1001 (Ada Lovelace) to account 1002 (Grace Hopper) (simulated — no gateway is configured)
Notified the customer about: 25 moved from account 1001 (Ada Lovelace) to account 1002 (Grace Hopper)
```

Four separate features of this practical just announced themselves in one
transfer, and each line comes from a different file:

| Line | Comes from | The feature |
|---|---|---|
| `-> transfer` / `<- transfer` | `BankAuditInterceptor` | An **interceptor** wrapping every `@Logged` method on `AccountService`. `@Logged` is on the class; the interceptor is enabled by `@Priority`, with no XML |
| `[email] GlobalBank: …` | `EmailNotifier` | A **qualifier** picked this bean out of two candidates |
| `[sms]   GlobalBank: …` | `SmsNotifier` | The same interface, the other bean |
| `Notified the customer about: …` | `TransferNotifier` | An **observer** on `TransferCompletedEvent` |

**And notice where those lines are.** They are printed *after* `<- transfer`. The
event was fired inside the transfer, before the commit — but the observer is
declared `@Observes(during = TransactionPhase.AFTER_SUCCESS)`, so the container
held it back until the transaction had actually committed. That ordering is the
feature, not an accident: a customer must not be told about money that moved
before the database agreed that it did.

**Now do the same transfer the other way.** Enter `25` from `1001` to `1002` again
— but this time click **Queue it instead** rather than **Transfer now**. The page
says the transfer has been queued; reload it and the balances have changed. Look at
the console again:

```
GlobalBank: -> transferOrRefuse
GlobalBank: <- transferOrRefuse (12 ms)
[email] GlobalBank: 25 moved from account 1001 (Ada Lovelace) to account 1002 (Grace Hopper) (simulated — no mail server is configured)
[sms]   GlobalBank: 25 moved from account 1001 (Ada Lovelace) to account 1002 (Grace Hopper) (simulated — no gateway is configured)
```

Read the first line and you will see it names a **different method**. Pressing that
button did not call `AccountService` from the page at all. It handed a message to
`TransferQueueSender`, which put it on `java:/jms/queue/TransferQueue`; the
message-driven bean `TransferMDB` picked it up on its own thread, called
`transferOrRefuse`, and wrote its own audit row.

Two things are worth noticing in that output.

**The notification still happened — and after `<- transferOrRefuse`.** The observer
does not care which door the transfer came in by. `AFTER_SUCCESS` waits for whatever
transaction was in progress, and here that was the message-driven bean's, not the
page's. Same annotation, same guarantee, a different transaction.

**But the audit log now says `QUEUED-TRANSFER`, not `TRANSFER`.** That is not the
service recording itself. `AccountsBean` records `TRANSFER` for the direct path;
`TransferMDB` records `QUEUED-TRANSFER` for this one. Sign in as the manager and you
can see both rows sitting side by side, which is the honest picture: the audit trail
is written by the two callers, not by the bank.

This step exists for a second reason. `transferOrRefuse` is the only `@PermitAll`
method in the whole application — it is open to every caller, which looks alarming
on a bank. It is safe because **its only caller is a message-driven bean, which
runs with no request, no session and no user**, so there is no identity for a role
check to test. Pressing this button is how you confirm that claim instead of
taking it on trust.

### 7. Where does "GlobalBank" come from?

Nowhere in `AccountService`, `EmailNotifier` or `BankAuditInterceptor` is the
string `"GlobalBank"` written. All three write `bankName`, and all three got it
from one `@Produces` field:

```java
@Produces @BankName
String bankName = "GlobalBank";
```

Change that one line in `BankConfiguration`, redeploy, and every log line changes.
That is the argument for a producer: one value, one place, however many consumers.

### 8. Try the raw SQL view as a customer, and be refused

```
http://localhost:8080/globalbank-web/account?action=raw
```

You are signed in, so `web.xml` lets you through — and then you are refused:

```
REFUSED: the raw SQL view is for BANK_MANAGER only.
You are signed in as ada@globalbank.test. The same rows are on accounts.xhtml.
```

with HTTP status **403**. Two refusals happened, one behind the other, and both
are worth seeing:

1. `AccountServlet` asked `security.isCallerInRole("BANK_MANAGER")` and answered
   with a sentence.
2. Even without that check, `RawAccountDao.allAccountsByHand()` carries
   `@RolesAllowed("BANK_MANAGER")` and the container would have thrown
   `EJBAccessException`.

The first one exists only so a human gets an explanation. **The second one is the
security.** Never let the friendly check be the only check.

### 9. Sign out, sign in as the manager, and see what changed

Click **Sign out**, then sign in as:

```
manager@globalbank.test / manager
```

Now the audit log is on the page — one `TRANSFER · OK` row for the transfer you
made in step 6 — and `account?action=raw` works.

Note what is **not** in the audit log: a row for your step 8 refusal. `AccountServlet`
returns 403 without recording anything, and the container's own `EJBAccessException`
does not write to this table either. An authorization refusal leaves evidence in the
**server log**, not in the application's audit trail — which is a real gap, and worth
knowing about before you are asked to investigate one.

Same application, same beans, same page. The only difference is one word in a
column of `bank_users`. That is role-based security.

### 10. Prove the interceptor needs no XML

Open `WEB-INF/beans.xml`. It is **empty**, and the interceptor works anyway.

Look at the top of `BankAuditInterceptor`:

```java
@Interceptor
@Logged
@Priority(Interceptor.Priority.APPLICATION)
```

`@Priority` is what switches it on for the whole application. Older material —
and a lot of it is still online — insists on this instead:

```xml
<interceptors>
    <class>com.globalbank.interceptors.BankAuditInterceptor</class>
</interceptors>
```

Both work. The XML form is what you use when you want to be able to turn an
interceptor off per deployment without recompiling; `@Priority` is what you use
when you just want it on. This project uses `@Priority` because there is then no
file to forget.

### 11. Change the audit depth without recompiling

Add a system property to WildFly's start-up and restart it:

```
standalone.bat -c standalone-full.xml -Dglobalbank.auditDepth=5        (Windows)
./standalone.sh -c standalone-full.xml -Dglobalbank.auditDepth=5       (macOS / Linux)
```

Sign in as the manager and make a few transfers: the audit table now shows five
lines instead of twenty-five. Remove the property and it goes back to the
default. Read `BankConfiguration.auditDepth()` — that is a `@Produces` **method**,
as opposed to the `@Produces` **field** in step 7, and it is the shape you use
when the value has to be worked out rather than just known.

---

## The two rules that are easiest to get wrong

Both of these cost real deployments, and neither produces a helpful error.

### Rule 1 — a producer of a non-proxyable type must be `@Dependent`

`BankName` produces a `String` and `AuditDepth` produces an `int`. Neither
producer carries a scope annotation, and that is deliberate.

When a bean has a normal scope (`@ApplicationScoped`, `@RequestScoped`, …), CDI
hands injection points a **proxy** — a stand-in that looks up the real instance
on each call. A proxy works by subclassing. `String` is `final` and `int` is a
primitive, so neither can be subclassed.

Writing `@ApplicationScoped @Produces @BankName String bankName = "GlobalBank";`
is therefore not a warning you can ignore. It is a **definition error**: the
application refuses to deploy. Try it once, on purpose, and read the message —
you will recognise it forever.

### Rule 2 — an unannotated method on a secured bean is open to everyone

On a bean that carries security annotations *anywhere*, a method with no
`@RolesAllowed`, `@PermitAll` or `@DenyAll` is **unchecked** — reachable by
anybody, including a caller with no identity at all. There is no deny-by-default.

That is why every public method on `AccountService` and `RawAccountDao` is
annotated, including the ones that are obviously meant to be open. `@PermitAll`
on `transferOrRefuse` would behave identically if it were left off, and would mean
something completely different to the next person reading the file.

---

## If something goes wrong

| What you see | What it means | What to do |
|---|---|---|
| You are never sent to the login page | The URL is not in a `security-constraint` | A bean annotation cannot protect a URL. Add `/accounts.xhtml` to `web.xml` |
| `too many redirects` | The login page is itself protected | `web.xml` must protect `/accounts.xhtml` and `/account` — and must **not** protect `/login.xhtml` |
| The login page renders with no styling | Faces' own resources are protected | Do not use `/*` as the url-pattern |
| Login page redisplays, silently, forever | **`LoginBean.login()` is not being called** | This is the classic failure. Check the button is `action="#{login.login}"`, and read the comment in `ApplicationConfig` — with a *custom* form mechanism, nothing authenticates unless your code asks |
| `That user name and password combination was not recognised.` | Wrong password, or no such user | Both give one message on purpose. Check `[Users] created …` appeared in the console at deploy |
| `ELResolver` / `PropertyNotFoundException: login` | The bean is not reachable by name | `LoginBean` needs both `@Named("login")` and a scope |
| A customer sees a page error instead of a hidden audit log | `getAuditLines()` was called for a customer | Guard the bean method *and* the `rendered=` — JSF evaluates inside `rendered="false"` |
| `EJBAccessException` when a customer transfers | `@RolesAllowed` on the method is narrower than you meant | `transfer` allows both `CUSTOMER` and `BANK_MANAGER`; check which role you signed in as |
| `Ambiguous dependencies for type Notifier` | A qualifier was removed | Both `@Email` and `@Sms` are needed on the injection points in `TransferNotifier` |
| Deployment fails, mentions `String` and a producer | Rule 1 above | Remove the scope from the `@BankName`/`@AuditDepth` producers |
| `Table "BANK_USERS" not found` at login | `BankUser` is not in `persistence.xml` | JPA only creates entities it has been told about |
| The notification lines never appear | No observer, or the transaction rolled back | Look for `TransferNotifier` in the console; a *refused* transfer genuinely must not notify |
| Nothing survives a restart | `ExampleDS` is in memory | Restarting wipes accounts **and users**. Redeploy and start from step 4 |

---

## Check yourself

Answer these before starting the whole-course review.

1. Which annotation decides that `/accounts.xhtml` needs a signed-in user — and
   why can't a bean annotation do that job?
2. `@RolesAllowed` and `@PermitAll` are both on `AccountService`. What does an
   *unannotated* method get?
3. Why is `AuditService.record` open to everyone while `AuditService.recent` is
   not?
4. Why does `LedgerInitializer` write to the database directly instead of calling
   `AccountService.open(…)`?
5. The observer in `TransferNotifier` says `during = TransactionPhase.AFTER_SUCCESS`.
   What would go wrong with the default?
6. There are two `Notifier` beans. What exactly happens if you write
   `@Inject Notifier notifier;` with no qualifier?
7. Why must the `@Produces @BankName String` producer be `@Dependent`?
8. `transferOrRefuse` is `@PermitAll` on a bank. Why is that acceptable here, and
   what specifically makes it acceptable?
9. `BankAuditInterceptor` is not mentioned in `beans.xml`. Why does it still run?
10. In `AccountServlet`, why ask `isCallerInRole` when `RawAccountDao` would refuse
    the call anyway?

---

## Going further (optional challenges)

1. **Break the qualifier.** Delete `@Email` from `TransferNotifier`'s `email`
   field and redeploy. Read the "ambiguous dependency" error carefully — it names
   both candidates, which is how you find this class of problem quickly.
2. **Break the producer scope.** Add `@ApplicationScoped` to the `bankName`
   producer field. The deployment refuses. Find the line in the log that says why,
   and put it back.
3. **Turn the interceptor off.** Remove `@Priority` and add the `<interceptors>`
   block to `beans.xml` instead. Same behaviour, different switch. Then change the
   annotation to something else (`@Audit`?) and see how many files move.
4. **Make the roles differ more.** Add a third user with only `BANK_MANAGER` and
   see what a manager cannot do. Then try `@RolesAllowed("CUSTOMER")` on
   `transfer` alone and watch a manager be refused by the container — and notice
   that the refusal happens *before* the bank's own balance check, and that the
   audit log records nothing, because no business method ever ran.
5. **Watch `AFTER_SUCCESS` earn its place.** Change the observer to
   `during = TransactionPhase.AFTER_FAILURE` and queue a transfer that will be
   refused. The notification now fires for the refusal instead — which is a
   perfectly sensible thing to want, and a different product decision.
6. **Add your own event.** Fire a second event from `AccountService.open(…)` and
   have a listener write an audit line. You will not need to touch anything that
   already exists.

---

## Why this project looks like this

| Thing you see | Why it is there |
|---|---|
| `@DeclareRoles` on the beans | Declares the role names the class uses, so a typo is a role nobody holds |
| `@RolesAllowed` on **every** public method | An unannotated method on a secured bean is open to everyone |
| `@PermitAll` written out on `transferOrRefuse` | It would behave the same if omitted, and mean something else entirely |
| A private `moveMoney(…)` | So the role check governs one entry point and not the other, on purpose |
| `@Logged` on a class, not on ten methods | An interceptor binding applies to a whole class in one word |
| `@Priority` instead of `<interceptors>` in `beans.xml` | No file to forget; the XML stays available for per-deployment switching |
| `@Priority(APPLICATION)`, not a low number | Platform interceptors sit below it, so the transaction is already open |
| `@Produces` **field** for the bank name | For a value that is already known |
| `@Produces` **method** for the audit depth | For a value that has to be computed, and can take injected parameters |
| No scope on either producer | `String` and `int` cannot be proxied; a normal scope is a deployment error |
| `@Qualifier` with `RUNTIME` retention | CDI reads the label at run time; the default `CLASS` retention throws it away first |
| `@Target` on the qualifiers, `METHOD` on the value ones | Where the label may legally be written |
| One interface, two `@ApplicationScoped` beans | The caller knows `Notifier`; the qualifier decides which |
| Log lines instead of real email and SMS | Four things to configure would stand between you and the lesson |
| `Event<TransferCompletedEvent>` injected | The sender declares what it announces, and knows nothing about listeners |
| A `record` as the event payload | Immutable, and it carries values rather than live entities |
| `during = AFTER_SUCCESS` | A customer must not be told about money that has not committed |
| `ApplicationConfig` with no code in it | Two annotations are the whole configuration |
| `@DatabaseIdentityStoreDefinition` with `callerQuery`/`groupsQuery` | The two questions a login asks: what is the hash, and what may they do |
| `hashAlgorithm = Pbkdf2PasswordHash.class` | Without it the default is plain text — a silent downgrade, not an error |
| `Pbkdf2PasswordHash` **injected** into `UserInitializer` | Generate hashes; never assemble the four-part string by hand |
| `LoginBean` calling `authenticate(…)` | With a custom form mechanism, nothing logs in unless your code asks |
| `AuthenticationParameters.withParams().credential(…)` | `authenticate` takes the wrapper, not a bare `Credential` |
| `@RequestScoped` on `LoginBean` | It holds a password; the shorter its life, the better |
| `@ViewScoped` on `AccountsBean` | It caches; the longer its life, the better. Opposite answers, opposite reasons |
| One message for "no such user" and "wrong password" | Distinguishing them tells an attacker which user names are real |
| `<security-constraint>` in `web.xml` | Which URLs need a signed-in user is not a property of any class |
| `/login.xhtml` deliberately unprotected | Protect it and you get a redirect loop |
| No `<login-config>` element | The mechanism is declared by annotation and would fight a `FORM` entry |
| `<security-role>` entries | Redundant with an identity store, and the file's own answer to "what roles exist?" |
| `getAuditLines()` returning `List.of()` for a customer | JSF evaluates inside `rendered="false"`, so a call would throw mid-render |
| A sign-out **form**, not a link | Signing out changes state, so it must be a POST |
| `BankUser.toString()` printing no hash | A `toString` that prints a credential is how secrets reach log files |

---

## Shipping it as an EAR (Session 14) — read this, then decide

Practical 3's last session is about packaging, and the honest summary is this:
**this project ships as a war, and it runs.** The multi-module EAR below is
shown in full so you can see the shape and build it yourself, but it is not the
thing this folder guarantees to build.

That is a deliberate decision, not an omission. An EAR is four Maven projects
with a parent, and every extra module is another place for a beginner's build to
fail before they have seen the application work once. Get the war running first.
Then do this.

### What an EAR is for

A war bundles *web* components. An `ejb-jar` bundles *enterprise beans*. An EAR
bundles them together, plus any plain libraries they share. You move to an EAR
when you want the beans and the pages to be separately deployable, or when more
than one module needs the same library.

### The layout

```
globalbank-ear/                 <packaging>pom</packaging>   ← the parent
├── pom.xml                     <modules> lists the three below
├── globalbank-ejb/             <packaging>ejb</packaging>   ← the beans move here
│   └── src/main/java/com/globalbank/{ejb,entity,messaging,dao,notify,events,…}
├── globalbank-web/             <packaging>war</packaging>   ← the pages stay here
│   └── src/main/{java/com/globalbank/{web,security},webapp}
└── globalbank-ear/             <packaging>ear</packaging>   ← the package itself
    └── src/main/application/META-INF/application.xml
```

### The parent pom

```xml
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.globalbank</groupId>
  <artifactId>globalbank-parent</artifactId>
  <version>1.0.0</version>
  <packaging>pom</packaging>          <!-- pom, not war: this builds nothing itself -->

  <modules>
    <module>globalbank-ejb</module>
    <module>globalbank-web</module>
    <module>globalbank-ear</module>
  </modules>

  <properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>jakarta.platform</groupId>
        <artifactId>jakarta.jakartaee-api</artifactId>
        <version>10.0.0</version>
        <scope>provided</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
</project>
```

`<dependencyManagement>` is the section that stops you repeating a version number
in four files. It declares a version once; each module then lists the dependency
with no `<version>` at all.

### The EJB module

```xml
<project>
  <parent>
    <groupId>com.globalbank</groupId>
    <artifactId>globalbank-parent</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>globalbank-ejb</artifactId>
  <packaging>ejb</packaging>          <!-- builds a jar the container treats as a bean archive -->

  <dependencies>
    <dependency>
      <groupId>jakarta.platform</groupId>
      <artifactId>jakarta.jakartaee-api</artifactId>
    </dependency>
  </dependencies>
</project>
```

Move `ejb/`, `entity/`, `messaging/`, `dao/`, `notify/`, `events/`,
`interceptors/`, `qualifiers/`, `config/` and `resources/META-INF/persistence.xml`
here.

> **`persistence.xml` moves with the entities, and the datasource name does not
> change.** A persistence unit belongs to the module that owns the entities, not
> to the war. This is the single most common thing to get wrong when splitting a
> war in two: leave the file behind and you get "no persistence unit named
> globalbankPU" at deploy time.

### The web module

```xml
<project>
  <parent>
    <groupId>com.globalbank</groupId>
    <artifactId>globalbank-parent</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>globalbank-web</artifactId>
  <packaging>war</packaging>

  <dependencies>
    <dependency>
      <groupId>jakarta.platform</groupId>
      <artifactId>jakarta.jakartaee-api</artifactId>
    </dependency>
    <!-- THE LINE THAT MAKES IT WORK. The war does not get the beans from the
         classpath — without this, the pages compile against nothing and the
         classes are not visible at run time either. -->
    <dependency>
      <groupId>com.globalbank</groupId>
      <artifactId>globalbank-ejb</artifactId>
      <version>1.0.0</version>
      <type>ejb</type>                <!-- `ejb`, not `jar` — an EJB module's type -->
      <scope>provided</scope>         <!-- the EAR supplies it; do not nest a copy in the war -->
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.4.0</version>
        <configuration>
          <!-- No WEB-INF/lib inside the war. The classes are in the EAR, and a
               second copy on the war's own classloader is how you get two
               different Account classes and a ClassCastException nobody can
               explain. -->
          <failOnMissingWebXml>false</failOnMissingWebXml>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

Keep `web/`, `security/` and `webapp/` here.

`RemoteClient` gets the `globalbank-ejb` jar on its classpath, and the JNDI name
changes from

```
ejb:/globalbank-web/AccountService!com.globalbank.ejb.AccountServiceRemote
```

to

```
ejb:/globalbank-ejb/AccountService!com.globalbank.ejb.AccountServiceRemote
```

Read the two: the module name in the middle is the *artifact* the bean lives in,
not the one that deployed it. This is exactly the kind of breakage Session 14 is
warning you about.

### The EAR module

```xml
<project>
  <parent>
    <groupId>com.globalbank</groupId>
    <artifactId>globalbank-parent</artifactId>
    <version>1.0.0</version>
  </parent>
  <artifactId>globalbank-ear</artifactId>
  <packaging>ear</packaging>

  <dependencies>
    <dependency>
      <groupId>com.globalbank</groupId>
      <artifactId>globalbank-ejb</artifactId>
      <version>1.0.0</version>
      <type>ejb</type>
    </dependency>
    <dependency>
      <groupId>com.globalbank</groupId>
      <artifactId>globalbank-web</artifactId>
      <version>1.0.0</version>
      <type>war</type>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-ear-plugin</artifactId>
        <version>3.3.0</version>
        <configuration>
          <version>10</version>              <!-- Jakarta EE 10 -->
          <defaultLibBundleDir>lib</defaultLibBundleDir>   <!-- <library-directory> -->
          <modules>
            <ejbModule>
              <groupId>com.globalbank</groupId>
              <artifactId>globalbank-ejb</artifactId>
              <bundleFileName>globalbank-ejb.jar</bundleFileName>
            </ejbModule>
            <webModule>
              <groupId>com.globalbank</groupId>
              <artifactId>globalbank-web</artifactId>
              <contextRoot>/globalbank</contextRoot>   <!-- the URL changes too -->
            </webModule>
          </modules>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

`<defaultLibBundleDir>lib</defaultLibBundleDir>` is the `<library-directory>`
Session 14 talks about: shared jars go in the EAR's `lib/` folder, which is on
the classpath of every module, instead of being duplicated inside the war.

### Build and deploy

```
mvn clean package                        (from the parent folder: builds all three)
copy globalbank-ear\target\globalbank-ear-1.0.0.ear  C:\wildfly\standalone\deployments\
```

### What changes for you, concretely

| | War (this folder) | EAR |
|---|---|---|
| Artefact | `globalbank-web.war` | `globalbank-ear-1.0.0.ear` |
| URL | `http://localhost:8080/globalbank-web/` | `http://localhost:8080/globalbank/` |
| JNDI module name | `globalbank-web` | `globalbank-ejb` |
| Maven projects | 1 | 4 |
| Where the beans live | same war as the pages | their own jar |
| `RemoteClient` needs | the war's `target/classes` | `globalbank-ejb/target/globalbank-ejb.jar` |

If the JNDI name change is what finally makes the "module name" idea click, the
EAR has done its job.

---

## What the whole three-stage build added up to

Practical 1 gave you entities, beans and a database.
Practical 2 gave you a page, a queue and a second way in.
Practical 3 gave you a login, a rule about who may do what, and code that reacts
to what already happened.

Roughly 31 source files, three tables, and no SQL written by hand anywhere. Every
one of those files is in this folder, and every one of them is commented for
someone reading it for the first time.
