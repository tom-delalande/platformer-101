#!/usr/bin/env bash
# This script was AI-generated (claude-3.5-sonnet)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

BINARY="${1:-$PROJECT_DIR/engine/build/bin/linuxArm64/releaseExecutable/engine.kexe}"
OUTPUT_DIR="${2:-$PROJECT_DIR/build/batocera}"

if [[ ! -f "$BINARY" ]]; then
    echo ":: Binary not found at $BINARY"
    echo "   Build it first: ./scripts/build-linux.sh"
    exit 1
fi

mkdir -p "$OUTPUT_DIR/platformer/Assets"

cp "$BINARY" "$OUTPUT_DIR/platformer/platformer.kexe"
cp -r "$PROJECT_DIR/Assets" "$OUTPUT_DIR/platformer"

LAUNCHER="$OUTPUT_DIR/platformer-launcher.sh"
cat > "$LAUNCHER" << 'LAUNCHER_EOF'
#!/bin/bash
# Batocera port launcher for Platformer
# Place this file in /userdata/roms/ports/
# Place the platformer/ directory in /userdata/roms/ports/
# Ensure the binary is executable: chmod +x /userdata/roms/ports/platformer/platformer.kexe

PORT_DIR="$(dirname "$0")/platformer"
cd "$PORT_DIR"

# Batocera SDL configuration
#export SDL_VIDEODRIVER="${SDL_VIDEODRIVER:-KMSDRM}"
#export SDL_RENDER_DRIVER="${SDL_RENDER_DRIVER:-opengles2}"
#export SDL_JOYSTICK_DEVICE=/dev/input/js0

export MODE="${MODE:-PLAY}"
export MAP="${MAP:-1_1}"

LOG_FILE="/tmp/platformer.log"
"./platformer.kexe" > "$LOG_FILE" 2>&1
exit $?
LAUNCHER_EOF
chmod +x "$LAUNCHER"
chmod +x "$OUTPUT_DIR/platformer/platformer.kexe"

cat > "$OUTPUT_DIR/gamelist.xml" << 'GAMELIST_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<gameList>
    <game>
        <path>./platformer-launcher.sh</path>
        <name>Platformer</name>
        <desc>A Kotlin/Native platformer game powered by raylib</desc>
        <rating>0</rating>
        <players>1</players>
    </game>
</gameList>
GAMELIST_EOF

echo ":: Package created at $OUTPUT_DIR"
echo ""
echo "To deploy to Batocera via SSH:"
echo "  scp -r $OUTPUT_DIR/* root@BATOCERA_IP:/userdata/roms/ports/"
echo ""
echo "Or copy to a USB stick and copy from Batocera's file manager."
