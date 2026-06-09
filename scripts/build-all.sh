#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

echo "=============================================="
echo " Platformer — Building All Targets"
echo "=============================================="
echo ""

BUILD_RAYLIB=${BUILD_RAYLIB:-true}
BUILD_GAME=${BUILD_GAME:-true}
SUCCESSES=()
FAILURES=()

# ── Step 1: Build native libs (Docker) ──────────────────────────

if [[ "$BUILD_RAYLIB" == "true" ]]; then
    echo ""
    echo "═══ Step 1: Building native libraries ═══"
    echo ""

    # linuxArm64
    echo "--- linuxArm64: Building raylib ---"
    if $SCRIPT_DIR/build-sdl-linuxArm64.sh; then
        SUCCESSES+=("raylib-linuxArm64")
    else
        FAILURES+=("raylib-linuxArm64")
    fi
    echo ""

    # linuxX64
    echo "--- linuxX64: Building raylib ---"
    if $SCRIPT_DIR/build-raylib-linuxX64.sh; then
        SUCCESSES+=("raylib-linuxX64")
    else
        FAILURES+=("raylib-linuxX64")
    fi
    echo ""

    # mingwX64
    echo "--- mingwX64: Building raylib ---"
    if $SCRIPT_DIR/build-raylib-windowsX64.sh; then
        SUCCESSES+=("raylib-windowsX64")
    else
        FAILURES+=("raylib-windowsX64")
    fi
    echo ""

    # Ensure headers are present (macOS ARM uses Homebrew, no Docker needed)
    if [[ ! -f "$PROJECT_DIR/native/include/raylib.h" ]]; then
        echo "--- Shared headers: extracting (using linuxArm64 Docker image) ---"
        if $SCRIPT_DIR/build-raylib-headers.sh; then
            SUCCESSES+=("raylib-headers")
        else
            FAILURES+=("raylib-headers")
        fi
        echo ""
    fi

    echo "═══ Native library builds complete ═══"
else
    echo "═══ Skipping native library builds (BUILD_RAYLIB=false) ═══"
fi

# ── Step 2: Build game executables ──────────────────────────────

if [[ "$BUILD_GAME" == "true" ]]; then
    echo ""
    echo "═══ Step 2: Building game executables ═══"
    echo ""

    export JAVA_HOME="${JAVA_HOME:-$HOME/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home}"

    for TARGET in macosArm64 linuxArm64 linuxX64 mingwX64; do
        TASK=":engine:linkReleaseExecutable${TARGET}"
        echo "--- $TARGET: $TASK ---"
        cd "$PROJECT_DIR"
        if ./gradlew clean "$TASK" --no-daemon 2>&1; then
            SUCCESSES+=("game-$TARGET")
        else
            FAILURES+=("game-$TARGET")
        fi
        echo ""
    done

    echo "═══ Game builds complete ═══"
else
    echo "═══ Skipping game builds (BUILD_GAME=false) ═══"
fi

# ── Summary ─────────────────────────────────────────────────────

echo ""
echo "=============================================="
echo " Build Summary"
echo "=============================================="
echo ""

if [[ ${#SUCCESSES[@]} -gt 0 ]]; then
    echo "✅ Succeeded:"
    for T in "${SUCCESSES[@]}"; do echo "   - $T"; done
fi

if [[ ${#FAILURES[@]} -gt 0 ]]; then
    echo "❌ Failed:"
    for T in "${FAILURES[@]}"; do echo "   - $T"; done
fi

echo ""
echo "Build artifacts:"
echo "  macOS ARM64: engine/build/bin/macosArm64/releaseExecutable/engine.kexe"
echo "  Linux ARM64: engine/build/bin/linuxArm64/releaseExecutable/engine.kexe"
echo "  Linux x86_64: engine/build/bin/linuxX64/releaseExecutable/engine.kexe"
echo "  Windows x86_64: engine/build/bin/mingwX64/releaseExecutable/engine.exe"

if [[ ${#FAILURES[@]} -gt 0 ]]; then
    exit 1
fi
