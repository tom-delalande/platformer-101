#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

echo ":: Building SDL library for Linux ARM64..."

TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" EXIT

docker buildx build \
    -f Dockerfile.linuxArm64 \
    --platform linux/arm64 \
    -t sdl3-arm64 .

PLATFORM_DIR=$PROJECT_DIR/native/linuxArm64
mkdir -p "$PLATFORM_DIR/lib"
mkdir -p "$PLATFORM_DIR/include"

docker create --name sdl3 sdl3-arm64
docker cp sdl3:/artifacts/lib "$PLATFORM_DIR"
docker cp sdl3:/artifacts/include "$PLATFORM_DIR"
docker rm sdl3

