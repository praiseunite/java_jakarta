/* ==========================================================================
   course.js — shared shell: sidebar, topbar, progress, quiz, footer nav
   Every page sets  window.PAGE = "<slug>"  before loading this file.
   Content lives in a single <main class="page"> … </main>.
   ========================================================================== */
(function () {
  "use strict";

  /* ---- Course manifest (order = learning path) ------------------------- */
  var MANIFEST = [
    { group: "Prerequisites" },
    { slug: "index",                 file: "index.html",                 unit: "Home",    title: "Overview & how to use this" },
    { slug: "00-foundations",        file: "00-foundations.html",        unit: "Prep",    title: "Programming from absolute zero" },
    { slug: "01-setup",              file: "01-setup.html",              unit: "Prep",    title: "Set up your environment" },

    { group: "Week 1 — Core EJB Architecture" },
    { slug: "s01-02",  file: "session-01-02-intro-and-beans.html",  unit: "Session 1–2", title: "Introduction & Session Bean Types" },
    { slug: "s03",     file: "session-03-resource-creation.html",   unit: "Session 3",   title: "Resource Creation in Jakarta EJB" },
    { slug: "s04",     file: "session-04-working-with-ejb.html",    unit: "Session 4",   title: "Working with Jakarta Enterprise Beans" },
    { slug: "s04t",    file: "session-04-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 1–4" },

    { group: "Week 2 — Web Tier & Messaging" },
    { slug: "s05",     file: "session-05-facelets.html",            unit: "Session 5",   title: "Facelets in Jakarta EE" },
    { slug: "s06",     file: "session-06-local-remote.html",        unit: "Session 6",   title: "Local & Remote Clients" },
    { slug: "s07t",    file: "session-07-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 5–6" },
    { slug: "s07",     file: "session-07-messaging.html",           unit: "Session 7",   title: "Jakarta Messaging (JMS)" },
    { slug: "s08",     file: "session-08-connectors.html",          unit: "Session 8",   title: "Jakarta Connectors Architecture" },
    { slug: "s08t",    file: "session-08-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 7–8" },

    { group: "Week 3 — Validation & Advanced Transactions" },
    { slug: "s09",     file: "session-09-bean-validation.html",     unit: "Session 9",   title: "Bean Validation" },
    { slug: "s10",     file: "session-10-ejb-transactions.html",    unit: "Session 10",  title: "EJB Transaction Execution & JNDI" },
    { slug: "s10t",    file: "session-10-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 9–10" },

    { group: "Week 4 — Jakarta CDI Framework" },
    { slug: "s11",     file: "session-11-cdi-part1.html",           unit: "Session 11",  title: "Jakarta CDI – Part I" },
    { slug: "s12",     file: "session-12-cdi-part2.html",           unit: "Session 12",  title: "Jakarta CDI – Part II" },
    { slug: "s12t",    file: "session-12-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 11–12" },

    { group: "Week 5 — Security & Packaging" },
    { slug: "s13",     file: "session-13-cdi-beans-security.html",  unit: "Session 13",  title: "CDI Beans & Security" },
    { slug: "s14",     file: "session-14-packaging.html",           unit: "Session 14",  title: "Packaging Jakarta Applications" },
    { slug: "s14t",    file: "session-14-tiy.html",                 unit: "Review",      title: "Try It Yourself: Sessions 13–14" },

    { group: "Bonus Material" },
    { slug: "13-jpa-persistence", file: "13-jpa-persistence.html",   unit: "Bonus",       title: "JPA: Storing Objects Properly" },
    { slug: "15-rest-jaxrs",      file: "15-rest-jaxrs.html",        unit: "Bonus",       title: "REST APIs with JAX-RS" },
    { slug: "16-testing",         file: "16-testing.html",           unit: "Bonus",       title: "Testing & Reading Errors" },

    { group: "Reference" },
    { slug: "glossary",           file: "glossary.html",             unit: "Ref",         title: "Glossary — every term, plain words" }
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
