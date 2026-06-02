#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SKIP_ASSETS=false
if [[ "${1:-}" == "--skip-assets" ]]; then
    SKIP_ASSETS=true
fi

# Kotlin 2.3.21 doesn't support Java 25+
export JAVA_HOME="${JAVA_HOME:-$HOME/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home}"

# Step 1: Rebuild raylib static lib for ARM64 via Docker
echo ":: Building raylib library..."
docker buildx build --platform linux/arm64 --no-cache -t raylib-builder .
docker run --rm -v "$PROJECT_DIR"/native/raylib/raylib-6.0_linux_arm64:/output raylib-builder

# Step 2: Rebuild the Kotlin/Native binary (links against the DRM raylib)
echo ":: Building Kotlin/Native executable..."
cd "$PROJECT_DIR"
./gradlew clean :engine:linkReleaseExecutableLinuxArm64 --no-daemon

# Step 3: Package for Batocera
echo ":: Packaging for Batocera..."
"$SCRIPT_DIR/package-for-batocera.sh"

# Step 4: Deploy to Batocera
OUTPUT_DIR="$PROJECT_DIR/build/batocera"
BATOCERA_HOST="${BATOCERA_HOST:-root@BATOCERA.local}"
BATOCERA_PORTS="${BATOCERA_PORTS:-/userdata/roms/ports}"

if [[ "$SKIP_ASSETS" == true ]]; then
    echo ":: Deploying binary + launcher only (--skip-assets)..."
    ssh "$BATOCERA_HOST" "mkdir -p $BATOCERA_PORTS/platformer"
    scp "$OUTPUT_DIR/platformer/platformer.kexe" "$BATOCERA_HOST:$BATOCERA_PORTS/platformer/platformer.kexe"
    scp "$OUTPUT_DIR/platformer-launcher.sh" "$BATOCERA_HOST:$BATOCERA_PORTS/platformer-launcher.sh"
else
    echo ":: Deploying full package (binary + assets)..."
    scp -r "$OUTPUT_DIR/"* "$BATOCERA_HOST:$BATOCERA_PORTS/"
fi

echo ":: Done"
