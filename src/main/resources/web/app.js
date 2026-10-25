(function () {
  'use strict';

  var MASK = '********';
  var SECTIONS = [
    { id: 'discord', title: 'Discord', optional: false,
      help: 'Create a bot in the Discord Developer Portal, open Bot, and copy its token.',
      fields: [{ key: 'botToken', label: 'Bot token', secret: true }] },
    { id: 'plex', title: 'Plex', optional: false,
      help: 'Enter the address as host or IP only, without http:// (for example 192.168.1.10). The default Plex port is 32400.',
      fields: [{ key: 'plexIp', label: 'Plex IP or host' },
               { key: 'plexPort', label: 'Plex port', inputmode: 'numeric' },
               { key: 'plexToken', label: 'Plex token', secret: true }] },
    { id: 'tautulli', title: 'Tautulli', optional: true,
      help: 'Enter host:port without http://, for example 192.168.1.10:8181. The API key is under Settings, Web Interface.',
      fields: [{ key: 'tautulliUrl', label: 'Tautulli host:port' },
               { key: 'tautulliApiKey', label: 'Tautulli API key', secret: true }] },
    { id: 'radarr', title: 'Radarr', optional: true,
      help: 'Enter host:port without http://, for example 192.168.1.10:7878. The API key is under Settings, General.',
      fields: [{ key: 'radarrUrl', label: 'Radarr host:port' },
               { key: 'radarrApiKey', label: 'Radarr API key', secret: true }] },
    { id: 'sonarr', title: 'Sonarr', optional: true,
      help: 'Enter host:port without http://, for example 192.168.1.10:8989. The API key is under Settings, General.',
      fields: [{ key: 'sonarrUrl', label: 'Sonarr host:port' },
               { key: 'sonarrApiKey', label: 'Sonarr API key', secret: true }] }
  ];
  var COPY_HELP = ' To keep using an existing message, turn on Developer Mode in Discord (User Settings, Advanced), '
    + 'right-click the message and choose Copy Message Link, then paste it below.';
  var MESSAGE_GROUPS = [
    { id: 'status', title: 'Status message', channelKey: 'statusChannelId', messageKey: 'statusMessageId',
      help: 'The live activity message the bot keeps editing. It must have been written by this bot.' + COPY_HELP,
      fields: [{ key: 'statusChannelId', label: 'Status channel ID', inputmode: 'numeric' },
               { key: 'statusMessageId', label: 'Status message ID', inputmode: 'numeric' }] },
    { id: 'queue', title: 'Download queue message', channelKey: 'queueChannelId', messageKey: 'queueMessageId',
      help: 'The download queue message the bot keeps editing. It must have been written by this bot.' + COPY_HELP,
      fields: [{ key: 'queueChannelId', label: 'Queue channel ID', inputmode: 'numeric' },
               { key: 'queueMessageId', label: 'Queue message ID', inputmode: 'numeric' }] },
    { id: 'role', title: 'Role message', channelKey: 'roleChannelId', messageKey: 'roleMessageId',
      help: 'The message people react to for a role. It must have been written by this bot.' + COPY_HELP
        + ' The role ID is under Server Settings, Roles: right-click the role and choose Copy Role ID.',
      fields: [{ key: 'roleId', label: 'Role ID', inputmode: 'numeric' },
               { key: 'roleChannelId', label: 'Role channel ID', inputmode: 'numeric' },
               { key: 'roleMessageId', label: 'Role message ID', inputmode: 'numeric' }] }
  ];
  var LINK_RE = /^https:\/\/(?:www\.|ptb\.|canary\.)?discord(?:app)?\.com\/channels\/(\d+)\/(\d+)\/(\d+)$/;
  var STATE_LABELS = { NOT_CONFIGURED: 'not configured', STARTING: 'starting', RUNNING: 'running', FAILED: 'failed' };

  var csrf = '';
  var initial = {};
  var revealGen = 0;
  var pollGen = 0;
  var advanced = false;
  var LOG_MAX_CHARS = 200000;
  var LOG_MAX_LINES = 1000;
  var logOffset = -1;
  var logBuf = '';
  var logPaused = false;
  var logOpen = false;
  var logGen = 0;
  var logTimer = null;

  // Every field the form can send, in page order.
  function allFields() {
    var out = [];
    SECTIONS.concat(MESSAGE_GROUPS).forEach(function (s) { out = out.concat(s.fields); });
    return out;
  }

  function $(id) { return document.getElementById(id); }

  function el(tag, props, children) {
    var e = document.createElement(tag);
    Object.keys(props || {}).forEach(function (k) {
      if (k === 'text') { e.textContent = props[k]; } else { e.setAttribute(k, props[k]); }
    });
    (children || []).forEach(function (c) { e.appendChild(c); });
    return e;
  }

  // ---- reveal (eye) button on password-type fields ----
  var SVG_NS = 'http://www.w3.org/2000/svg';
  function eyeIcon(off) {
    var svg = document.createElementNS(SVG_NS, 'svg');
    [['viewBox', '0 0 24 24'], ['width', '22'], ['height', '22'], ['fill', 'none'], ['stroke', 'currentColor'],
     ['stroke-width', '2'], ['stroke-linecap', 'round'], ['stroke-linejoin', 'round'], ['aria-hidden', 'true'],
     ['focusable', 'false']].forEach(function (a) { svg.setAttribute(a[0], a[1]); });
    var paths = ['M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z'];
    if (off) { paths.push('M3 3l18 18'); }
    var shapes = paths.map(function (d) { var p = document.createElementNS(SVG_NS, 'path'); p.setAttribute('d', d); return p; });
    if (!off) {
      var c = document.createElementNS(SVG_NS, 'circle');
      c.setAttribute('cx', '12'); c.setAttribute('cy', '12'); c.setAttribute('r', '3');
      shapes.push(c);
    }
    shapes.forEach(function (n) { svg.appendChild(n); });
    return svg;
  }

  function setRevealed(input, btn, on) {
    input.type = on ? 'text' : 'password';
    btn.setAttribute('aria-pressed', on ? 'true' : 'false');
    btn.setAttribute('aria-label', on ? 'Hide value' : 'Show value');
    while (btn.firstChild) { btn.removeChild(btn.firstChild); }
    btn.appendChild(eyeIcon(on));
  }

  function addReveal(input) {
    var wrap = el('div', { 'class': 'pw-wrap' });
    var btn = el('button', { type: 'button', 'class': 'reveal' });
    input.parentNode.replaceChild(wrap, input);
    wrap.appendChild(input);
    wrap.appendChild(btn);
    setRevealed(input, btn, false);
    btn.addEventListener('click', function () { onRevealClick(input, btn); });
  }

  // A saved secret is only ever fetched on click, when the field still holds the mask.
  function onRevealClick(input, btn) {
    var key = input.id.indexOf('f-') === 0 ? input.id.substring(2) : '';
    if (input.type !== 'password' || !key || input.value !== MASK || initial[key] !== MASK) {
      setRevealed(input, btn, input.type === 'password');
      return;
    }
    var gen = revealGen;
    var note = input.parentNode.querySelector('.reveal-error');
    if (note) { note.parentNode.removeChild(note); }
    btn.disabled = true;
    api('POST', '/api/secret', { key: key }).then(function (r) {
      if (gen !== revealGen) { return; }
      if (r.status === 200 && typeof r.data.value === 'string') {
        input._revealed = r.data.value;
        input.value = r.data.value;
        setRevealed(input, btn, true);
      } else if (r.status !== 401) {
        showRevealError(input, errText(r, 'Could not show the value'));
      }
    }).catch(function () {
      if (gen === revealGen) { showRevealError(input, 'Could not reach the server'); }
    }).then(function () { btn.disabled = false; });
  }

  function showRevealError(input, text) {
    var p = el('p', { 'class': 'result bad reveal-error', role: 'alert', text: text });
    input.parentNode.appendChild(p);
  }

  // The value a field would be saved with: a revealed, unedited secret counts as unchanged.
  function fieldValue(f) {
    var inp = $('f-' + f.key);
    return inp._revealed !== undefined && inp.value === inp._revealed ? initial[f.key] : inp.value;
  }

  function hideAllReveals() {
    revealGen++;
    Array.prototype.forEach.call(document.querySelectorAll('.pw-wrap'), function (w) {
      var inp = w.querySelector('input');
      if (inp._revealed !== undefined && inp.value === inp._revealed) { inp.value = MASK; }
      inp._revealed = undefined;
      var note = w.querySelector('.reveal-error');
      if (note) { w.removeChild(note); }
      var b = w.querySelector('button.reveal');
      b.disabled = false;
      setRevealed(inp, b, false);
    });
  }

  function show(view) {
    hideAllReveals();
    ['loading', 'login-view', 'change-view', 'main-view'].forEach(function (v) {
      $(v).hidden = (v !== view);
    });
  }

  function stopPolling() {
    pollGen++;
    // The pending timer is left to fire: it sees the generation changed and resolves, so Save re-enables.
  }

  // Resolves {status, data}. 401 outside login returns to the login screen.
  function api(method, path, body, timeoutMs) {
    var headers = {};
    var opts = { method: method, headers: headers, credentials: 'same-origin' };
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(body);
    }
    if (method !== 'GET' && path !== '/api/login' && csrf) { headers['X-CSRF-Token'] = csrf; }
    var ctl = null;
    var timer = null;
    if (timeoutMs && typeof AbortController !== 'undefined') {
      ctl = new AbortController();
      opts.signal = ctl.signal;
      timer = setTimeout(function () { ctl.abort(); }, timeoutMs);
    }
    return fetch(path, opts).then(function (res) {
      return res.json().catch(function () { return {}; }).then(function (data) {
        if (res.status === 401 && path !== '/api/login') { toLogin(); }
        return { status: res.status, data: data || {} };
      });
    }).then(function (r) { if (timer) { clearTimeout(timer); } return r; },
            function (e) { if (timer) { clearTimeout(timer); } throw e; });
  }

  // Basic mode hides the optional cards and turns the status line links into plain text.
  // Hidden fields stay in the page, so saving never clears them.
  function applyMode() {
    Array.prototype.forEach.call(document.querySelectorAll('.adv-only'), function (n) { n.hidden = !advanced; });
    ['status', 'queue', 'role'].forEach(function (id) {
      var a = $(id + '-set');
      if (advanced) { a.setAttribute('href', '#messages-' + id); a.setAttribute('aria-haspopup', 'dialog'); a.classList.remove('plain'); }
      else { a.removeAttribute('href'); a.removeAttribute('aria-haspopup'); a.classList.add('plain'); }
    });
    if (!advanced) { closeMessages(false); }
  }

  $('advanced').addEventListener('change', function () {
    advanced = $('advanced').checked;
    try { localStorage.setItem('advancedMode', advanced ? '1' : '0'); } catch (e) { }
    applyMode();
  });

  function toLogin() {
    closeLogs();
    stopPolling();
    csrf = '';
    initial = {};
    $('save').disabled = false;
    $('logout').disabled = false;
    $('login-password').value = '';
    show('login-view');
  }

  function errText(r, fallback) {
    return (r && r.data && typeof r.data.error === 'string' && r.data.error) ? r.data.error : fallback;
  }

  function route(session) {
    if (!session.loggedIn) { toLogin(); return Promise.resolve(); }
    csrf = session.csrf || csrf;
    if (session.mustChange) { show('change-view'); return Promise.resolve(); }
    return loadMain();
  }

  function init() {
    buildSections();
    try { advanced = localStorage.getItem('advancedMode') === '1'; } catch (e) { advanced = false; }
    $('advanced').checked = advanced;
    applyMode();
    api('GET', '/api/session').then(function (r) { return route(r.data); })
      .catch(function () { $('loading').textContent = 'Could not reach the server. Reload to try again.'; });
  }

  // ---- login ----
  $('login-form').addEventListener('submit', function (ev) {
    ev.preventDefault();
    var btn = $('login-submit');
    $('login-error').textContent = '';
    btn.disabled = true;
    api('POST', '/api/login', { username: $('login-username').value, password: $('login-password').value })
      .then(function (r) {
        if (r.status === 429) { $('login-error').textContent = 'Too many attempts, wait 30 seconds'; return; }
        if (r.status !== 200) { $('login-error').textContent = errText(r, 'Sign in failed'); return; }
        csrf = r.data.csrf || '';
        $('login-password').value = '';
        return route({ loggedIn: true, mustChange: !!r.data.mustChange, csrf: csrf });
      })
      .catch(function () { $('login-error').textContent = 'Could not reach the server'; })
      .then(function () { btn.disabled = false; });
  });

  // ---- forced credential change ----
  $('change-form').addEventListener('submit', function (ev) {
    ev.preventDefault();
    var err = $('change-error');
    var user = $('change-username').value.trim();
    var pw = $('change-password').value;
    err.textContent = '';
    if (!user) { err.textContent = 'Enter a username'; return; }
    if (pw.length < 8) { err.textContent = 'Password must be at least 8 characters'; return; }
    if (pw === 'admin') { err.textContent = 'Password cannot be "admin"'; return; }
    if (pw !== $('change-repeat').value) { err.textContent = 'Passwords do not match'; return; }
    var btn = $('change-submit');
    btn.disabled = true;
    api('POST', '/api/credentials', { username: user, password: pw })
      .then(function (r) {
        if (r.status !== 200) { if (r.status !== 401) { err.textContent = errText(r, 'Could not save login'); } return; }
        csrf = r.data.csrf || csrf;
        $('change-password').value = '';
        $('change-repeat').value = '';
        return loadMain();
      })
      .catch(function () { err.textContent = 'Could not reach the server'; })
      .then(function () { btn.disabled = false; });
  });

  // ---- main page ----
  function buildSections() {
    var host = $('sections');
    SECTIONS.forEach(function (s) {
      var card = el('section', { 'class': 'card' + (s.optional ? ' adv-only' : ''), 'aria-labelledby': 'h-' + s.id });
      card.appendChild(el('h2', { id: 'h-' + s.id, text: s.title + (s.optional ? ' (optional)' : '') }));
      card.appendChild(el('p', { 'class': 'help', text: s.help }));
      s.fields.forEach(function (f) {
        card.appendChild(el('label', { 'for': 'f-' + f.key, text: f.label }));
        var attrs = { id: 'f-' + f.key, name: f.key, type: f.secret ? 'password' : 'text',
                      autocomplete: f.secret ? 'new-password' : 'off', autocapitalize: 'off', spellcheck: 'false' };
        if (f.inputmode) { attrs.inputmode = f.inputmode; }
        if (f.secret) { attrs['data-bwignore'] = 'true'; attrs['data-1p-ignore'] = 'true'; attrs['data-lpignore'] = 'true'; }
        var inp = el('input', attrs);
        card.appendChild(inp);
        if (f.secret) { addReveal(inp); }
      });
      var btn = el('button', { id: 'test-' + s.id, type: 'button', text: 'Test' });
      var res = el('p', { id: 'result-' + s.id, 'class': 'result', 'aria-live': 'polite' });
      btn.addEventListener('click', function () { runTest(s, btn, res); });
      card.appendChild(el('div', { 'class': 'test-row' }, [btn, res]));
      host.appendChild(card);
    });
    host.appendChild(buildMessagesCard());
  }

  function buildMessagesCard() {
    // A modal dialog opened by a status line link; shows one group at a time. The inputs stay in the
    // DOM (hidden groups included) so the page's Save works the same and never clears their values.
    var dlg = el('dialog', { id: 'messages', 'aria-labelledby': 'h-messages' });
    var closeBtn = el('button', { id: 'messages-close', type: 'button', 'class': 'close', 'aria-label': 'Close', text: '\u00d7' });
    closeBtn.addEventListener('click', function () { closeMessages(true); });
    dlg.addEventListener('cancel', function (ev) { ev.preventDefault(); closeMessages(true); });
    dlg.addEventListener('close', function () { if (openGroup !== null) { closeMessages(true); } });
    dlg.addEventListener('click', function (ev) { if (ev.target === dlg) { closeMessages(true); } });
    dlg.appendChild(el('div', { 'class': 'card-head' }, [
      el('h2', { id: 'h-messages', text: 'Discord message' }), closeBtn]));
    MESSAGE_GROUPS.forEach(function (g) {
      var box = el('div', { id: 'messages-' + g.id, 'class': 'group', role: 'group', 'aria-labelledby': 'h-messages', hidden: '' });
      box.appendChild(el('p', { 'class': 'help', text: g.help }));
      box.appendChild(el('label', { 'for': 'link-' + g.id, text: 'Paste message link (fills the channel and message ID below)' }));
      var link = el('input', { id: 'link-' + g.id, type: 'text', autocomplete: 'off', autocapitalize: 'off',
                               spellcheck: 'false', 'aria-describedby': 'link-error-' + g.id });
      box.appendChild(link);
      var linkErr = el('p', { id: 'link-error-' + g.id, 'class': 'error', 'aria-live': 'polite' });
      box.appendChild(linkErr);
      link.addEventListener('input', function () { applyLink(g, link, linkErr); });
      link.addEventListener('change', function () { applyLink(g, link, linkErr); });
      g.fields.forEach(function (f) {
        box.appendChild(el('label', { 'for': 'f-' + f.key, text: f.label }));
        box.appendChild(el('input', { id: 'f-' + f.key, name: f.key, type: 'text', inputmode: 'numeric',
                                      autocomplete: 'off', autocapitalize: 'off', spellcheck: 'false' }));
      });
      var btn = el('button', { id: 'test-' + g.id, type: 'button', text: 'Test' });
      var res = el('p', { id: 'result-' + g.id, 'class': 'result', 'aria-live': 'polite' });
      btn.addEventListener('click', function () { runTest(g, btn, res); });
      box.appendChild(el('div', { 'class': 'test-row' }, [btn, res]));
      dlg.appendChild(box);
    });
    var saveBtn = el('button', { id: 'messages-save', type: 'button', 'class': 'primary', text: 'Save' });
    var doneBtn = el('button', { id: 'messages-done', type: 'button', text: 'Done' });
    doneBtn.addEventListener('click', function () { closeMessages(true); });
    saveBtn.addEventListener('click', function () { saveSettings(true); });
    dlg.appendChild(el('p', { id: 'messages-save-result', 'class': 'result', 'aria-live': 'polite' }));
    dlg.appendChild(el('div', { 'class': 'dialog-actions' }, [doneBtn, saveBtn]));
    return dlg;
  }

  // Status line links open the messages dialog on one group. Hidden groups stay in the DOM so
  // saving never clears their values.
  var openGroup = null;

  function markLinks() {
    ['status', 'queue', 'role'].forEach(function (id) {
      var a = $(id + '-set');
      if (advanced) { a.setAttribute('aria-expanded', openGroup === id ? 'true' : 'false'); }
      else { a.removeAttribute('aria-expanded'); }
    });
  }

  function closeMessages(restoreFocus) {
    var opener = openGroup;
    openGroup = null;
    var dlg = $('messages');
    if (dlg && dlg.open) { dlg.close(); }
    document.documentElement.classList.remove('modal-open');
    MESSAGE_GROUPS.forEach(function (g) { var b = $('messages-' + g.id); if (b) { b.hidden = true; } });
    if ($('messages-save-result')) { setResult($('messages-save-result'), null, ''); }
    markLinks();
    if (restoreFocus && opener && $(opener + '-set')) { $(opener + '-set').focus(); }
  }

  function goToGroup(id) {
    var g = MESSAGE_GROUPS.filter(function (x) { return x.id === id; })[0];
    if (!g || !$('messages-' + id)) { return; }
    MESSAGE_GROUPS.forEach(function (x) { $('messages-' + x.id).hidden = x.id !== id; });
    if (logOpen) { closeLogs(); }
    openGroup = id;
    $('h-messages').textContent = g.title;
    setResult($('messages-save-result'), null, '');
    markLinks();
    document.documentElement.classList.add('modal-open');
    var dlg = $('messages');
    if (!dlg.open) { if (dlg.showModal) { dlg.showModal(); } else { dlg.setAttribute('open', ''); } }
    dlg.scrollTop = 0;
    $('f-' + g.fields[0].key).focus();
  }

  ['status', 'queue', 'role'].forEach(function (id) {
    $(id + '-set').addEventListener('click', function (ev) {
      ev.preventDefault();
      if (advanced) { goToGroup(id); }
    });
  });

  // Fills the group's channel and message IDs from a pasted message link. Digits only, text only.
  function applyLink(g, link, err) {
    var v = link.value.trim();
    if (!v) { err.textContent = ''; return; }
    var m = LINK_RE.exec(v);
    if (!m) { err.textContent = 'Not a Discord message link'; return; }
    $('f-' + g.channelKey).value = m[2];
    $('f-' + g.messageKey).value = m[3];
    link.value = '';
    err.textContent = '';
  }

  function loadMain() {
    return Promise.all([api('GET', '/api/settings'), api('GET', '/api/status')]).then(function (rs) {
      if (rs[0].status === 401 || rs[1].status === 401) { return; }
      fillSettings(rs[0].data);
      renderStatus(rs[1].data);
      show('main-view');
    });
  }

  function fillSettings(data) {
    initial = {};
    hideAllReveals();
    allFields().forEach(function (f) {
      var v = data[f.key] == null ? '' : String(data[f.key]);
      initial[f.key] = v;
      $('f-' + f.key).value = v;
    });
    var w = $('warning');
    if (typeof data.warning === 'string' && data.warning) { w.textContent = data.warning; w.hidden = false; }
    else { w.textContent = ''; w.hidden = true; }
  }

  function setItem(id, label, isSet) {
    var el = $(id);
    el.textContent = '';
    if (!isSet) { el.textContent = label + ': not set'; return; }
    el.appendChild(document.createTextNode(label));
    var tick = document.createElement('span');
    tick.className = 'tick';
    tick.setAttribute('aria-hidden', 'true');
    tick.textContent = ' \u2713';
    el.appendChild(tick);
    var sr = document.createElement('span');
    sr.className = 'sr-only';
    sr.textContent = ': set';
    el.appendChild(sr);
  }

  function renderStatus(d) {
    var label = STATE_LABELS[d.botState] || String(d.botState || 'unknown').toLowerCase();
    $('bot-state').textContent = 'Bot: ' + label;
    setItem('status-set', 'Status message', d.statusSet);
    setItem('queue-set', 'Queue message', d.queueSet);
    setItem('role-set', 'Role message', d.roleSet);
  }

  function setResult(node, ok, msg) {
    node.className = 'result' + (ok === null ? '' : ok ? ' ok' : ' bad');
    node.textContent = msg;
  }

  function runTest(s, btn, res) {
    var body = {};
    s.fields.forEach(function (f) { body[f.key] = $('f-' + f.key).value; });
    btn.disabled = true;
    btn.textContent = 'Testing...';
    setResult(res, null, '');
    api('POST', '/api/test/' + s.id, body, 12000)
      .then(function (r) {
        if (r.status === 401) { return; }
        if (r.status !== 200) { setResult(res, false, errText(r, 'Test failed')); return; }
        setResult(res, !!r.data.ok, String(r.data.message == null ? '' : r.data.message));
      })
      .catch(function (e) {
        setResult(res, false, e && e.name === 'AbortError' ? 'Timed out after 12 seconds' : 'Could not reach the server');
      })
      .then(function () { btn.disabled = false; btn.textContent = 'Test'; });
  }

  $('settings-form').addEventListener('submit', function (ev) {
    ev.preventDefault();
    saveSettings(false);
  });

  // fromDialog: the Save button inside the messages dialog; the dialog closes once the save is accepted.
  function saveSettings(fromDialog) {
    var btn = $('save');
    var dlgBtn = $('messages-save');
    var out = fromDialog ? $('messages-save-result') : $('save-result');
    var body = {};
    // Only fields the user changed are sent (absent = keep the saved value, "" = clear it). This is what
    // stops a page that loaded before a /setup command ran from overwriting the IDs that command saved.
    allFields().forEach(function (f) {
      var v = fieldValue(f);
      if (v !== initial[f.key]) { body[f.key] = v; }
    });
    btn.disabled = true;
    dlgBtn.disabled = true;
    setResult(out, null, 'Saving...');
    stopPolling();
    api('PUT', '/api/settings', body)
      .then(function (r) {
        if (r.status === 401) { return; }
        if (r.status !== 200) { setResult(out, false, errText(r, 'Save failed')); return; }
        if (fromDialog) { closeMessages(true); out = $('save-result'); }
        setResult(out, null, 'Saved. Starting the bot...');
        return api('GET', '/api/settings').then(function (s) {
          if (s.status === 200) { fillSettings(s.data); }
          return pollStatus(out, Date.now(), pollGen);
        });
      })
      .catch(function () { setResult(out, false, 'Could not reach the server'); })
      .then(function () { btn.disabled = false; dlgBtn.disabled = false; });
  }

  function pollStatus(out, startedAt, gen) {
    return api('GET', '/api/status').then(function (r) {
      if (gen !== pollGen || r.status !== 200) { return; }
      renderStatus(r.data);
      if (r.data.botState !== 'STARTING') {
        var ok = r.data.botState === 'RUNNING';
        var label = STATE_LABELS[r.data.botState] || String(r.data.botState).toLowerCase();
        setResult(out, r.data.botState === 'FAILED' ? false : ok ? true : null,
          'Saved. Bot is ' + label + '.');
        return;
      }
      if (Date.now() - startedAt >= 30000) {
        setResult(out, false, 'Saved, but the bot is still starting after 30 seconds.');
        return;
      }
      return new Promise(function (resolve) {
        setTimeout(function () {
          resolve(gen === pollGen ? pollStatus(out, startedAt, gen) : undefined);
        }, 2000);
      });
    });
  }

  // ---- logs ----
  function trimLog(text) {
    if (text.length > LOG_MAX_CHARS) { text = text.slice(text.length - LOG_MAX_CHARS); text = text.slice(text.indexOf('\n') + 1); }
    var lines = text.split('\n');
    if (lines.length > LOG_MAX_LINES + 1) { text = lines.slice(lines.length - LOG_MAX_LINES - 1).join('\n'); }
    return text;
  }

  function showLog(text, reset) {
    var box = $('logs-text');
    var follow = box.scrollTop + box.clientHeight >= box.scrollHeight - 24;
    logBuf = trimLog(reset ? text : logBuf + text);
    box.textContent = logBuf;
    if (follow || reset) { box.scrollTop = box.scrollHeight; }
  }

  function scheduleLogs(gen, ms) {
    clearTimeout(logTimer);
    logTimer = setTimeout(function () { pollLogs(gen); }, ms);
  }

  function pollLogs(gen) {
    if (gen !== logGen || !logOpen || logPaused || document.hidden) { return; }
    api('GET', '/api/logs?offset=' + logOffset).then(function (r) {
      if (gen !== logGen) { return; }
      if (r.status === 200) {
        if (typeof r.data.nextOffset === 'number') { logOffset = r.data.nextOffset; }
        if (r.data.reset || r.data.text) { showLog(String(r.data.text || ''), !!r.data.reset); }
        $('logs-state').textContent = logBuf ? '' : 'No log output yet';
      } else if (r.status !== 401) {
        $('logs-state').textContent = 'Could not load the log';
      }
    }).catch(function () {
      if (gen === logGen) { $('logs-state').textContent = 'Could not reach the server'; }
    }).then(function () { if (gen === logGen) { scheduleLogs(gen, 2000); } });
  }

  function startLogs() {
    logGen++;
    pollLogs(logGen);
  }

  function closeLogs() {
    logOpen = false;
    logGen++;
    clearTimeout(logTimer);
    logOffset = -1;
    logBuf = '';
    $('logs-text').textContent = '';
    $('logs-view').hidden = true;
    $('settings-form').hidden = false;
    $('footer').hidden = false;
    $('logs-toggle').textContent = 'Logs';
    $('logs-toggle').setAttribute('aria-expanded', 'false');
  }

  $('logs-toggle').addEventListener('click', function () {
    if (logOpen) { closeLogs(); return; }
    logOpen = true;
    logPaused = false;
    $('logs-pause').textContent = 'Pause';
    $('logs-view').hidden = false;
    $('settings-form').hidden = true;
    $('footer').hidden = true;
    $('logs-toggle').textContent = 'Settings';
    $('logs-toggle').setAttribute('aria-expanded', 'true');
    startLogs();
  });

  $('logs-pause').addEventListener('click', function () {
    logPaused = !logPaused;
    $('logs-pause').textContent = logPaused ? 'Resume' : 'Pause';
    $('logs-state').textContent = logPaused ? 'Paused' : '';
    if (logPaused) { clearTimeout(logTimer); logGen++; } else { startLogs(); }
  });

  document.addEventListener('visibilitychange', function () {
    if (!logOpen || logPaused) { return; }
    if (document.hidden) { clearTimeout(logTimer); logGen++; } else { startLogs(); }
  });

  $('logout').addEventListener('click', function () {
    var btn = $('logout');
    btn.disabled = true;
    api('POST', '/api/logout', {}).catch(function () { }).then(function () {
      btn.disabled = false;
      toLogin();
    });
  });

  ['login-password', 'change-password', 'change-repeat'].forEach(function (id) { addReveal($(id)); });
  init();
})();
