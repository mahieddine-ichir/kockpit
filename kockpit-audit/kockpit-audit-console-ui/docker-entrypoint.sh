#!/bin/sh
# Replace placeholder with actual environment variable
#if [ ! -z "$VITE_API_BASE" ]; then
#  sed -i "s|\${VITE_API_BASE}|$VITE_API_BASE|g" /usr/share/nginx/html/config.js
#fi

sed -i "s|\${__KOCKPIT_BACKEND__}|$__KOCKPIT_BACKEND__|g" /etc/nginx/conf.d/default.conf

exec "$@"
