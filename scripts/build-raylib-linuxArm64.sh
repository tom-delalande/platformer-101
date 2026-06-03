#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

echo ":: Building raylib library..."
docker buildx build --platform linux/arm64 --no-cache -t raylib-builder .
docker run --rm -v $PROJECT_DIR/native:/output raylib-builder
