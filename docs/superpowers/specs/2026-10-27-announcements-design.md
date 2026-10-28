# New-content announcements: design

## Intent and why

People in the author's Plex Discord have no way to learn that something new is ready to watch. The bot already knows Radarr and Sonarr and already posts live messages. This adds a channel feed: when a movie arrives, or a TV season finishes arriving, the bot posts one clean card with the poster, so people see it without asking.

## Goals

1. A new movie in Radarr produces one announcement card in a chosen channel.
2. A TV season produces one "Season complete" card, once, when Sonarr reports every episode of that season has a file. Single episodes never post.
3. The card shows the poster, title with year, a short summary, and a details line (genre, runtime or episode count, quality).
4. Posts are silent: no role or user mentions.
5. Each item is announced once. Restarts, re-imports and upgrades never repost it.
6. Setup follows the existing pattern: a `/setupannouncements` slash command (admin only) run in the target channel records the channel; the setup page shows whether it is set and has an on/off switch for movies and for seasons.

## Non-goals

- Webhooks from Radarr or Sonarr. The bot polls (decided with the author).
- Pinging a role (decided: post silently).
- Announcing the existing library when the feature is first turned on.
- Individual episodes, partial seasons, or upgrades of existing files.
- Verifying against Plex itself. Sonarr's counts are trusted (decided with the author).
- Customising the card layout.

## Anti-goals (a failure even if it technically works)

- Reposting the same movie or season after a restart, an upgrade-in-place, or a failed poll.
- A flood on first enable, or on first run after the state file is lost, that announces the whole library.
- A half-filled card (missing title or poster) being posted because a lookup failed.
- Any API key appearing in the card, logs, or the page.
- Breaking `/setupstatus`, `/setupcontentqueue`, the download queue, or existing environment-variable deployments.

## Constraints

- Existing stack: Java 17, Gradle (Kotlin DSL), Javacord 3.8.0, Gson, Log4j2. No new runtime dependency.
- Radarr and Sonarr URL and key already exist in settings; nothing new is needed to talk to them.
- Settings are string fields in the `Settings` record with file-over-env precedence; new fields follow that pattern.
- Persistent state lives in `CONFIG_DIR` (default `/config`), next to `settings.json`.
- The setup page is vanilla JS in `src/main/resources/web/`; DOM APIs only; usable at phone width.
- Tests run without live Discord, Radarr or Sonarr.

## Decisions made with the author

- Detection is polling, every one to two minutes.
- A season is complete when Sonarr says so.
- Silent posts, no mentions.
- Card content: poster, title and year, summary, details line (mockup approved).
- Movies and seasons are announced; episodes are not.

## Calls made by the designer (not yet agreed by the author)

- The channel is set by `/setupannouncements`, run in the target channel, admin only. Re-running moves it to the new channel.
- The setup page gets two switches, "Announce new movies" and "Announce completed seasons", each living in its section (Radarr, Sonarr), on by default once a channel is set.
- Baseline: the first time an announcement type is enabled with a channel set, the bot records what already exists as announced without posting. Only later arrivals post.
- Announced items are tracked in `CONFIG_DIR/announced.json`, keyed by Radarr movie id, and by Sonarr series id plus season number.
- Posters and summaries come from Radarr and Sonarr, not Plex.
- A card posts when Radarr or Sonarr reports the file imported; the bot does not wait for Plex.
- If a poll fails, the bot logs it, marks the Radarr or Sonarr health dot, and retries on the next poll. It posts nothing partial.
- If posting to Discord fails (channel deleted, missing permission), the item stays un-announced and is retried; after a failure the bot logs once per cause rather than every poll.
- Cap per poll: at most five cards per poll cycle in total (movies and seasons together), oldest first, the rest on following cycles, so a bulk import does not flood the channel.

### Calls added after the builder check (designer's, not yet agreed by the author)

- Baseline is stored per type inside `announced.json`, not inferred from the file being absent. If `announced.json` is lost, the bot logs a warning and re-baselines silently. A dropped item is the lesser failure than a flood.
- The baseline runs once per type, the first time that type is on with a channel set. Changing the channel does not re-baseline. While a switch is off, polling still records new arrivals as seen without posting, so turning it back on never floods; things that arrive while it is off are not announced.
- Items are keyed by TMDB id for movies and TVDB id plus season number for seasons, so deleting and re-adding the same title does not repost. A season that gains episodes after it was announced is not announced again.
- A season counts as complete only when every aired, monitored episode has a file and no episode of that season has a future air date. A season still airing is never announced as complete. Specials (season 0) are not announced.
- A Discord post that fails leaves that item un-announced and retried with backoff. A failing item does not block others. A channel that is permanently gone keeps retrying slowly and shows on the health dot; the bot never disables itself.
- Existing deployments: a missing switch or channel field means off. Running `/setupannouncements` sets the channel and turns both switches on.
- One announcement channel globally, like the status and queue channels.

## Left to the builder

- Package layout, class names, the exact Radarr and Sonarr endpoints and fields used to detect "new movie with file" and "season with all episodes", including how to treat unmonitored episodes and specials.
- State file format and write safety (atomic write).
- How the poll loop is scheduled alongside the existing tasks, and how it picks up settings changes made on the page without a restart.
- The JSON shape of the new settings fields and page routes, following the existing ones.
- Test strategy: the detection logic, the baseline rule, de-duplication and the per-poll cap should be unit-testable with fake Radarr and Sonarr responses.

## Known facts about the current code

- `Settings` holds the Radarr and Sonarr URL and key; `SettingsStore` reads `settings.json` with environment fallback.
- `Application` runs scheduled tasks and holds the Discord client; `DownloadQueueWorker` already calls Radarr.
- `SetupCommandsListener` registers admin-only per-server setup commands; `/setupstatus` and `/setupcontentqueue` store a channel id and message id in settings.
- `HealthMonitor` drives the per-section health dots on the setup page.
