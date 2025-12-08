#!/bin/sh
# Replace placeholder with actual environment variable
if [ ! -z "$VITE_API_BASE" ]; then
  find /usr/share/nginx/html -type f -name "*.js" -exec sed -i "s|__VITE_API_BASE__|$VITE_API_BASE|g" {} \;
fi

exec "$@"
