/* ==========================================================================
   course.js — shared shell: sidebar, topbar, progress, quiz, footer nav
   Every page sets  window.PAGE = "<slug>"  before loading this file.
   Content lives in a single <main class="page"> … </main>.
   ========================================================================== */
(function () {
  "use strict";

  /* ---- Course manifest (order = learning path) ------------------------- */
  var MANIFEST = [
    { group: "Start here" },
    { slug: "index",                 file: "index.html",                 unit: "Home",    title: "Overview & how to use this" },
    { slug: "00-foundations",        file: "00-foundations.html",        unit: "Unit 00", title: "Programming from absolute zero" },
    { slug: "01-setup",              file: "01-setup.html",              unit: "Unit 01", title: "Set up your workshop" },

    { group: "The core idea" },
    { slug: "02-what-is-jakarta-ee", file: "02-what-is-jakarta-ee.html", unit: "Unit 02", title: "What Jakarta EE actually is" },
    { slug: "03-session-beans",      file: "03-session-beans.html",      unit: "Unit 03", title: "Session beans: stateless, stateful, singleton" },
    { slug: "04-entity-and-mdb",     file: "04-entity-and-mdb.html",     unit: "Unit 04", title: "Entity beans & message-driven beans" },

    { group: "Wiring things together" },
    { slug: "05-jndi-and-resources", file: "05-jndi-and-resources.html", unit: "Unit 05", title: "JNDI, DataSources & connection pools" },
    { slug: "06-transactions",       file: "06-transactions.html",       unit: "Unit 06", title: "Transactions & the ACID rules" },
    { slug: "07-concurrency-security",file:"07-concurrency-security.html",unit: "Unit 07", title: "Concurrency, locking & security" },
    { slug: "08-review-integration", file: "08-review-integration.html", unit: "Unit 08", title: "Putting it together + debugging" },

    { group: "Building the web tier" },
    { slug: "09-facelets-jsf",       file: "09-facelets-jsf.html",       unit: "Unit 09", title: "Facelets & Jakarta Faces (JSF)" },
    { slug: "10-local-remote-clients",file:"10-local-remote-clients.html",unit: "Unit 10", title: "Local vs remote clients" },
    { slug: "11-messaging-jms",      file: "11-messaging-jms.html",      unit: "Unit 11", title: "Messaging with JMS" },
    { slug: "12-connectors-jdbc",    file: "12-connectors-jdbc.html",    unit: "Unit 12", title: "Connectors (JCA) & JDBC" },

    { group: "Filling the gaps" },
    { slug: "13-jpa-persistence",    file: "13-jpa-persistence.html",    unit: "Unit 13", title: "JPA: storing objects properly" },
    { slug: "14-cdi",               file: "14-cdi.html",                unit: "Unit 14", title: "CDI: injection & scopes" },
    { slug: "15-rest-jaxrs",        file: "15-rest-jaxrs.html",         unit: "Unit 15", title: "REST APIs with JAX-RS" },
    { slug: "16-testing",           file: "16-testing.html",            unit: "Unit 16", title: "Testing & reading errors" },

    { group: "Reference" },
    { slug: "glossary",            file: "glossary.html",              unit: "Ref",     title: "Glossary — every term, plain words" }
  ];

  var PAGES = MANIFEST.filter(function (m) { return m.slug; });
  var SLUG = window.PAGE || "index";
  var idx = PAGES.findIndex(function (p) { return p.slug === SLUG; });

  /* ---- localStorage helpers (private-mode safe) ---------------------- */
  var STORE = "ejbz.progress.v1";
  function readDone() {
    try { return JSON.parse(localStorage.getItem(STORE) || "{}") || {}; }
    catch (e) { return {}; }
  }
  function writeDone(obj) {
    try { localStorage.setItem(STORE, JSON.stringify(obj)); } catch (e) {}
  }
  var done = readDone();

  /* ---- Theme ------------------------------------------------------- */
  var TKEY = "ejbz.theme";
  function applyTheme(t) {
    if (t === "light" || t === "dark") document.documentElement.setAttribute("data-theme", t);
    else document.documentElement.removeAttribute("data-theme");
  }
  try { applyTheme(localStorage.getItem(TKEY)); } catch (e) {}
  function cycleTheme() {
    var cur;
    try { cur = localStorage.getItem(TKEY); } catch (e) { cur = null; }
    var next = cur === "dark" ? "light" : cur === "light" ? "system" : "dark";
    try { next === "system" ? localStorage.removeItem(TKEY) : localStorage.setItem(TKEY, next); } catch (e) {}
    applyTheme(next === "system" ? null : next);
    updateThemeLabel();
  }
  function updateThemeLabel() {
    var el = document.getElementById("themeLabel");
    if (!el) return;
    var cur;
    try { cur = localStorage.getItem(TKEY); } catch (e) { cur = null; }
    el.textContent = cur === "dark" ? "Dark" : cur === "light" ? "Light" : "Auto";
  }

  /* ---- Build shell ---------------------------------------------- */
  function el(tag, cls, html) {
    var n = document.createElement(tag);
    if (cls) n.className = cls;
    if (html != null) n.innerHTML = html;
    return n;
  }

  function pct() {
    var total = PAGES.filter(function (p) { return p.slug !== "index" && p.slug !== "glossary"; }).length;
    var d = Object.keys(done).length;
    return Math.min(100, Math.round((d / total) * 100));
  }

  function buildSidebar() {
    var sb = el("aside", "sidebar");
    sb.setAttribute("aria-label", "Course contents");

    var brand = el("a", "sidebar__brand");
    brand.href = "index.html";
    brand.innerHTML = "<b>Enterprise Java,<br>From Zero</b><span>Jakarta EE for total beginners</span>";
    sb.appendChild(brand);

    var prog = el("div", "sidebar__progress");
    prog.innerHTML = "Progress · <span id='pctText'>" + pct() + "%</span><span class='sidebar__bar'><i id='pctBar'></i></span>";
    sb.appendChild(prog);

    var list = el("ul", "toc");
    MANIFEST.forEach(function (m) {
      if (m.group) { list.appendChild(el("li", "toc__group", m.group)); return; }
      var li = el("li");
      var a = el("a");
      a.href = m.file;
      if (m.slug === SLUG) a.setAttribute("aria-current", "page");
      if (done[m.slug]) a.classList.add("is-done");
      var n = m.unit.replace(/[^0-9]/g, "") || "·";
      a.innerHTML = "<span class='num'>" + n + "</span><span class='label'>" + m.title + "</span><span class='tick'>✓</span>";
      li.appendChild(a);
      list.appendChild(li);
    });
    sb.appendChild(list);
    return sb;
  }

  function buildTopbar() {
    var tb = el("div", "topbar");
    var mt = el("button", "iconbtn menu-toggle", "☰ Menu");
    mt.setAttribute("aria-label", "Open contents");
    mt.addEventListener("click", function () {
      document.querySelector(".sidebar").classList.toggle("open");
      document.querySelector(".scrim").classList.toggle("show");
    });
    tb.appendChild(mt);

    var pos = el("span", "topbar__pos");
    if (idx >= 0 && SLUG !== "index" && SLUG !== "glossary") {
      var contentPages = PAGES.filter(function (p) { return p.slug !== "index" && p.slug !== "glossary"; });
      var ci = contentPages.findIndex(function (p) { return p.slug === SLUG; });
      pos.textContent = PAGES[idx].unit + " · " + (ci + 1) + " / " + contentPages.length;
    } else {
      pos.textContent = "Enterprise Java, From Zero";
    }
    tb.appendChild(pos);

    tb.appendChild(el("span", "topbar__spacer"));

    var th = el("button", "iconbtn", "◐ Theme: <span id='themeLabel'>Auto</span>");
    th.setAttribute("aria-label", "Switch colour theme");
    th.addEventListener("click", cycleTheme);
    tb.appendChild(th);
    return tb;
  }

  function buildFooterNav() {
    if (SLUG === "index") return null;
    var foot = el("nav", "pagefoot");
    foot.setAttribute("aria-label", "Between units");

    if (SLUG !== "glossary") {
      var btn = el("button", "markdone");
      btn.type = "button";
      var isDone = !!done[SLUG];
      btn.setAttribute("aria-pressed", String(isDone));
      btn.textContent = isDone ? "Marked complete" : "Mark this unit complete";
      btn.addEventListener("click", function () {
        done = readDone();
        if (done[SLUG]) delete done[SLUG]; else done[SLUG] = Date.now();
        writeDone(done);
        var now = !!done[SLUG];
        btn.setAttribute("aria-pressed", String(now));
        btn.textContent = now ? "Marked complete" : "Mark this unit complete";
        refreshProgress();
      });
      foot.appendChild(btn);
    }

    var prev = PAGES[idx - 1], next = PAGES[idx + 1];
    if (prev) {
      var pa = el("a", "nav prev");
      pa.href = prev.file;
      pa.innerHTML = "<span class='d'>← Previous</span><span>" + prev.title + "</span>";
      foot.appendChild(pa);
    }
    if (next) {
      var na = el("a", "nav next");
      na.href = next.file;
      na.innerHTML = "<span class='d'>Next →</span><span>" + next.title + "</span>";
      foot.appendChild(na);
    }
    return foot;
  }

  function refreshProgress() {
    done = readDone();
    var p = pct();
    var t = document.getElementById("pctText"), b = document.getElementById("pctBar");
    if (t) t.textContent = p + "%";
    if (b) b.style.width = p + "%";
    document.querySelectorAll(".toc a").forEach(function (a) {
      var href = a.getAttribute("href");
      var m = PAGES.find(function (x) { return x.file === href; });
      if (m) a.classList.toggle("is-done", !!done[m.slug]);
    });
    document.querySelectorAll(".card[data-slug]").forEach(function (c) {
      c.classList.toggle("is-done", !!done[c.getAttribute("data-slug")]);
    });
  }

  /* ---- Quiz engine ------------------------------------------------ */
  function buildQuizzes() {
    document.querySelectorAll(".quiz[data-quiz]").forEach(function (box) {
      var raw = box.querySelector("script[type='application/json']");
      if (!raw) return;
      var items;
      try { items = JSON.parse(raw.textContent); } catch (e) { return; }
      raw.remove();
      items.forEach(function (it, qi) {
        var q = el("div", "quiz__q");
        q.appendChild(el("p", null, (qi + 1) + ". " + it.q));
        var ul = el("ul", "quiz__opts");
        var fb = el("p", "quiz__fb");
        it.opts.forEach(function (opt, oi) {
          var li = el("li");
          var b = el("button");
          b.type = "button";
          b.innerHTML = "<span class='mk'>" + "ABCDEF"[oi] + "</span><span>" + opt + "</span>";
          b.addEventListener("click", function () {
            var correct = oi === it.a;
            ul.querySelectorAll("button").forEach(function (x, xi) {
              x.disabled = true;
              if (xi === it.a) x.classList.add("is-correct");
            });
            if (!correct) b.classList.add("is-wrong");
            fb.innerHTML = (correct ? "<b>Correct.</b> " : "<b>Not quite.</b> ") + (it.why || "");
            fb.classList.add("show");
          });
          li.appendChild(b);
          ul.appendChild(li);
        });
        q.appendChild(ul);
        q.appendChild(fb);
        box.appendChild(q);
      });
    });
  }

  /* ---- Assemble ------------------------------------------------- */
  document.addEventListener("DOMContentLoaded", function () {
    var page = document.querySelector("main.page");
    if (!page) return;

    var app = el("div", "app");
    var sidebar = buildSidebar();
    var main = el("div", "main");
    var scrim = el("div", "scrim");
    scrim.addEventListener("click", function () {
      sidebar.classList.remove("open");
      scrim.classList.remove("show");
    });

    main.appendChild(buildTopbar());
    page.parentNode.insertBefore(app, page);
    main.appendChild(page);
    var foot = buildFooterNav();
    if (foot) page.appendChild(foot);
    app.appendChild(sidebar);
    app.appendChild(main);
    document.body.appendChild(scrim);

    updateThemeLabel();
    refreshProgress();
    buildQuizzes();

    // expose a tiny hook for other scripts / index page
    window.Course = { PAGES: PAGES, done: function () { return readDone(); }, refreshProgress: refreshProgress };
    document.dispatchEvent(new CustomEvent("course:ready"));
  });
})();
