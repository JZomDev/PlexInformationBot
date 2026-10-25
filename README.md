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

## Security notes

- Open the page right after starting the container and change the default login. Until you do, anyone who can reach the port can sign in as `admin` / `admin`.
- Never forward the port to the internet. If you must expose it, put it behind your own reverse proxy with HTTPS.
- Behind a reverse proxy that rewrites the Host header, signing in can return 403. The page checks that the request's Origin matches its Host, so pass the original Host through. For nginx: `proxy_set_header Host $host;`
- To reset the login to `admin` / `admin`, delete `credentials.json` in the config folder.
- The Discord bot token, Plex token and API keys are saved in `settings.json` in the config folder. Keep that folder private. The page only shows saved secrets as `********`.

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
| `ROLE_ID` | none, set by `/setupreact` |
| `TEXT_CHANNELID` | none, set by `/setupstatus` |
| `MESSAGEID` | none, set by `/setupstatus` |
| `QUEUE_TEXT_CHANNELID` | none, set by `/setupcontentqueue` |
| `QUEUEMESSAGEID` | none, set by `/setupcontentqueue` |

## Building from source

Requires Java 17.

```
./gradlew fatJar
java -jar build/libs/PlexInformationBot.jar
```

To build the image yourself: `docker build -t plexinfobot .`
