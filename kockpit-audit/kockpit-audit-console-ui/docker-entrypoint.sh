#!/bin/sh
# Replace placeholder with actual environment variable
if [ ! -z "$VITE_API_BASE" ]; then
  sed -i "s|\${VITE_API_BASE}|$VITE_API_BASE|g" /usr/share/nginx/html/config.js
fi

exec "$@"
