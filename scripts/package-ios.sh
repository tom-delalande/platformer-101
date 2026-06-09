#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

TARGET="${1:-iosSimulatorArm64}"
CONFIG="${2:-release}"

# Map target to architecture
case "$TARGET" in
    iosArm64)
        ARCH="arm64"
        SDK="iphoneos"
        DEPLOY_TARGET="13.0"
        EXECUTABLE_BASE="engine"
        ;;
    iosSimulatorArm64)
        ARCH="arm64"
        SDK="iphonesimulator"
        DEPLOY_TARGET="13.0"
        EXECUTABLE_BASE="engine"
        ;;
    *)
        echo "Usage: $0 {iosArm64|iosSimulatorArm64} [release|debug]"
        exit 1
        ;;
esac

APP_NAME="Platformer101"
APP_DIR="$PROJECT_DIR/build/iosApp/$APP_NAME.app"

echo ":: Building for $TARGET ($CONFIG)..."
if [ "$CONFIG" = "release" ]; then
    "$PROJECT_DIR/gradlew" :engine:linkReleaseExecutable"$TARGET"
else
    "$PROJECT_DIR/gradlew" :engine:linkDebugExecutable"$TARGET"
fi

echo ":: Creating .app bundle at $APP_DIR..."
mkdir -p "$APP_DIR"

# Copy the executable
EXECUTABLE_PATH=$(find "$PROJECT_DIR/engine/build/bin/$TARGET" -name "*.kexe" -o -name "engine" -type f | head -1)
if [ -z "$EXECUTABLE_PATH" ]; then
    # Try default Kotlin/Native output path
    EXECUTABLE_PATH="$PROJECT_DIR/engine/build/bin/$TARGET/${CONFIG}Executable/engine.kexe"
fi

if [ ! -f "$EXECUTABLE_PATH" ]; then
    echo ":: ERROR: Executable not found at $EXECUTABLE_PATH"
    echo ":: Build output search path: $PROJECT_DIR/engine/build/bin/$TARGET/"
    find "$PROJECT_DIR/engine/build/bin" -type f 2>/dev/null || true
    exit 1
fi

cp "$EXECUTABLE_PATH" "$APP_DIR/$APP_NAME"

# Copy Info.plist
cp "$PROJECT_DIR/native/$TARGET/Info.plist" "$APP_DIR/"
# Update executable name in plist
sed -i '' "s|<string>game</string>|<string>$APP_NAME</string>|" "$APP_DIR/Info.plist"

# Copy Assets
cp -r "$PROJECT_DIR/Assets" "$APP_DIR/Assets"

# Sign (required for device, optional for simulator)
if [ "$TARGET" = "iosArm64" ]; then
    echo ":: Signing with ad-hoc signature..."
    codesign --force --sign - --entitlements /dev/null "$APP_DIR"
fi

echo ":: Done! App bundle at: $APP_DIR"
echo ""
echo "To install on simulator:"
echo "  xcrun simctl install booted \"$APP_DIR\""
echo "  xcrun simctl launch booted com.platformer-101.app"
echo ""
echo "To run directly (simulator only):"
echo "  $PROJECT_DIR/gradlew :engine:run$TARGET"
