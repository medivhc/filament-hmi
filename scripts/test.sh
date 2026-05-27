#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SDK_ROOT="${ANDROID_SDK_ROOT:-/workspace/android-sdk}"
"$ROOT_DIR/scripts/bootstrap_android_sdk.sh"
cat > "$ROOT_DIR/local.properties" <<EOL
sdk.dir=$SDK_ROOT
EOL
cd "$ROOT_DIR"
if [ ! -f "$ROOT_DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  if command -v gradle >/dev/null 2>&1; then
    gradle wrapper --gradle-version 8.14.4
  else
    echo "[ERROR] gradle/wrapper/gradle-wrapper.jar missing and 'gradle' is unavailable to regenerate wrapper." >&2
    exit 1
  fi
fi
./gradlew test
