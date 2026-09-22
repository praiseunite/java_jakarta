/* ==========================================================================
   runner.js — the "Try it" bench engine (100% offline, no Java required)

   Two kinds of bench:

   1. LIVE  <div class="bench" data-run="calc"> … editable <pre class="bench__code">
            A JavaScript "logic twin" re-implements the algorithm, so editing the
            inputs and pressing Run actually recomputes the answer.

   2. CANNED <div class="bench" data-run="canned"> with a
             <script type="text/plain" class="bench__canned">…expected output…</script>
             Run reveals the real console / WildFly server-log text. Used for
             examples that need a running application server.
   ========================================================================== */
(function () {
  "use strict";

  var reduced = window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  /* ---- small helpers ------------------------------------------------ */
  function nums(text) {
    var m = text.match(/-?\d+(\.\d+)?/g);
    return m ? m.map(Number) : [];
  }
  function firstString(text) {
    var m = text.match(/"([^"]*)"|'([^']*)'/);
    return m ? (m[1] != null ? m[1] : m[2]) : null;
  }
  function kv(text, key) {
    var re = new RegExp(key + "\\s*=\\s*(-?\\d+(?:\\.\\d+)?)");
    var m = text.match(re);
    return m ? Number(m[1]) : null;
  }
  function esc(s) {
    return String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }
  function line(cls, s) { return "<span class='" + cls + "'>" + esc(s) + "</span>"; }

  /* ---- logic twins ------------------------------------------------- */
  var RECIPES = {

    /* Session 3 — calculator */
    calc: function (src) {
      var n = nums(src);
      var a = (kv(src, "a") != null) ? kv(src, "a") : (n.length ? n[0] : 10);
      var b = (kv(src, "b") != null) ? kv(src, "b") : (n.length > 1 ? n[1] : 4);
      var out = [];
      out.push(line("dim", "=== ZenTech Calculator Service ==="));
      out.push(a + " + " + b + " = " + (a + b));
      out.push(a + " - " + b + " = " + (a - b));
      out.push(a + " * " + b + " = " + (a * b));
      if (b === 0) out.push(line("err", "divide(" + a + ", 0)  ->  ArithmeticException: Cannot divide by zero!"));
      else out.push(a + " / " + b + " = " + (a / b));
      out.push(line("dim", "================================"));
      out.push("");
      out.push(line("log", "[server] [CalculatorBean] add() called with: " + a + " + " + b));
      out.push(line("log", "[server] [CalculatorBean] divide() called with: " + a + ".0 / " + b + ".0"));
      return out;
    },

    /* Session 1&2 assignment — vowel checker */
    vowels: function (src) {
      var s = firstString(src);
      if (s == null) s = "Jakarta Enterprise Beans";
      var found = [];
      var count = 0;
      for (var i = 0; i < s.length; i++) {
        var c = s[i].toLowerCase();
        if ("aeiou".indexOf(c) > -1) { count++; if (found.indexOf(c) === -1) found.push(c); }
      }
      return [
        'input           : "' + s + '"',
        "containsVowels  : " + (count > 0),
        "countVowels     : " + count,
        "getVowelsFound  : Vowels found: " + found.join(", ")
      ];
    },

    /* Session 6 assignment — palindrome (string + number) */
    palindrome: function (src) {
      var s = firstString(src);
      var num = null;
      if (s == null) {
        var n = nums(src);
        num = n.length ? n[0] : 1221;
      }
      var out = [];
      if (s != null) {
        var cleaned = s.toLowerCase().replace(/[^a-z0-9]/g, "");
        var rev = cleaned.split("").reverse().join("");
        out.push('isStringPalindrome("' + s + '")');
        out.push("  normalised : \"" + cleaned + "\"");
        out.push("  reversed   : \"" + rev + "\"");
        out.push("  result     : " + (cleaned === rev ? "Yes, it is a Palindrome!" : "No, it is not a Palindrome."));
      } else {
        var isPal = num >= 0 && String(num) === String(num).split("").reverse().join("");
        out.push("isNumberPalindrome(" + num + ")");
        if (num < 0) out.push("  negative numbers are never palindromes");
        out.push("  result : " + (isPal ? "Yes, it is a Palindrome!" : "No, it is not a Palindrome."));
      }
      return out;
    },

    /* Session 6 — prime generator */
    primes: function (src) {
      var limit = kv(src, "limit");
      if (limit == null) { var n = nums(src); limit = n.length ? n[0] : 50; }
      limit = Math.max(2, Math.min(2000, Math.floor(limit)));
      var res = [];
      for (var i = 2; i <= limit; i++) {
        var p = true;
        for (var d = 2; d * d <= i; d++) if (i % d === 0) { p = false; break; }
        if (p) res.push(i);
      }
      return [
        "getPrimesUpTo(" + limit + ")",
        "",
        res.join(" "),
        "",
        line("dim", res.length + " primes found")
      ];
    },

    /* Session 6 — string reversal */
    reverse: function (src) {
      var s = firstString(src);
      if (s == null) s = "Jakarta Enterprise Beans";
      return [
        'Enter a string to reverse: ' + s,
        'Reversed string from Remote EJB: ' + s.split("").reverse().join("")
      ];
    },

    /* Session 1&2 assignment — average calculator */
    average: function (src) {
      var arr = nums(src);
      if (!arr.length) return [line("err", "Empty array/list passed — WARNING: returning 0.0"), "average = 0.0"];
      var sum = arr.reduce(function (a, b) { return a + b; }, 0);
      var avg = sum / arr.length;
      return [
        "numbers : [" + arr.join(", ") + "]",
        "count   : " + arr.length,
        "sum     : " + sum,
        "average : " + (Math.round(avg * 1000) / 1000)
      ];
    },

    /* Session 8 — JDBC CRUD, mimicked over an in-memory array */
    jdbc: function () {
      var rows = [];
      var id = 0;
      function ins(u, e, r) { id++; rows.push({ id: id, u: u, e: e, r: r }); return id; }
      function fmt(x) {
        return "[User #" + x.id + "] " + x.u.padEnd(12) + " | " + x.e.padEnd(28) + " | Role: " + x.r.padEnd(10) + " | Joined: 2026-09-06 08:15:00.0";
      }
      var out = [];
      out.push(line("dim", "================================================="));
      out.push(line("dim", "   Enterprise JDBC Data Access Layer Demo"));
      out.push(line("dim", "================================================="));
      out.push(line("ok", "[DAO] Table 'user_accounts' initialized successfully."));
      out.push("");
      out.push(line("dim", "--- Step 1: Inserting Accounts ---"));
      var i1 = ins("alice_admin", "alice@enterprise.com", "ADMIN");
      var i2 = ins("bob_teller", "bob@enterprise.com", "TELLER");
      var i3 = ins("charlie_dev", "charlie@enterprise.com", "DEVELOPER");
      rows.forEach(function (x) { out.push("Inserted: " + fmt(x)); });
      out.push("");
      out.push(line("dim", "--- Step 2: Listing All Accounts ---"));
      rows.forEach(function (x) { out.push(fmt(x)); });
      out.push("");
      out.push(line("dim", "--- Step 3: Updating Email for User #" + i2 + " ---"));
      rows.find(function (x) { return x.id === i2; }).e = "bob.teller.new@enterprise.com";
      out.push("Update successful: true");
      out.push("Refreshed Record: " + fmt(rows.find(function (x) { return x.id === i2; })));
      out.push("");
      out.push(line("dim", "--- Step 4: Deleting User #" + i3 + " ---"));
      rows = rows.filter(function (x) { return x.id !== i3; });
      out.push("Deleted ID #" + i3 + ": true");
      out.push("");
      out.push(line("dim", "--- Step 5: Final Database State ---"));
      rows.forEach(function (x) { out.push(fmt(x)); });
      out.push("");
      out.push(line("ok", "JDBC Lab execution completed successfully!"));
      return out;
    }
  };

  /* polyfill padEnd for very old engines (safety) */
  if (!String.prototype.padEnd) {
    String.prototype.padEnd = function (n, s) {
      s = s || " "; var str = String(this);
      while (str.length < n) str += s;
      return str.slice(0, Math.max(n, String(this).length));
    };
  }

  /* ---- render output (typed reveal unless reduced-motion) --------- */
  function reveal(outEl, lines) {
    outEl.innerHTML = "";
    if (reduced) { outEl.innerHTML = lines.join("\n"); return; }
    var i = 0;
    (function step() {
      if (i >= lines.length) return;
      outEl.innerHTML += (i ? "\n" : "") + lines[i];
      outEl.scrollTop = outEl.scrollHeight;
      i++;
      setTimeout(step, 90);
    })();
  }

  /* ---- wire every bench on the page ------------------------------ */
  function initBench(bench) {
    var kind = bench.getAttribute("data-run");
    var codeEl = bench.querySelector(".bench__code");
    var outEl = bench.querySelector(".bench__out");
    var runBtn = bench.querySelector(".btn-run");
    var resetBtn = bench.querySelector("[data-reset]");
    if (!outEl) { outEl = document.createElement("pre"); outEl.className = "bench__out"; outEl.setAttribute("aria-live", "polite"); bench.appendChild(outEl); }
    var original = codeEl ? codeEl.textContent : "";

    function run() {
      var lines;
      if (kind === "canned") {
        var canned = bench.querySelector(".bench__canned");
        lines = canned ? canned.textContent.replace(/^\n/, "").replace(/\s+$/, "").split("\n") : ["(no output attached)"];
      } else if (RECIPES[kind]) {
        try { lines = RECIPES[kind](codeEl ? codeEl.textContent : ""); }
        catch (e) { lines = [line("err", "Runner error: " + e.message)]; }
      } else {
        lines = [line("err", "Unknown recipe: " + kind)];
      }
      reveal(outEl, lines);
    }

    if (runBtn) runBtn.addEventListener("click", run);
    if (resetBtn && codeEl) resetBtn.addEventListener("click", function () {
      codeEl.textContent = original;
      outEl.innerHTML = "";
    });
    // keyboard: Ctrl/Cmd+Enter inside editable code runs it
    if (codeEl && codeEl.getAttribute("contenteditable") === "true") {
      codeEl.addEventListener("keydown", function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key === "Enter") { e.preventDefault(); run(); }
      });
    }
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".bench[data-run]").forEach(initBench);
  });
})();
