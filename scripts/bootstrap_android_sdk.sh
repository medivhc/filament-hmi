#!/usr/bin/env bash
set -euo pipefail
SDK_ROOT="${ANDROID_SDK_ROOT:-/workspace/android-sdk}"
CMDLINE_TOOLS_ZIP_URL="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip"
CMDLINE_TOOLS_ZIP="/tmp/cmdline-tools.zip"
CMDLINE_DIR="$SDK_ROOT/cmdline-tools/latest"
SDKMANAGER="$CMDLINE_DIR/bin/sdkmanager"
mkdir -p "$SDK_ROOT/cmdline-tools"
if [[ ! -x "$SDKMANAGER" ]]; then
  echo "[bootstrap-android-sdk] Installing command line tools into $CMDLINE_DIR"
  curl -L "$CMDLINE_TOOLS_ZIP_URL" -o "$CMDLINE_TOOLS_ZIP"
  unzip -q -o "$CMDLINE_TOOLS_ZIP" -d "$SDK_ROOT/cmdline-tools"
  rm -rf "$CMDLINE_DIR"
  mv "$SDK_ROOT/cmdline-tools/cmdline-tools" "$CMDLINE_DIR"
else
  echo "[bootstrap-android-sdk] Command line tools already installed"
fi
echo "[bootstrap-android-sdk] Ensuring Android SDK packages are installed"
set +o pipefail
yes | "$SDKMANAGER" --sdk_root="$SDK_ROOT" "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null
set -o pipefail
echo "[bootstrap-android-sdk] Done. SDK_ROOT=$SDK_ROOT"
