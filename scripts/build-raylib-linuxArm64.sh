#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

echo ":: Building raylib library for Linux ARM64..."

TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

docker buildx build --platform linux/arm64 --no-cache  -f Dockerfile.linuxArm64 -t raylib-builder-linuxarm64 .
docker run --rm -v "$TMP_DIR:/output" raylib-builder-linuxArm64

# Extract shared headers (if not already present)
HEADERS_DIR=$PROJECT_DIR/native/include
if [[ ! -f "$HEADERS_DIR/raylib.h" ]]; then
    mkdir -p "$HEADERS_DIR"
    cp -r "$TMP_DIR/include/"* "$HEADERS_DIR/"
    echo ":: raylib headers installed to $HEADERS_DIR"
fi

# Extract platform libs
PLATFORM_DIR=$PROJECT_DIR/native/linuxArm64
mkdir -p "$PLATFORM_DIR/lib"
cp -r "$TMP_DIR/lib/"* "$PLATFORM_DIR/lib/"

echo ":: raylib + system libs for Linux ARM64 installed to $PLATFORM_DIR/lib/"
