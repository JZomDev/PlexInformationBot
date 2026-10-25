#!/bin/sh
PUID="${PUID:-99}"
PGID="${PGID:-100}"
CONFIG_DIR="${CONFIG_DIR:-/config}"
export CONFIG_DIR

if [ "$PUID" = "0" ]; then
	exec java -jar PlexInformationBot.jar
fi

case "$PUID$PGID" in
	*[!0-9]*|"")
		echo "ERROR: PUID and PGID must be numbers (got PUID=$PUID, PGID=$PGID)" >&2
		exit 1
		;;
esac

getent group "$PGID" >/dev/null 2>&1 || groupadd -g "$PGID" plexbot >/dev/null 2>&1 || { echo "ERROR: could not create group $PGID" >&2; exit 1; }
getent passwd "$PUID" >/dev/null 2>&1 || useradd -u "$PUID" -g "$PGID" -M -s /usr/sbin/nologin plexbot >/dev/null 2>&1 || { echo "ERROR: could not create user $PUID" >&2; exit 1; }

mkdir -p "$CONFIG_DIR" 2>/dev/null
chown -R "$PUID:$PGID" "$CONFIG_DIR" 2>/dev/null || echo "WARN: could not chown $CONFIG_DIR (read-only or unsupported mount); continuing"

exec setpriv --reuid="$PUID" --regid="$PGID" --init-groups java -jar PlexInformationBot.jar
