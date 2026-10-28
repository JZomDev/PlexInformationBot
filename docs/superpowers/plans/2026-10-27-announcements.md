# Announcements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Post one silent Discord card per new Radarr movie and per completed Sonarr season, once each, in a channel set by `/setupannouncements`.

**Architecture:** A pure `Announcer` runs one poll cycle against two narrow ports (`ArrSource` for Radarr/Sonarr data, `CardSink` for posting) and an `AnnouncedStore` (file-backed). Baseline, de-dup, cap and backoff live in `Announcer` and are unit-tested with fakes. `Application` schedules a poll alongside its existing tasks. Three new string settings carry the channel and two switches.

**Tech Stack:** Java 17, Gradle, Javacord 3.8.0, Gson, JUnit Jupiter 5.10.2, `java.net.http.HttpClient`. No new dependency.

**Spec:** `docs/superpowers/specs/2026-10-27-announcements-design.md`

**Gate (run by the orchestrator, not trusted from builders):**
`docker run --rm -v "$PWD":/w -w /w -v plexbot-gradle:/root/.gradle eclipse-temurin:17-jdk sh gradlew test --no-daemon -q ; echo "exit=$?"`

## Global Constraints

- Polling only, every 1 to 2 minutes (use 90 s). No webhooks.
- Posts are silent: `AllowedMentions` empty, no role or user mentions.
- At most 5 cards per poll cycle in total (movies and seasons together), oldest first.
- Movies keyed by TMDB id; seasons by TVDB id plus season number. Season 0 never announced.
- Season complete = `statistics.episodeCount > 0`, `episodeFileCount >= episodeCount`, and no `statistics.nextAiring`.
- Existing deployments: a missing channel or switch means off. `/setupannouncements` sets the channel and both switches to `"true"`.
- No API key in cards, logs or errors. Never post a card with a blank title or poster URL; skip and retry next cycle instead.
- Settings fields stay strings. Commit messages: one short conventional line, no body, no attribution. Do not touch gate config or `.github/`.
- Files stay well under 1,000 lines; `Application.java` must not grow beyond wiring.

## Review Focus

1. A bulk import of 30 movies at once: five cards per cycle, no duplicates across cycles, order oldest first.
2. Restart mid-cycle or lost `announced.json`: no flood (silent re-baseline, one warning log).
3. Radarr or Sonarr down or returning garbage: no partial card, no crash of the poll task, health dot shows it.
4. Discord post failure (channel deleted, no permission): item retried with backoff, other items not blocked, log once per cause.
5. A running season (episodes 1-5 have files, later ones unaired or `nextAiring` set): never announced as complete.

---

### Task 1: Settings fields and validation

**Files:**
- Modify: `src/main/java/org/example/config/Settings.java`, `src/main/java/org/example/listeners/SetupCommandsListener.java` (positional constructor calls in `apply`), `src/main/java/org/example/web/HealthMonitor.java` only if a positional constructor appears there
- Modify (tests, positional constructors): `SettingsStoreTest`, `SettingsIdsTest`, `MainTest`, `AppConfigTest`, `RestartSerialisationTest`, `HealthMonitorTest`, `SetupCommandsTest`
- Test: `src/test/java/org/example/config/SettingsAnnounceTest.java`

**Interfaces:**
- Produces: three new trailing record components `announceChannelId`, `announceMoviesOn`, `announceSeasonsOn` (all `String`, null becomes `""`); `boolean announceMovies()` and `boolean announceSeasons()` return true only when the field equals `"true"` and `announceChannelId` is non-blank; `announceChannelId` is checked by `invalidIdField()` and trimmed by `trimmedIds()`; no env var names.

- [ ] **Step 1: Write `SettingsAnnounceTest`**: missing fields deserialise to `""`; `announceMovies()` is false with a channel but switch `""`; true with channel and `"true"`; false with switch `"true"` and no channel; a non-numeric `announceChannelId` is reported by `invalidIdField()`; `masked()` and `applyForm` leave the switches untouched (not secret).
- [ ] **Step 2: Run the gate**; expect compile failure.
- [ ] **Step 3: Add the three components** to the record, the null-to-empty constructor, `empty()`, `overlay()` (file value wins), `invalidIdField()`, `trimmedIds()`; update every positional call site listed above, appending `""` values.
- [ ] **Step 4: Run the gate**; expect `exit=0`.
- [ ] **Step 5: Commit** `feat: added announcement settings fields`

### Task 2: AnnouncedStore

**Files:**
- Create: `src/main/java/org/example/announce/AnnouncedStore.java`
- Test: `src/test/java/org/example/announce/AnnouncedStoreTest.java`

**Interfaces:**
- Produces: `AnnouncedStore(Path dir)`; `boolean baselined(String type)`; `void markBaselined(String type)`; `boolean seen(String type, String key)`; `void markSeen(String type, String key)`; `boolean wasLost()` (true if the file was unreadable or corrupt at load, as opposed to simply absent on first run); persists via `OwnerOnlyFiles.writeAtomically` to `announced.json`. Types are `"movie"` and `"season"`; keys are `String`.

