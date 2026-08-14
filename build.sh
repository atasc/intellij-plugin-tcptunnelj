#!/bin/bash
# Builds the plugin zip and opens build/distributions in the file manager.
set -euo pipefail

cd "$(dirname "$(readlink -f "$0")")"

./gradlew buildPlugin

DIST_DIR="$PWD/build/distributions"

if [ ! -d "$DIST_DIR" ]; then
  echo "build.sh: $DIST_DIR not found, nothing to open" >&2
  exit 1
fi

ls -lh "$DIST_DIR"

if command -v xdg-open >/dev/null 2>&1; then
  nohup xdg-open "$DIST_DIR" >/dev/null 2>&1 &
elif command -v gio >/dev/null 2>&1; then
  nohup gio open "$DIST_DIR" >/dev/null 2>&1 &
else
  echo "build.sh: no xdg-open/gio available, open $DIST_DIR manually" >&2
fi

exit 0
