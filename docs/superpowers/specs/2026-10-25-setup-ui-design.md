# Setup UI for PlexInformationBot: design

## Intent and why

PlexInformationBot is configured today through 15 environment variables, several of which are Discord IDs that must be copied by hand with developer mode on. That is fine for the author's Unraid box and a wall for anyone else. The author plans to release the bot to others.

Goal: a stranger starts one container, opens a page in a browser, fills in their connection details, clicks Test on each section until it is green, runs a few slash commands in Discord, and never touches environment variables again. The model is requestrr: set up once, never again.

## Goals

1. A mobile-friendly web page, served by the bot itself on its own port, to enter and save: Discord bot token, Plex IP, port and token, and optionally Tautulli, Radarr and Sonarr.
2. A Test button per section that checks the connection with the values currently in the form, and shows a clear pass or fail with the reason.
3. Discord-side setup by slash commands, so nobody copies a channel or message ID:
   - `/setupreact` posts the role message ("React to this message to get your roles!") in the current channel. The command takes the role to hand out as a role option.
   - `/setupstatus` posts the live activity message in the current channel (guess, see below).
   - `/setupcontentqueue` posts the download queue message in the current channel.
   Each command records the channel ID and the new message ID in the saved settings. The existing updaters then edit that message.
4. A login: basic form login, `admin` / `admin` until changed (see Login).
5. Settings persist in a file in a mounted folder and survive restarts and image upgrades.
6. Existing deployments keep working: the same 15 environment variables are still read. Anything set in the settings file wins over the environment.

## Non-goals (first release)

- Multiple accounts or roles on the web page.
- HTTPS. People who want it put the bot behind their own reverse proxy.
- Translating the web page or the bot into other languages.
- Replacing the bot's behaviour (embeds, quality profiles, role reaction logic). This is a configuration layer only.

## Anti-goals (a failure even if it technically works)

- Breaking the current Unraid and Docker setups that use environment variables.
- Any secret appearing in full in the page after it is saved, in logs, or in an error shown to the user. (The javaplex library currently logs every Plex request URL including the Plex token; that must be quieted as part of this work.)
- Storing the password in plain text.
- A setup that needs the bot to already be logged in to Discord before the page loads. The page must be reachable on a fresh install with no token.

## Constraints

- Existing stack: Java 17, Gradle (Kotlin DSL), Javacord 3.8.0, javaplex 6.1.0 (from JitPack), Gson, Log4j2. Built as a fat jar and run as a container.
- One program, one container, one extra port. No separate frontend build step.
- `Main` holds the configuration today as `public static String` fields read through static imports across `Application`, the workers and the listeners. The settings layer must replace those reads.
- Mobile-friendly page: usable at phone width.

## Decisions made with the author

- The page is served from inside the bot jar.
- Discord-side IDs come from slash commands the bot answers, not from the page.
- The role handed out by `/setupreact` is a command option (guess, confirmed as reasonable by the author).
- Settings, including saved channel and message IDs, live in one config file in a mounted folder.
- Slash commands are limited to server administrators.
- Re-running a command moves that message to the new channel and leaves the old message in place.
- The page shows which parts are set up, such as "queue message: set" (the author's idea was not asked for; included as a small status line).
- Login: a form login. A local credentials file holds the account. If the file does not exist, `admin` / `admin` is accepted. The first successful login forces a new username and password, which creates the file. Deleting the file restores `admin` / `admin`.
- "Whatever is good practice" for the rest of login (author's words): salted, slow password hash (bcrypt or PBKDF2); random session id in an HttpOnly, SameSite cookie; a CSRF token on every state-changing form or request; failed logins slowed down per client.

## Calls made by the designer (not yet agreed by the author)

- Default web port 4546, changeable by a `WEB_PORT` environment variable. Config folder `/config` by default, changeable by `CONFIG_DIR`.
- Settings file format is JSON (`settings.json`); credentials are a separate file in the same folder.
- The web server is the JDK's built-in `com.sun.net.httpserver` or a small embedded library such as Javalin, whichever the builder judges simpler to keep dependency-light. The page is static HTML and vanilla JavaScript.
- A "Save" on the page applies the new settings and (re)starts the Discord connection and the update tasks without restarting the container.
- `/setupstatus` exists for the live activity message, since it has the same channel and message ID problem.
- The Test buttons do the following:
  - Discord: log in with the entered token and report the bot name and the servers it is in.
  - Plex: connect to the server and report its friendly name.
  - Tautulli, Radarr, Sonarr: one status call with the entered URL and key.
  - Queue and status: a "post or refresh now" action that confirms the saved message can be edited.

## Where each existing setting goes

All 15 current environment variables are covered; there are no other settings (no colours, intervals or guild ID).

| Env var | Where it is set in the new design |
|---|---|
| `BOT_TOKEN`, `IP`, `PORT`, `PLEX_KEY` | Web page, Discord and Plex sections |
| `TAUTULLI_URL`, `API_KEY` | Web page, Tautulli section (optional) |
| `RADARR_URL`, `RADARR_API`, `SONARR_URL`, `SONARR_API` | Web page, Radarr and Sonarr sections (optional) |
| `ROLE_ID`, and the role message | `/setupreact` (role option) |
| `TEXT_CHANNELID`, `MESSAGEID` | `/setupstatus` |
| `QUEUE_TEXT_CHANNELID`, `QUEUEMESSAGEID` | `/setupcontentqueue` |

## More calls made by the designer (from the builder check; not yet agreed by the author)

- Precedence is per field. A field set in the settings file wins; a field absent from the file falls back to the environment variable. An empty field in the form means "unset", so it falls back to the environment.
- Secrets are never sent to the page in full. The page receives a placeholder; if a saved secret field comes back untouched, the old value is kept. Typing a new value replaces it.
- Role reactions: the listener accepts the role message by saved message ID, and also still by the existing bot-author plus text match, so deployments set up the old way keep working. The emoji stays the clapperboard.
- Slash commands: `/activestreams` stays global. The three setup commands are registered per server at startup and when the bot joins a server (so they appear immediately), with admin-only default permissions. Nothing is registered until a Discord token is saved and the bot has logged in.
- The session cookie is `Secure` only when the request arrived with `X-Forwarded-Proto: https`. Login throttling uses the connection's remote address; `X-Forwarded-For` is not trusted.
- Testing a Discord token that differs from the live one uses a short-lived second connection that is closed straight after; if it matches the live token, the Test reports the live bot's details without a new connection.

## Left to the builder

- Package layout, naming and how the settings object replaces the static fields in `Main`.
- The exact HTTP routes, JSON shapes and page layout.
- Test strategy. The settings store, the password handling and the Test checks should be unit-testable without a live Discord, Plex or Radarr.
- How to unregister and re-register slash commands, and keeping `/activestreams` working.
- Dockerfile, volume and port changes, and an updated README with an Unraid template note. (The current `dockerfile` does not build on Apple Silicon because the Gradle Alpine base image has no ARM variant; fixing that is welcome but not required.)

## Known facts about the current code

- `Application` builds the Discord client, creates a Plex client from `IP`, `PORT` and `PLEX_KEY`, and runs three scheduled tasks: activity text (15s), status embed (5s, edits only on change), and queue embed (120s).
- `ReactListener` finds the role message by the bot being the author and the message text matching `Main.DISCORD_MESSAGE`; it takes the role ID in its constructor. `RoleListener` reads `ROLE_ID` from `Main`.
- Slash commands are registered globally in `SlashCommandsSetUp`; the only one today is `activestreams`.
- All of this exists on `master` as of the merge of PR 3.
