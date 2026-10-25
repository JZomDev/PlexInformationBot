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
  var MESSAGE_INTRO = 'Normally the /setupstatus, /setupcontentqueue and /setupreact commands fill these in. '
    + 'To keep using an existing message the bot already wrote, paste its IDs here. '
    + 'In Discord turn on Developer Mode (User Settings, Advanced), right-click the message and choose '
    + 'Copy Message Link. The link ends with .../channelId/messageId. '
    + 'The role ID is under Server Settings, Roles: right-click the role and choose Copy Role ID.';
  var MESSAGE_GROUPS = [
    { id: 'status', title: 'Status message', channelKey: 'statusChannelId', messageKey: 'statusMessageId',
      help: 'The live activity message the bot keeps editing. It must have been written by this bot.',
      fields: [{ key: 'statusChannelId', label: 'Status channel ID', inputmode: 'numeric' },
               { key: 'statusMessageId', label: 'Status message ID', inputmode: 'numeric' }] },
    { id: 'queue', title: 'Download queue message', channelKey: 'queueChannelId', messageKey: 'queueMessageId',
      help: 'The download queue message the bot keeps editing. It must have been written by this bot.',
      fields: [{ key: 'queueChannelId', label: 'Queue channel ID', inputmode: 'numeric' },
               { key: 'queueMessageId', label: 'Queue message ID', inputmode: 'numeric' }] },
    { id: 'role', title: 'Role message', channelKey: 'roleChannelId', messageKey: 'roleMessageId',
      help: 'The message people react to for a role. It must have been written by this bot.',
      fields: [{ key: 'roleId', label: 'Role ID', inputmode: 'numeric' },
               { key: 'roleChannelId', label: 'Role channel ID', inputmode: 'numeric' },
               { key: 'roleMessageId', label: 'Role message ID', inputmode: 'numeric' }] }
  ];
  var LINK_RE = /^https:\/\/(?:www\.|ptb\.|canary\.)?discord(?:app)?\.com\/channels\/(\d+)\/(\d+)\/(\d+)$/;
  var STATE_LABELS = { NOT_CONFIGURED: 'not configured', STARTING: 'starting', RUNNING: 'running', FAILED: 'failed' };

  var csrf = '';
  var initial = {};
  var pollGen = 0;

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

  function show(view) {
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

  function toLogin() {
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
      var card = el('section', { 'class': 'card', 'aria-labelledby': 'h-' + s.id });
      card.appendChild(el('h2', { id: 'h-' + s.id, text: s.title + (s.optional ? ' (optional)' : '') }));
      card.appendChild(el('p', { 'class': 'help', text: s.help }));
      s.fields.forEach(function (f) {
        card.appendChild(el('label', { 'for': 'f-' + f.key, text: f.label }));
        var attrs = { id: 'f-' + f.key, name: f.key, type: f.secret ? 'password' : 'text',
                      autocomplete: f.secret ? 'new-password' : 'off', autocapitalize: 'off', spellcheck: 'false' };
        if (f.inputmode) { attrs.inputmode = f.inputmode; }
        card.appendChild(el('input', attrs));
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
    var card = el('section', { 'class': 'card', 'aria-labelledby': 'h-messages' });
    card.appendChild(el('h2', { id: 'h-messages', text: 'Discord messages (optional)' }));
    card.appendChild(el('p', { 'class': 'help', text: MESSAGE_INTRO }));
    MESSAGE_GROUPS.forEach(function (g) {
      var box = el('div', { 'class': 'group', role: 'group', 'aria-labelledby': 'h-' + g.id });
      box.appendChild(el('h3', { id: 'h-' + g.id, text: g.title }));
      box.appendChild(el('p', { 'class': 'help', text: g.help }));
      box.appendChild(el('label', { 'for': 'link-' + g.id, text: 'Paste message link' }));
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
      card.appendChild(box);
    });
    return card;
  }

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
    allFields().forEach(function (f) {
      var v = data[f.key] == null ? '' : String(data[f.key]);
      initial[f.key] = v;
      $('f-' + f.key).value = v;
    });
    var w = $('warning');
    if (typeof data.warning === 'string' && data.warning) { w.textContent = data.warning; w.hidden = false; }
    else { w.textContent = ''; w.hidden = true; }
  }

  function renderStatus(d) {
    var label = STATE_LABELS[d.botState] || String(d.botState || 'unknown').toLowerCase();
    $('bot-state').textContent = 'Bot: ' + label;
    $('status-set').textContent = 'Status message: ' + (d.statusSet ? 'set' : 'not set');
    $('queue-set').textContent = 'Queue message: ' + (d.queueSet ? 'set' : 'not set');
    $('role-set').textContent = 'Role message: ' + (d.roleSet ? 'set' : 'not set');
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
    var btn = $('save');
    var out = $('save-result');
    var body = {};
    // Only fields the user changed are sent (absent = keep the saved value, "" = clear it). This is what
    // stops a page that loaded before a /setup command ran from overwriting the IDs that command saved.
    allFields().forEach(function (f) {
      var v = $('f-' + f.key).value;
      if (v !== initial[f.key]) { body[f.key] = v; }
    });
    btn.disabled = true;
    setResult(out, null, 'Saving...');
    stopPolling();
    api('PUT', '/api/settings', body)
      .then(function (r) {
        if (r.status === 401) { return; }
        if (r.status !== 200) { setResult(out, false, errText(r, 'Save failed')); return; }
        setResult(out, null, 'Saved. Starting the bot...');
        return api('GET', '/api/settings').then(function (s) {
          if (s.status === 200) { fillSettings(s.data); }
          return pollStatus(out, Date.now(), pollGen);
        });
      })
      .catch(function () { setResult(out, false, 'Could not reach the server'); })
      .then(function () { btn.disabled = false; });
  });

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

  $('logout').addEventListener('click', function () {
    var btn = $('logout');
    btn.disabled = true;
    api('POST', '/api/logout', {}).catch(function () { }).then(function () {
      btn.disabled = false;
      toLogin();
    });
  });

  init();
})();
