#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT_DIR/artifacts"
mkdir -p "$OUT_DIR"

# Requires running emulator/device with adb available.
adb shell screencap -p /sdcard/filament_hmi_scene.png
adb pull /sdcard/filament_hmi_scene.png "$OUT_DIR/render.png"

echo "Screenshot exported to $OUT_DIR/render.png"
