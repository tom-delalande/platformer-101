#!/usr/bin/env bash
# This script was AI-generated (claude-3.5-sonnet)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

BUILD_TYPE="${1:-release}"

if [[ "$BUILD_TYPE" != "debug" && "$BUILD_TYPE" != "release" ]]; then
    echo "Usage: $0 [debug|release]"
    exit 1
fi

if [[ "$(uname -s)" == "Linux" ]]; then
    echo ":: Native Linux build detected"

    if ! pkg-config --exists raylib 2>/dev/null; then
        echo ":: raylib not found via pkg-config. Install it:"
        echo "   sudo apt install libraylib-dev"
        echo "   or build from https://github.com/raysan5/raylib"
        exit 1
    fi

    cd "$PROJECT_DIR"
    if [[ "$BUILD_TYPE" == "debug" ]]; then
        ./gradlew :engine:linkDebugExecutableLinuxX64
    else
        ./gradlew :engine:linkReleaseExecutableLinuxX64
    fi

    BINARY="$PROJECT_DIR/engine/build/bin/linuxX64/${BUILD_TYPE}Executable/engine.kexe"
    if [[ -f "$BINARY" ]]; then
        echo ":: Build successful: $BINARY"
    else
        echo ":: Build completed but binary not found at $BINARY"
        exit 1
    fi
else
    echo ":: Cross-platform build using Docker"

    cd "$PROJECT_DIR"
    docker build \
        --platform linux/amd64 \
        --build-arg "BUILD_TYPE=$BUILD_TYPE" \
        -t platformer-builder \
        -f Dockerfile \
        .

    mkdir -p "$PROJECT_DIR/build/docker"
    docker run --rm \
        --platform linux/amd64 \
        -v "$PROJECT_DIR/build/docker:/output" \
        platformer-builder \
        bash -c "cp engine/build/bin/linuxX64/${BUILD_TYPE}Executable/engine.kexe /output/"

    BINARY="$PROJECT_DIR/build/docker/engine.kexe"
    if [[ -f "$BINARY" ]]; then
        echo ":: Build successful: $BINARY"
    else
        echo ":: Build completed but binary not found at $BINARY"
        exit 1
    fi
fi