- [ ] **Step 1: Write tests** (`@TempDir`): marks survive a reload; a fresh dir reports not baselined and `wasLost()` false; a corrupt file reports not baselined and `wasLost()` true and does not throw; markSeen is idempotent.
- [ ] **Step 2: Run the gate**; expect failure.
- [ ] **Step 3: Implement** with Gson, synchronised methods, write on every mark.
- [ ] **Step 4: Run the gate**; expect `exit=0`.
- [ ] **Step 5: Commit** `feat: added announced store`

### Task 3: Announcer (poll cycle logic)

**Files:**
- Create: `src/main/java/org/example/announce/{ArrSource.java,CardSink.java,Item.java,Announcer.java}`
- Test: `src/test/java/org/example/announce/AnnouncerTest.java`

**Interfaces:**
- Consumes: `AnnouncedStore` (Task 2).
- Produces:
  - `record Item(String type, String key, String title, int year, String summary, String details, String posterUrl, Instant added)` where `details` is the pre-built details line.
  - `interface ArrSource { List<Item> movies() throws IOException; List<Item> seasons() throws IOException; }` returning only items that currently qualify (movie has a file; season complete under the Global Constraints).
  - `interface CardSink { boolean post(Item item); }` returning false on failure.
  - `Announcer(ArrSource source, CardSink sink, AnnouncedStore store, Clock clock)` with `Result runCycle(boolean moviesOn, boolean seasonsOn, boolean channelSet)`; `record Result(int posted, boolean sourceFailed, boolean sinkFailed)`.
- Behaviour pinned by tests: (a) the first cycle with a type on and a channel set marks all current items seen, posts nothing, marks the type baselined; (b) off types still mark new items seen without posting; (c) later arrivals post once each, oldest `added` first, max 5 per cycle across both types; (d) a failed post leaves the item unseen, retries after backoff (30 s doubling to 15 min, per item, tracked by the injected `Clock`), and does not block other items; (e) `ArrSource` throwing sets `sourceFailed`, posts nothing, never throws; (f) blank title or blank `posterUrl` items are skipped and not marked seen; (g) when `store.wasLost()` is true on first cycle a warning is logged once and a silent re-baseline runs; (h) `channelSet` false posts and baselines nothing.

- [ ] **Step 1: Write `AnnouncerTest`** with an in-memory fake `ArrSource`, fake `CardSink`, fixed mutable `Clock`, `@TempDir` store, covering (a) to (h) plus Review Focus 1 (30 new movies over several cycles, no duplicates, oldest first).
- [ ] **Step 2: Run the gate**; expect failure.
- [ ] **Step 3: Implement `Announcer`** per the pinned behaviour; log failures once per cause (a repeated identical cause is not logged again until it clears).
- [ ] **Step 4: Run the gate**; expect `exit=0`.
- [ ] **Step 5: Commit** `feat: added announcer poll logic`

### Task 4: Radarr and Sonarr source

**Files:**
- Create: `src/main/java/org/example/announce/HttpArrSource.java`
- Test: `src/test/java/org/example/announce/HttpArrSourceTest.java`

**Interfaces:**
- Consumes: `ArrSource`, `Item` (Task 3); settings accessors for Radarr/Sonarr URL and key via a `Supplier<Settings>`.
- Produces: `HttpArrSource(Supplier<Settings> settings, HttpClient client)` implementing `ArrSource`. Uses `GET http://<host>/api/v3/movie` and `/api/v3/series` with `X-Api-Key`, 5 s connect and 10 s request timeouts. A blank URL or key for a service yields an empty list for that service. Non-2xx or bad JSON throws `IOException` with no key or URL query in the message.
- Mapping: movie qualifies when `hasFile`; key `tmdbId`; poster = first `images` entry with `coverType` `"poster"` and its `remoteUrl`; `added` from `added`; details `"<genres joined by ', '> · <runtime>m · <movieFile.quality.quality.name>"` with absent parts omitted. Season qualifies per the Global Constraints, `seasonNumber > 0`; key `tvdbId + ":" + seasonNumber`; title `"<series title> · Season N"`; details `"<genres> · <episodeFileCount> episodes"`; `added` from the series `added` (builder: if Sonarr exposes a better season import time cheaply, use it and note it in the report).

- [ ] **Step 1: Write tests** using a fake `com.sun.net.httpserver.HttpServer` (pattern in `ConnectionChecksTest`) serving canned `/api/v3/movie` and `/api/v3/series` JSON: movie with file is returned and one without is not; running season (`nextAiring` present, or `episodeFileCount < episodeCount`) is not returned; season 0 not returned; missing poster yields a blank `posterUrl` (the Announcer skips it); 401 throws `IOException` whose message contains neither key nor URL; blank settings return empty lists.
- [ ] **Step 2: Run the gate**; expect failure.
- [ ] **Step 3: Implement** with `HttpClient` and Gson.
- [ ] **Step 4: Run the gate**; expect `exit=0`.
- [ ] **Step 5: Commit** `feat: added radarr and sonarr announce source`

