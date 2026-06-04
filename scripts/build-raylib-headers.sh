#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

HEADERS_DIR=$PROJECT_DIR/native/include

if [[ -f "$HEADERS_DIR/raylib.h" ]]; then
    echo ":: Headers already exist at $HEADERS_DIR"
    exit 0
fi

echo ":: Extracting raylib headers to $HEADERS_DIR..."
mkdir -p "$HEADERS_DIR"

TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

# Use the default Dockerfile (linux/arm64) for headers — they're platform-independent
docker buildx build --platform linux/arm64 --no-cache -t raylib-headers-builder .
docker run --rm -v "$TMP_DIR:/output" raylib-headers-builder

cp -r "$TMP_DIR/include/"* "$HEADERS_DIR/"
echo ":: raylib headers installed to $HEADERS_DIR"
