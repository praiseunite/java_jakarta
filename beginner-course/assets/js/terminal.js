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
      ]
    },
    deploy: {
      "deploy target/hello-ejb.war": [
        "<span class='g'>Deployed \"hello-ejb.war\" (runtime-name: \"hello-ejb.war\")</span>",
        "WFLYUT0021: Registered web context: '/hello-ejb'",
        "java:global/hello-ejb/GreeterBean!com.example.GreeterRemote"
      ],
      "undeploy hello-ejb.war": ["Undeployed \"hello-ejb.war\""]
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
      // loose match: allow "./mvnw" for "mvn", trailing args on jboss deploy
      if (!hit) {
        if (/^\.?\/?mvnw? /.test(cmd)) hit = pack[cmd.replace(/^\.?\/?mvnw?/, "mvn")];
        if (!hit && /^(deploy|undeploy) /.test(cmd)) {
          hit = ["<span class='y'>(simulated) " + esc(cmd) + " — connect with jboss-cli first, then deploy target/hello-ejb.war</span>"];
        }
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
