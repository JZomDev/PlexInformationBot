# PlexInformationBot

A Discord bot that shows what your Plex server is doing: the number of people watching in the bot's status, a status message with the current streams, an optional Radarr/Sonarr download queue message, and a role message people can react to. The bot is set up on a small web page it serves itself, so no environment variables are needed on a new install.

## Quick start with Docker

```
docker run -d --name plexinfobot -p 4546:4546 -v "$(pwd)/config:/config" redzguilt/plexinfobot
```

Instead of the `config` folder you can use a named volume, for example `-v plexinfobot-config:/config`.

1. Open http://localhost:4546.
2. Sign in with `admin` / `admin`. You are asked to choose your own username and password straight away (at least 8 characters).
3. Fill in the Discord bot token and your Plex address, port and token. Tautulli, Radarr and Sonarr are optional.
4. Click Test in each section to check the connection.
5. Click Save. The bot starts (or restarts) with the new settings.
6. In Discord, run these in the channels you want:
   - `/setupreact` (pick the role to hand out) posts the role message.
   - `/setupstatus` posts the status message.
   - `/setupcontentqueue` posts the download queue message.

   These commands are admin-only.

## Changing the port

The setup page listens on 4546. To use another port, set `WEB_PORT` and map the same port:

```
docker run -d --name plexinfobot -e WEB_PORT=8080 -p 8080:8080 -v "$(pwd)/config:/config" redzguilt/plexinfobot
```

If the port is already taken, the bot still starts when a Discord token is configured, but without the setup page (check the log for the bind error). With no token configured it exits with an error, since there would be nothing to run.

`WEB_PORT` is the only environment setting a new install needs. `CONFIG_DIR` (default `/config`) can move the config folder but you normally do not need it.

## Unraid

Add a port mapping for container port 4546 and a path mapping from `/config` to a folder in your appdata share. The container runs as root, so a mounted folder is writable whatever its owner is.

## Using messages you already have

Normally `/setupstatus`, `/setupcontentqueue` and `/setupreact` post a new message and save its IDs. If the bot already wrote a status, queue or role message and you want to keep using it, turn on Advanced mode, click the Status, Queue or Role message item at the top of the setup page to open a popup for that message, and fill in its IDs instead.

- In Discord turn on Developer Mode (User Settings, Advanced), right-click the message and choose Copy Message Link. Paste it into "Paste message link" in the matching group and the channel and message IDs are filled in for you. The role ID is separate: in Server Settings, Roles, right-click the role and choose Copy Role ID.
- The IDs are the long numbers Discord shows (17 to 20 digits). Anything else is refused when you save.
- Click Test in a group to check, with the running bot, that the channel and message exist and that the bot wrote the message (a bot can only edit its own messages). For the role message it also checks that the role can be handed out. Test needs the bot to be connected, so save the Discord token first.
- Only fields you change are saved, so a message set up by a slash command in the meantime is not overwritten by an older copy of the page. Emptying a field clears it.

## Advanced mode

The setup page starts in basic mode: only the Discord token and Plex sections are shown, and the status line at the top (Status, Queue and Role message) is plain text. Tick "Advanced mode" in the header to also show Tautulli, Radarr, Sonarr, and to make the status line items links. Clicking one opens a popup with just that group (status, queue or role); the popup has its own Save button, and Done, the X, Escape or a click outside closes it. The choice is remembered in your browser. Switching modes never clears anything: fields that are hidden keep their saved values.

## Logs

The bot writes its log to `bot.log` in the `logs` folder inside the config folder (`/config/logs/bot.log` by default, readable only by its owner). It rolls over at 2 MB and keeps 3 older files (`bot.1.log` to `bot.3.log`). Console output is unchanged.

Click "Logs" in the setup page header to watch the latest `bot.log` live. It refreshes every 2 seconds while open, scrolls with new lines unless you scroll up, and has a Pause button. Only the last 1000 lines are kept in the page.

Secrets are redacted before the log leaves the server: Plex tokens, `token` and API key values in URLs or headers, and every token or key saved in your settings are replaced with `****`. Redaction also covers the written file for token and API key parameters, but the file is still your private data, so keep the config folder private.

## Security notes

- Open the page right after starting the container and change the default login. Until you do, anyone who can reach the port can sign in as `admin` / `admin`.
- Never forward the port to the internet. If you must expose it, put it behind your own reverse proxy with HTTPS.
- Behind a reverse proxy that rewrites the Host header, signing in can return 403. The page checks that the request's Origin matches its Host, so pass the original Host through. For nginx: `proxy_set_header Host $host;`
- To reset the login to `admin` / `admin`, delete `credentials.json` in the config folder.
- The Discord bot token, Plex token and API keys are saved in `settings.json` in the config folder. Keep that folder private. The page only shows saved secrets as `********`; the eye icon next to a saved secret reveals it to a signed-in user, on click only.

## Existing installs

The old environment variables still work as a fallback. Anything saved on the setup page wins, field by field. An empty field on the page counts as unset, so the environment variable is used.

| Environment variable | Setup page |
| --- | --- |
| `BOT_TOKEN` | Discord: Bot token |
| `IP` | Plex: Plex IP or host |
| `PORT` | Plex: Plex port |
| `PLEX_KEY` | Plex: Plex token |
| `API_KEY` | Tautulli: Tautulli API key |
| `TAUTULLI_URL` | Tautulli: Tautulli host:port |
| `RADARR_URL` | Radarr: Radarr host:port |
| `RADARR_API` | Radarr: Radarr API key |
| `SONARR_URL` | Sonarr: Sonarr host:port |
| `SONARR_API` | Sonarr: Sonarr API key |
| `ROLE_ID` | Discord messages: Role ID (or `/setupreact`) |
| `TEXT_CHANNELID` | Discord messages: Status channel ID (or `/setupstatus`) |
| `MESSAGEID` | Discord messages: Status message ID (or `/setupstatus`) |
| `QUEUE_TEXT_CHANNELID` | Discord messages: Queue channel ID (or `/setupcontentqueue`) |
| `QUEUEMESSAGEID` | Discord messages: Queue message ID (or `/setupcontentqueue`) |

## Building from source

Requires Java 17.

```
./gradlew fatJar
java -jar build/libs/PlexInformationBot.jar
```

To build the image yourself: `docker build -t plexinfobot .`
