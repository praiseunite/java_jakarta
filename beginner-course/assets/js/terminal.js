/* ==========================================================================
   terminal.js — a SIMULATED developer terminal.

   It runs nothing real. It maps commands a student types during the course
   (java -version, mvn clean package, jboss-cli …) to realistic canned output,
   so beginners can practise the commands with zero risk and zero install.

   Markup:
   <div class="terminal" data-terminal="setup">
     <div class="terminal__bar">…built by JS…</div>
     <div class="terminal__screen"></div>
     <div class="terminal__inputline">…built by JS…</div>
   </div>
   The data-terminal value selects an extra command pack (optional).
   ========================================================================== */
(function () {
  "use strict";

  function esc(s) {
    return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  /* base command set available on every terminal ------------------- */
  var BASE = {
    "help": [
      "Commands you can try here (simulated):",
      "  java -version          show the installed Java version",
      "  javac -version         show the compiler version",
      "  mvn -v                 show the Maven version",
      "  mvn clean package      build the project into target/",
      "  ls                     list files in this folder",
      "  cat pom.xml            show the build file",
      "  echo $JAVA_HOME        show the JAVA_HOME variable",
      "  jboss-cli.sh --connect connect to the WildFly management CLI",
      "  clear                  clear the screen"
    ],
    "clear": "__CLEAR__",
    "java -version": [
      'openjdk version "17.0.10" 2024-01-16 LTS',
      "OpenJDK Runtime Environment Temurin-17.0.10+7 (build 17.0.10+7-LTS)",
      "OpenJDK 64-Bit Server VM Temurin-17.0.10+7 (build 17.0.10+7-LTS, mixed mode, sharing)"
    ],
    "javac -version": ["javac 17.0.10"],
    "mvn -v": [
      "Apache Maven 3.9.6",
      "Maven home: C:\\tools\\apache-maven-3.9.6",
      'Java version: 17.0.10, vendor: Eclipse Adoptium',
      "Default locale: en_US, platform encoding: UTF-8",
      'OS name: "windows 11", version: "10.0", arch: "amd64"'
    ],
    "echo $JAVA_HOME": ["C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.10.7-hotspot"],
    "echo %JAVA_HOME%": ["C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.10.7-hotspot"],
    "ls": ["pom.xml   src/   target/   README.md"],
    "dir": ["pom.xml   src   target   README.md"],
    "cat pom.xml": [
      "<project>",
      "  <groupId>com.example</groupId>       <!-- your organisation -->",
      "  <artifactId>hello-ejb</artifactId>   <!-- this project's name -->",
      "  <version>1.0.0</version>",
      "  <packaging>war</packaging>           <!-- build a .war web archive -->",
      "  <dependencies>",
      "    <dependency>",
      "      <groupId>jakarta.platform</groupId>",
      "      <artifactId>jakarta.jakartaee-api</artifactId>",
      "      <version>10.0.0</version>",
      "      <scope>provided</scope>          <!-- the server already has it -->",
      "    </dependency>",
      "  </dependencies>",
      "</project>"
    ],
    "mvn clean package": [
      "[INFO] Scanning for projects...",
      "[INFO] Building hello-ejb 1.0.0",
      "[INFO] --- maven-compiler-plugin: compile ---",
      "[INFO] Compiling 3 source files to target/classes",
      "[INFO] --- maven-war-plugin: war ---",
      "[INFO] Building war: target/hello-ejb.war",
      "[INFO] ------------------------------------------------------------------------",
      "<span class='g'>[INFO] BUILD SUCCESS</span>",
      "[INFO] Total time:  4.812 s",
      "[INFO] ------------------------------------------------------------------------"
    ],
    "jboss-cli.sh --connect": [
      "[standalone@localhost:9990 /] ",
      "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
    ],
    "jboss-cli.bat --connect": [
      "[standalone@localhost:9990 /] ",
      "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
    ]
  };

  /* per-page extra packs ----------------------------------------- */
  var PACKS = {
    setup: {
      "wildfly": ["Tip: start WildFly from IntelliJ's Run panel, or run bin\\standalone.bat"],
      "standalone.bat": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "standalone.sh": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ]
    },
    deploy: {
      /* The lab's simplest deploy path: drop the war into the folder WildFly
         watches. No CLI, no menus — the server deploys it on its own. */
      "copy target\\hello-ejb.war c:\\wildfly\\standalone\\deployments\\": [
        "        1 file(s) copied."
      ],
      "cp target/hello-ejb.war ~/wildfly/standalone/deployments/": [
        "(no output — cp is silent when it succeeds)"
      ],
      "dir c:\\wildfly\\standalone\\deployments": [
        "hello-ejb.war",
        "hello-ejb.war.deployed      <-- WildFly creates this marker once it is live"
      ],
      "deploy target/hello-ejb.war": [
        "<span class='g'>Deployed \"hello-ejb.war\" (runtime-name: \"hello-ejb.war\")</span>",
        "WFLYUT0021: Registered web context: '/hello-ejb'",
        "java:global/hello-ejb/GreeterBean",
        "<span class='y'># GreeterBean has no remote interface, so the binding ends at the class name.</span>",
        "<span class='y'># Try it in a browser: http://localhost:8080/hello-ejb/greet?name=Ada</span>"
      ],
      "undeploy hello-ejb.war": ["Undeployed \"hello-ejb.war\""]
    },
    practicum: {
      /* The practical labs build a DIFFERENT project (globalbank-core, not
         hello-ejb), so BASE's generic output would be a lie here. A pack is
         merged OVER BASE with Object.assign, so re-using a command name here
         replaces it for pages that ask for data-terminal="practicum" only. */
      "help": [
        "Commands you can try here (simulated):",
        "  ls                     list files in this folder",
        "  cat pom.xml            show this project's build file",
        "  mvn clean package      build the project into target/",
        "  dir target             confirm the war was built",
        "  jboss-cli.bat --connect   connect to the WildFly management CLI",
        "  clear                  clear the screen"
      ],
      "ls": ["pom.xml   src/   target/   README.md"],
      "dir": ["pom.xml   src   target   README.md"],
      "standalone.bat": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "standalone.sh": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "jboss-cli.bat --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "jboss-cli.sh --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "cat pom.xml": [
        "<project>",
        "  <groupId>com.globalbank</groupId>        <!-- the pretend bank -->",
        "  <artifactId>globalbank-core</artifactId> <!-- this project's name -->",
        "  <version>1.0.0</version>",
        "  <packaging>war</packaging>               <!-- build a .war web archive -->",
        "  <dependencies>",
        "    <dependency>",
        "      <groupId>jakarta.platform</groupId>",
        "      <artifactId>jakarta.jakartaee-api</artifactId>",
        "      <version>10.0.0</version>",
        "      <scope>provided</scope>              <!-- the server already has it -->",
        "    </dependency>",
        "  </dependencies>",
        "</project>"
      ],
      "mvn clean package": [
        "[INFO] Scanning for projects...",
        "[INFO] Building globalbank-core 1.0.0",
        "[INFO] --- maven-resources-plugin: copy-resources ---",
        "[INFO] Copying 2 resources to target/classes",
        "[INFO] --- maven-compiler-plugin: compile ---",
        "[INFO] Compiling 6 source files to target/classes",
        "[INFO] --- maven-war-plugin: war ---",
        "[INFO] Building war: target/globalbank-core.war",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='g'>[INFO] BUILD SUCCESS</span>",
        "[INFO] Total time:  5.264 s",
        "[INFO] ------------------------------------------------------------------------"
      ],
      "dir target": [
        "classes/",
        "globalbank-core/            <-- the exploded war, useful for peeking inside",
        "globalbank-core.war         <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "ls target": [
        "classes/",
        "globalbank-core/            <-- the exploded war, useful for peeking inside",
        "globalbank-core.war         <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "copy target\\globalbank-core.war c:\\wildfly\\standalone\\deployments\\": [
        "        1 file(s) copied."
      ],
      "cp target/globalbank-core.war ~/wildfly/standalone/deployments/": [
        "(no output — cp is silent when it succeeds)"
      ],
      "dir c:\\wildfly\\standalone\\deployments": [
        "globalbank-core.war",
        "globalbank-core.war.deployed   <-- WildFly created this; the app is live"
      ],
      "deploy target/globalbank-core.war": [
        "<span class='g'>Deployed \"globalbank-core.war\" (runtime-name: \"globalbank-core.war\")</span>",
        "WFLYUT0021: Registered web context: '/globalbank-core'",
        "java:global/globalbank-core/AccountService",
        "java:global/globalbank-core/AccountNumberGenerator",
        "java:global/globalbank-core/LedgerInitializer",
        "<span class='y'># LedgerInitializer is @Startup, so it has already run:</span>",
        "<span class='y'>#   [Ledger] seeded 3 accounts: 1001, 1002, 1003</span>",
        "<span class='y'># Try it in a browser: http://localhost:8080/globalbank-core/account?action=all</span>"
      ],
      "deployment-info --name=globalbank-core.war": [
        "NAME                     RUNTIME-NAME             PERSISTENT RUNTIME-STATUS",
        "globalbank-core.war      globalbank-core.war      true       <span class='g'>OK</span>"
      ],
      "undeploy globalbank-core.war": ["Undeployed \"globalbank-core.war\""]
    },
    practicum2: {
      /* Practical 2's lab builds globalbank-web, and its front door is a web
         page rather than a URL. Several commands below look almost identical to
         the practicum pack's — they are not copies, they name a different
         project, and getting that wrong on the page would be a lie. Keep the
         two packs separate. */
      "help": [
        "Commands you can try here (simulated):",
        "  ls                        list files in this folder",
        "  cat pom.xml               show this project's build file",
        "  mvn clean package         build the project into target/",
        "  dir target                confirm the war was built",
        "  standalone.bat            start WildFly (simulated)",
        "  jboss-cli.bat --connect   connect to the management CLI",
        "  deploy target/globalbank-web.war   deploy it, and watch the queue appear",
        "  undeploy globalbank-web.war        take it back off the server",
        "  clear                     clear the screen",
        "<span class='y'>  # The javac / java commands that call the bank from outside WildFly</span>",
        "<span class='y'>  # work here too — copy them from Part 6 of the page.</span>"
      ],
      "ls": ["pom.xml   remote-client/   src/   target/   README.md"],
      "dir": ["pom.xml   remote-client   src   target   README.md"],
      "standalone.bat": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "standalone.sh": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "jboss-cli.bat --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "jboss-cli.sh --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "cat pom.xml": [
        "<project>",
        "  <groupId>com.globalbank</groupId>        <!-- the same pretend bank -->",
        "  <artifactId>globalbank-web</artifactId>  <!-- a NEW project, not an edit -->",
        "  <version>1.0.0</version>",
        "  <packaging>war</packaging>               <!-- still one war -->",
        "  <dependencies>",
        "    <dependency>",
        "      <groupId>jakarta.platform</groupId>",
        "      <artifactId>jakarta.jakartaee-api</artifactId>",
        "      <version>10.0.0</version>",
        "      <scope>provided</scope>              <!-- still provided, still one -->",
        "    </dependency>",
        "  </dependencies>",
        "</project>",
        "<span class='y'># Faces, Messaging and Validation are inside that one API jar.</span>",
        "<span class='y'># WildFly implements all of them, so none of them ship in the war.</span>"
      ],
      "mvn clean package": [
        "[INFO] Scanning for projects...",
        "[INFO] Building globalbank-web 1.0.0",
        "[INFO] --- maven-resources-plugin: copy-resources ---",
        "[INFO] Copying 1 resource to target/classes",
        "[INFO] --- maven-compiler-plugin: compile ---",
        "[INFO] Compiling 15 source files to target/classes",
        "[INFO] --- maven-war-plugin: war ---",
        "[INFO] Building war: target/globalbank-web.war",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='g'>[INFO] BUILD SUCCESS</span>",
        "[INFO] Total time:  6.118 s",
        "[INFO] ------------------------------------------------------------------------"
      ],
      "dir target": [
        "classes/",
        "globalbank-web/             <-- the exploded war, useful for peeking inside",
        "globalbank-web.war          <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "ls target": [
        "classes/",
        "globalbank-web/             <-- the exploded war, useful for peeking inside",
        "globalbank-web.war          <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "copy target\\globalbank-web.war c:\\wildfly\\standalone\\deployments\\": [
        "        1 file(s) copied."
      ],
      "cp target/globalbank-web.war ~/wildfly/standalone/deployments/": [
        "(no output — cp is silent when it succeeds)"
      ],
      "dir c:\\wildfly\\standalone\\deployments": [
        "globalbank-web.war",
        "globalbank-web.war.deployed   <-- WildFly created this; the app is live"
      ],
      "deploy target/globalbank-web.war": [
        "<span class='g'>Deployed \"globalbank-web.war\" (runtime-name: \"globalbank-web.war\")</span>",
        "<span class='y'># The queue, created at deploy time from @JMSDestinationDefinition:</span>",
        "#   jms-queue \"TransferQueue\" bound to java:/jms/queue/TransferQueue",
        "WFLYUT0021: Registered web context: '/globalbank-web'",
        "java:global/globalbank-web/AccountService",
        "<span class='y'>#   ...and its REMOTE view, which a program outside the server can reach:</span>",
        "#   java:global/globalbank-web/AccountService!com.globalbank.ejb.AccountServiceRemote",
        "java:global/globalbank-web/AccountsBean",
        "java:global/globalbank-web/TransferMDB",
        "<span class='y'># TransferMDB is listening on java:/jms/queue/TransferQueue.</span>",
        "<span class='y'># Nobody calls it. The container does, when a message arrives.</span>",
        "<span class='y'># Open it in a browser: http://localhost:8080/globalbank-web/</span>"
      ],
      "deployment-info --name=globalbank-web.war": [
        "NAME                     RUNTIME-NAME             PERSISTENT RUNTIME-STATUS",
        "globalbank-web.war       globalbank-web.war       true       <span class='g'>OK</span>"
      ],
      "undeploy globalbank-web.war": ["Undeployed \"globalbank-web.war\""],
      "javac -cp \"target/classes;c:\\wildfly\\bin\\client\\jboss-client.jar\" -d remote-client/classes remote-client/src/com/globalbank/client/RemoteClient.java": [
        "(no output — javac is silent when it succeeds)",
        "<span class='y'># Note what is NOT on that classpath: the war, and the bean.</span>",
        "<span class='y'># The client needs the INTERFACE. The implementation stays on the server.</span>"
      ],
      "java -cp \"remote-client/classes;target/classes;c:\\wildfly\\bin\\client\\jboss-client.jar\" com.globalbank.client.RemoteClient": [
        "Connected to the bank running inside WildFly.",
        "",
        "3 account(s), copied down the wire:",
        "  Account 1001 | Ada Lovelace | balance 1500.00 | @Version 0",
        "  Account 1002 | Grace Hopper | balance 250.00 | @Version 0",
        "  Account 1003 | Alan Turing | balance 0.00 | @Version 0",
        "",
        "Asking the server to move 25.00 from 1001 to 1002...",
        "  <span class='g'>Transferred 25.00 from 1001 (Ada Lovelace) to 1002 (Grace Hopper)</span>",
        "",
        "3 account(s), copied down the wire:",
        "  Account 1001 | Ada Lovelace | balance 1475.00 | @Version 1",
        "  Account 1002 | Grace Hopper | balance 275.00 | @Version 1",
        "  Account 1003 | Alan Turing | balance 0.00 | @Version 0",
        "",
        "Now asking for something the bank will refuse:",
        "<span class='r'>The call reached the server, and the bank refused:</span>",
        "  com.globalbank.ejb.InsufficientFundsException",
        "  Account 1003 holds 0.00, so 99999.00 cannot be transferred",
        "",
        "<span class='y'># Two rows changed, in another process, from a plain Java program.</span>"
      ],
      /* The macOS / Linux spelling of the same two commands. The pack offers both
         platforms everywhere else (ls/dir, cp/copy, standalone.sh/.bat), so a
         student on a Mac who copies the page's command should not fall off the
         end of it here. The classpath separator changes from ; to : and nothing
         else does. */
      "javac -cp \"target/classes:$WILDFLY_HOME/bin/client/jboss-client.jar\" -d remote-client/classes remote-client/src/com/globalbank/client/RemoteClient.java": [
        "(no output — javac is silent when it succeeds)",
        "<span class='y'># Note what is NOT on that classpath: the war, and the bean.</span>",
        "<span class='y'># The client needs the INTERFACE. The implementation stays on the server.</span>"
      ],
      "java -cp \"remote-client/classes:target/classes:$WILDFLY_HOME/bin/client/jboss-client.jar\" com.globalbank.client.RemoteClient": [
        "Connected to the bank running inside WildFly.",
        "",
        "3 account(s), copied down the wire:",
        "  Account 1001 | Ada Lovelace | balance 1500.00 | @Version 0",
        "  Account 1002 | Grace Hopper | balance 250.00 | @Version 0",
        "  Account 1003 | Alan Turing | balance 0.00 | @Version 0",
        "",
        "Asking the server to move 25.00 from 1001 to 1002...",
        "  <span class='g'>Transferred 25.00 from 1001 (Ada Lovelace) to 1002 (Grace Hopper)</span>",
        "",
        "3 account(s), copied down the wire:",
        "  Account 1001 | Ada Lovelace | balance 1475.00 | @Version 1",
        "  Account 1002 | Grace Hopper | balance 275.00 | @Version 1",
        "  Account 1003 | Alan Turing | balance 0.00 | @Version 0",
        "",
        "Now asking for something the bank will refuse:",
        "<span class='r'>The call reached the server, and the bank refused:</span>",
        "  com.globalbank.ejb.InsufficientFundsException",
        "  Account 1003 holds 0.00, so 99999.00 cannot be transferred",
        "",
        "<span class='y'># Two rows changed, in another process, from a plain Java program.</span>"
      ]
    },
    practicum3: {
      /* Practical 3 deployes the SAME war name as Practical 2 — deliberately, so
         it replaces it rather than sitting beside it. That means the build and
         deploy output below looks deceptively similar to practicum2's. It is
         not a copy: this project has three tables instead of two, two seeded
         users, a security mechanism, and an interceptor. If you are editing one
         of these packs, check which one you are in. */
      "help": [
        "Commands you can try here (simulated):",
        "  ls                        list files in this folder",
        "  cat pom.xml               show this project's build file",
        "  mvn clean package         build the project into target/",
        "  dir target                confirm the war was built",
        "  standalone.bat            start WildFly (simulated)",
        "  jboss-cli.bat --connect   connect to the management CLI",
        "  deploy target/globalbank-web.war   deploy it, and watch the users get seeded",
        "  undeploy globalbank-web.war        take it back off the server",
        "  clear                     clear the screen",
        "  <span class='y'># Two more worth trying, once you have deployed:</span>",
        "  <span class='y'>#   standalone.bat -Dglobalbank.auditDepth=5   change the audit depth</span>",
        "  <span class='y'>#   deployment-info --name=globalbank-web.war  is it actually running?</span>"
      ],
      "ls": ["pom.xml   src/   target/   README.md"],
      "dir": ["pom.xml   src   target   README.md"],
      "standalone.bat": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "standalone.sh": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080"
      ],
      "standalone.bat -Dglobalbank.auditDepth=5": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080",
        "<span class='y'># The system property is now readable by BankConfiguration.auditDepth(),</span>",
        "<span class='y'># which is a @Produces method — a value worked out, not a value known.</span>"
      ],
      "standalone.sh -Dglobalbank.auditDepth=5": [
        "=========================================================================",
        "  JBoss Bootstrap Environment",
        "=========================================================================",
        "WFLYSRV0025: WildFly Full 30.0.1.Final started in 3512ms",
        "  Admin console: http://localhost:9990",
        "  Welcome page:  http://localhost:8080",
        "<span class='y'># The system property is now readable by BankConfiguration.auditDepth(),</span>",
        "<span class='y'># which is a @Produces method — a value worked out, not a value known.</span>"
      ],
      "jboss-cli.bat --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "jboss-cli.sh --connect": [
        "[standalone@localhost:9990 /] ",
        "Connected to the WildFly management CLI. Type 'help' for options, 'quit' to exit."
      ],
      "cat pom.xml": [
        "<project>",
        "  <groupId>com.globalbank</groupId>        <!-- the same pretend bank -->",
        "  <artifactId>globalbank-web</artifactId>  <!-- SAME NAME as Practical 2, on purpose -->",
        "  <version>1.0.0</version>",
        "  <packaging>war</packaging>               <!-- still one war; the EAR is reference only -->",
        "  <dependencies>",
        "    <dependency>",
        "      <groupId>jakarta.platform</groupId>",
        "      <artifactId>jakarta.jakartaee-api</artifactId>",
        "      <version>10.0.0</version>",
        "      <scope>provided</scope>              <!-- still provided, still one -->",
        "    </dependency>",
        "  </dependencies>",
        "</project>",
        "<span class='y'># Security and CDI are inside that same API jar. No new dependency</span>",
        "<span class='y'># was added to get them — WildFly already implements both.</span>"
      ],
      "mvn clean package": [
        "[INFO] Scanning for projects...",
        "[INFO] Building globalbank-web 1.0.0",
        "[INFO] --- maven-resources-plugin: copy-resources ---",
        "[INFO] Copying 1 resource to target/classes",
        "[INFO] --- maven-compiler-plugin: compile ---",
        "[INFO] Compiling 31 source files to target/classes",
        "[INFO] --- maven-war-plugin: war ---",
        "[INFO] Building war: target/globalbank-web.war",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='g'>[INFO] BUILD SUCCESS</span>",
        "[INFO] Total time:  7.402 s",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='y'># 15 source files in Practical 2, 31 here. The build file did not change.</span>"
      ],
      "dir target": [
        "classes/",
        "globalbank-web/             <-- the exploded war, useful for peeking inside",
        "globalbank-web.war          <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "ls target": [
        "classes/",
        "globalbank-web/             <-- the exploded war, useful for peeking inside",
        "globalbank-web.war          <-- this is the file you deploy",
        "maven-archiver/",
        "maven-status/"
      ],
      "copy target\\globalbank-web.war c:\\wildfly\\standalone\\deployments\\": [
        "        1 file(s) copied.",
        "<span class='y'># If Practical 2's war is still in that folder it must be removed first:</span>",
        "<span class='y'># two deployments with the same runtime name is a conflict, not an upgrade.</span>"
      ],
      "cp target/globalbank-web.war ~/wildfly/standalone/deployments/": [
        "(no output — cp is silent when it succeeds)",
        "<span class='y'># If Practical 2's war is still in that folder it must be removed first:</span>",
        "<span class='y'># two deployments with the same runtime name is a conflict, not an upgrade.</span>"
      ],
      "dir c:\\wildfly\\standalone\\deployments": [
        "globalbank-web.war",
        "globalbank-web.war.deployed   <-- WildFly created this; the app is live"
      ],
      "deploy target/globalbank-web.war": [
        "<span class='g'>Deployed \"globalbank-web.war\" (runtime-name: \"globalbank-web.war\")</span>",
        "WFLYSRV0010: Deployed \"globalbank-web.war\"",
        "<span class='y'># JPA now creates THREE tables, not two:</span>",
        "#   create table accounts   (id, holder, balance, version, ...)",
        "#   create table audit_log  (id, action, outcome, detail, happened_at)",
        "#   <span class='g'>create table bank_users (username, password_hash, group_name)</span>",
        "AMQ122005: Deployed queue \"TransferQueue\"",
        "<span class='y'># The security mechanism from @CustomFormAuthenticationMechanismDefinition:</span>",
        "#   CustomFormAuthenticationMechanism -> login page /login.xhtml",
        "<span class='y'># The identity store from @DatabaseIdentityStoreDefinition:</span>",
        "#   DatabaseIdentityStore -> java:jboss/datasources/ExampleDS",
        "#   callerQuery: SELECT password_hash FROM bank_users WHERE username = ?",
        "#   groupsQuery: SELECT group_name    FROM bank_users WHERE username = ?",
        "#   hashAlgorithm: Pbkdf2PasswordHash",
        "[Users] seeded 2 users: ada@globalbank.test (CUSTOMER), manager@globalbank.test (BANK_MANAGER)",
        "[Users] created ada@globalbank.test in group CUSTOMER",
        "[Users] created manager@globalbank.test in group BANK_MANAGER",
        "<span class='y'># The interceptor. No beans.xml entry was needed:</span>",
        "#   BankAuditInterceptor -> @Logged, enabled by @Priority(2000)  APPLICATION",
        "EJB AccountService        -> declared roles: CUSTOMER, BANK_MANAGER",
        "#   transfer()   @RolesAllowed({\"CUSTOMER\",\"BANK_MANAGER\"})",
        "#   open()       @RolesAllowed(\"CUSTOMER\")",
        "#   transferOrRefuse()  @PermitAll   <span class='y'>&lt;-- called by the MDB, which has no caller</span>",
        "EJB AuditService          -> record() @PermitAll, recent() @RolesAllowed(\"BANK_MANAGER\")",
        "WFLYUT0021: Registered web context: '/globalbank-web'",
        "<span class='y'># web.xml protects /accounts.xhtml and /account — NOT /login.xhtml,</span>",
        "<span class='y'># or the redirect would loop. There is deliberately no &lt;login-config&gt;.</span>",
        "<span class='y'># Open it in a browser: http://localhost:8080/globalbank-web/</span>",
        "<span class='y'># You will be sent to /login.xhtml. Try ada@globalbank.test / secret</span>"
      ],
      "deployment-info --name=globalbank-web.war": [
        "NAME                     RUNTIME-NAME             PERSISTENT RUNTIME-STATUS",
        "globalbank-web.war       globalbank-web.war       true       <span class='g'>OK</span>"
      ],
      "undeploy globalbank-web.war": ["Undeployed \"globalbank-web.war\""]
    },
    practicum3ear: {
      /* The multi-module EAR from the Session 14 section of Practical 3's page.
         The page shows it as REFERENCE — this folder guarantees the war, not the
         EAR — so the simulated build here is a Reactor build of four projects.
         Keep it separate from practicum3: one project and four projects produce
         very different output, and blurring them would teach the wrong thing. */
      "help": [
        "Commands you can try here (simulated):",
        "  ls                        list the reactor's modules",
        "  cat pom.xml               show the PARENT pom (packaging 'pom')",
        "  mvn clean package         build all three modules from the parent",
        "  dir globalbank-ear\\target which of the three artefacts came out",
        "  deploy globalbank-ear-1.0.0.ear    deploy the whole thing as one unit",
        "  undeploy globalbank-ear-1.0.0.ear",
        "  clear                     clear the screen",
        "  <span class='y'># Notice what changed: the URL, the JNDI module name, and the number</span>",
        "  <span class='y'># of Maven projects. Everything else is the same application.</span>"
      ],
      "ls": ["globalbank-ear/   globalbank-ejb/   globalbank-web/   pom.xml   README.md"],
      "dir": ["globalbank-ear   globalbank-ejb   globalbank-web   pom.xml   README.md"],
      "cat pom.xml": [
        "<project>",
        "  <groupId>com.globalbank</groupId>",
        "  <artifactId>globalbank-parent</artifactId>   <!-- the parent: builds nothing itself -->",
        "  <version>1.0.0</version>",
        "  <packaging>pom</packaging>                   <!-- pom, not war -->",
        "",
        "  <modules>                                    <!-- build these three, in order -->",
        "    <module>globalbank-ejb</module>            <!-- the beans, <packaging>ejb</packaging> -->",
        "    <module>globalbank-web</module>            <!-- the pages, <packaging>war</packaging> -->",
        "    <module>globalbank-ear</module>            <!-- the package itself, <packaging>ear</packaging> -->",
        "  </modules>",
        "",
        "  <dependencyManagement>                       <!-- declare each version ONCE -->",
        "    <dependencies>",
        "      <dependency>",
        "        <groupId>jakarta.platform</groupId>",
        "        <artifactId>jakarta.jakartaee-api</artifactId>",
        "        <version>10.0.0</version>              <!-- modules below omit <version> -->",
        "        <scope>provided</scope>",
        "      </dependency>",
        "    </dependencies>",
        "  </dependencyManagement>",
        "</project>"
      ],
      "mvn clean package": [
        "[INFO] Scanning for projects...",
        "[INFO] ------------------------------------------------------------------------",
        "[INFO] Reactor Build Order:",
        "[INFO]",
        "[INFO] globalbank-parent                                 [pom]",
        "[INFO] globalbank-ejb                                    [ejb]",
        "[INFO] globalbank-web                                    [war]",
        "[INFO] globalbank-ear                                    [ear]",
        "[INFO]",
        "[INFO] Building globalbank-ejb 1.0.0",
        "[INFO] --- maven-compiler-plugin: compile ---",
        "[INFO] Compiling 24 source files to globalbank-ejb/target/classes",
        "[INFO] Building ejb: globalbank-ejb/target/globalbank-ejb.jar",
        "[INFO]",
        "[INFO] Building globalbank-web 1.0.0",
        "[INFO] --- maven-compiler-plugin: compile ---",
        "[INFO] Compiling 7 source files to globalbank-web/target/classes",
        "<span class='y'>[INFO] --- maven-war-plugin: war ---</span>",
        "<span class='y'>[INFO] Packaging webapp — WEB-INF/lib is empty: the beans come from the EAR</span>",
        "[INFO] Building war: globalbank-web/target/globalbank-web.war",
        "[INFO]",
        "[INFO] Building globalbank-ear 1.0.0",
        "[INFO] --- maven-ear-plugin: ear ---",
        "[INFO] Building ear: globalbank-ear/target/globalbank-ear-1.0.0.ear",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='g'>[INFO] BUILD SUCCESS</span>",
        "[INFO] ------------------------------------------------------------------------",
        "<span class='y'># Four folders, one command. That is what a reactor build is for.</span>"
      ],
      "dir globalbank-ear\\target": [
        "globalbank-ear-1.0.0.ear   <-- one file, containing both modules",
        "maven-archiver/",
        "maven-status/"
      ],
      "ls globalbank-ear/target": [
        "globalbank-ear-1.0.0.ear   <-- one file, containing both modules",
        "maven-archiver/",
        "maven-status/"
      ],
      "copy globalbank-ear\\target\\globalbank-ear-1.0.0.ear c:\\wildfly\\standalone\\deployments\\": [
        "        1 file(s) copied."
      ],
      "cp globalbank-ear/target/globalbank-ear-1.0.0.ear ~/wildfly/standalone/deployments/": [
        "(no output — cp is silent when it succeeds)"
      ],
      "deploy globalbank-ear-1.0.0.ear": [
        "<span class='g'>Deployed \"globalbank-ear-1.0.0.ear\" (runtime-name: \"globalbank-ear-1.0.0.ear\")</span>",
        "WFLYSRV0010: Deployed \"globalbank-ear-1.0.0.ear\"",
        "<span class='y'># One EAR, two sub-deployments:</span>",
        "#   globalbank-ejb.jar    <-- the beans, in their own module now",
        "#   globalbank-web.war    <-- the pages, unchanged",
        "<span class='y'># The library directory, from &lt;defaultLibBundleDir&gt;lib&lt;/defaultLibBundleDir&gt;:</span>",
        "#   lib/                  <-- shared jars go here, on every module's classpath",
        "<span class='y'># THE JNDI NAME CHANGED. Read the middle segment:</span>",
        "#   before:  ejb:/<span class='y'>globalbank-web</span>/AccountService!com.globalbank.ejb.AccountServiceRemote",
        "#   now:     ejb:/<span class='g'>globalbank-ejb</span>/AccountService!com.globalbank.ejb.AccountServiceRemote",
        "<span class='y'># It names the module the BEAN lives in, not the one that deployed it.</span>",
        "<span class='y'># This is the breakage Session 14 warns about. RemoteClient fails here.</span>",
        "<span class='y'># And the URL changed too, from &lt;contextRoot&gt;:</span>",
        "#   before:  http://localhost:8080/globalbank-web/",
        "#   now:     http://localhost:8080/<span class='g'>globalbank</span>/",
        "[Users] seeded 2 users: ada@globalbank.test (CUSTOMER), manager@globalbank.test (BANK_MANAGER)",
        "<span class='y'># Same application. Same login. Same roles. Different shape.</span>"
      ],
      "deployment-info --name=globalbank-ear-1.0.0.ear": [
        "NAME                          RUNTIME-NAME                  PERSISTENT RUNTIME-STATUS",
        "globalbank-ear-1.0.0.ear      globalbank-ear-1.0.0.ear      true       <span class='g'>OK</span>",
        "  sub-deployment              globalbank-ejb.jar                       <span class='g'>OK</span>",
        "  sub-deployment              globalbank-web.war                       <span class='g'>OK</span>"
      ],
      "undeploy globalbank-ear-1.0.0.ear": ["Undeployed \"globalbank-ear-1.0.0.ear\""]
    }
  };

  function initTerminal(term) {
    var pack = Object.assign({}, BASE, PACKS[term.getAttribute("data-terminal")] || {});
    var history = [];
    var hpos = -1;

    var bar = term.querySelector(".terminal__bar") || term.appendChild(document.createElement("div"));
    bar.className = "terminal__bar";
    bar.innerHTML = "<span class='lights'><i></i><i></i><i></i></span> simulated shell — nothing here touches your computer";

    var screen = term.querySelector(".terminal__screen") || term.appendChild(document.createElement("div"));
    screen.className = "terminal__screen";
    if (!screen.textContent.trim()) {
      screen.innerHTML = "<div class='out'>Type <b>help</b> and press Enter to see what you can run.</div>";
    }

    var lineWrap = term.querySelector(".terminal__inputline") || term.appendChild(document.createElement("div"));
    lineWrap.className = "terminal__inputline";
    lineWrap.innerHTML = "<span class='prompt'>you@workshop:~$</span>";
    var input = document.createElement("input");
    input.type = "text";
    input.setAttribute("aria-label", "Type a command");
    input.autocapitalize = "off";
    input.autocomplete = "off";
    input.spellcheck = false;
    lineWrap.appendChild(input);

    function print(html, cls) {
      var d = document.createElement("div");
      d.className = cls || "out";
      d.innerHTML = html;
      screen.appendChild(d);
      screen.scrollTop = screen.scrollHeight;
    }

    function runCmd(raw) {
      var cmd = raw.trim().replace(/\s+/g, " ");
      print("<span class='prompt'>you@workshop:~$</span> <span class='cmd'>" + esc(raw) + "</span>", "");
      if (!cmd) return;
      history.push(raw); hpos = history.length;

      if (cmd === "clear") { screen.innerHTML = ""; return; }

      var hit = pack[cmd];
      // loose match: allow "./script.sh" for "script.sh", and "./mvnw" for "mvn"
      if (!hit && cmd.indexOf("./") === 0) hit = pack[cmd.slice(2)];
      if (!hit && /^\.?\/?mvnw? /.test(cmd)) hit = pack[cmd.replace(/^\.?\/?mvnw?/, "mvn")];
      // cd succeeds silently in a real shell, so print nothing
      if (!hit && /^cd /.test(cmd)) hit = [""];
      if (!hit && /^(deploy|undeploy) /.test(cmd)) {
        hit = ["<span class='y'>(simulated) " + esc(cmd) + " — connect with jboss-cli first, then name the archive inside your target/ folder</span>"];
      }

      if (hit === "__CLEAR__") { screen.innerHTML = ""; return; }
      if (hit) {
        print(hit.join("\n"));
      } else if (cmd === "quit" || cmd === "exit") {
        print("bye 👋");
      } else {
        print("<span class='r'>" + esc(cmd.split(" ")[0]) + ": command not found</span>\nThis is a teaching terminal. Try <b>help</b>.");
      }
    }

    input.addEventListener("keydown", function (e) {
      if (e.key === "Enter") { runCmd(input.value); input.value = ""; }
      else if (e.key === "ArrowUp") { if (hpos > 0) { hpos--; input.value = history[hpos] || ""; e.preventDefault(); } }
      else if (e.key === "ArrowDown") { if (hpos < history.length - 1) { hpos++; input.value = history[hpos] || ""; } else { hpos = history.length; input.value = ""; } }
    });

    term.addEventListener("click", function (e) {
      if (window.getSelection && String(window.getSelection())) return; // let people select text
      input.focus();
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".terminal[data-terminal], .terminal.js-terminal").forEach(initTerminal);
  });
})();