### Task 5: Card and Discord sink, scheduling, health

**Files:**
- Create: `src/main/java/org/example/announce/{DiscordCardSink.java,AnnouncementService.java}`
- Modify: `src/main/java/org/example/Application.java` (wiring only), `src/main/java/org/example/web/HealthMonitor.java`
- Test: `src/test/java/org/example/announce/DiscordCardSinkTest.java`, `AnnouncementServiceTest.java`

**Interfaces:**
- Consumes: `Announcer`, `AnnouncedStore`, `HttpArrSource` (Tasks 2-4); `AppConfig.get()`; `DiscordApi` from the connect-time `api`.
- Produces:
  - `static EmbedBuilder DiscordCardSink.embed(Item item)`: colour `0xE5A00D`, author line "New movie on Plex" or "Season complete on Plex", title = item title with year for movies, description = summary (truncated to 300 characters), details line as footer text, `posterUrl` as thumbnail; no mentions. `boolean post(Item)` sends it to the channel in `AppConfig.get().announceChannelId()` with empty `AllowedMentions` and returns false on any exception.
  - `AnnouncementService(Announcer announcer, Supplier<Settings> settings)` with `void tick()` that calls `runCycle(settings.announceMovies(), settings.announceSeasons(), channel non-blank)`, catches everything, and exposes `Optional<String> lastProblem()`.
  - `Application` schedules `tick()` every 90 s (delay 0) on the scheduler created in `launchScheduledExecutor`, creating the service once per connection. It is the only change there.
  - `HealthMonitor` marks Radarr or Sonarr "down" with message "Announcements cannot reach this service" only when the announcer's last cycle reported `sourceFailed` for it and announcements are on; otherwise unchanged. (Builder: pass a `Supplier<Optional<String>>` into `HealthMonitor` rather than coupling it to `Application`.)
- Review Focus 3 and 4 are exercised here: embed has no mentions and no key; sink failure returns false rather than throwing.

- [ ] **Step 1: Write tests**: `embed()` for a movie and a season (title, author line, footer, thumbnail, description truncation); no `@` mention text can escape (summary containing `@everyone` is rendered inert, assert `AllowedMentions` is empty on the built message options); `tick()` with channel blank does nothing and does not throw; `tick()` swallows an exception from the source.
- [ ] **Step 2: Run the gate**; expect failure.
- [ ] **Step 3: Implement**; wire into `Application`; extend `HealthMonitor` with the supplier.
- [ ] **Step 4: Run the gate**; expect `exit=0`.
- [ ] **Step 5: Commit** `feat: added announcement scheduling and cards`

### Task 6: Slash command, setup page, README

**Files:**
- Modify: `src/main/java/org/example/SlashCommandsSetUp.java`, `src/main/java/org/example/listeners/SetupCommandsListener.java`, `src/main/resources/web/{index.html,app.js,style.css}`, `README.md`
- Test: `src/test/java/org/example/listeners/SetupCommandsTest.java` (extend), `src/test/java/org/example/web/SetupPageTest.java` (extend)

**Interfaces:**
- Produces: command `setupannouncements` (admin only, per server, no options, description "Send announcements here"), handled in `SetupCommandsListener` with the same admin check, ephemeral reply and `store.update(...)` + `AppConfig.reload()` as the others, but posting no message: `apply(...)` for it sets `announceChannelId` to the current channel and both switches to `"true"`. Reply "Done. Announcements will post in this channel." Re-running moves the channel.
- Page: a "Announce new movies" switch inside the Radarr section and "Announce completed seasons" inside the Sonarr section (reusing the `.switch` style), sending `announceMoviesOn` and `announceSeasonsOn` as `"true"` or `""`; a status line "Announcements: channel set" with the green tick pattern, or "not set" with the hint "run /setupannouncements in a channel", shown in the Discord messages dialog group list; README gains a short Announcements section (commands, switches, polling interval, one-channel note).
- Existing `/setupreact`, `/setupstatus`, `/setupcontentqueue` behaviour and tests are unchanged.

- [ ] **Step 1: Write tests**: `apply` for `setupannouncements` sets the three fields and nothing else; the page HTML mentions `/setupannouncements` and contains both switch ids; a non-admin path is covered by extending the existing admin test where one exists, otherwise note it as unverified.
- [ ] **Step 2: Run the gate**; expect failure.
- [ ] **Step 3: Implement** the command definition, the listener branch, the page switches and status line, and the README section.
- [ ] **Step 4: Run the gate**; expect `exit=0`. Then orchestrator-only: a throwaway container check on port 4547 with a scratch config dir (never the user's config), confirming the switches save and the status line renders. The user does not want the bot run locally against their real setup.
- [ ] **Step 5: Commit** `feat: added setupannouncements command and page switches`
