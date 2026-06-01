#!/usr/bin/env bash
# This script was AI-generated (claude-3.5-sonnet)
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

BINARY="${1:-$PROJECT_DIR/build/docker/engine.kexe}"
OUTPUT_DIR="${2:-$PROJECT_DIR/build/batocera}"

if [[ ! -f "$BINARY" ]]; then
    echo ":: Binary not found at $BINARY"
    echo "   Build it first: ./scripts/build-linux.sh"
    exit 1
fi

mkdir -p "$OUTPUT_DIR/platformer/Assets"

cp "$BINARY" "$OUTPUT_DIR/platformer/platformer.kexe"
cp -r "$PROJECT_DIR/Assets/Maps" "$OUTPUT_DIR/platformer/Assets/Maps"
cp -r "$PROJECT_DIR/Assets/Inputs" "$OUTPUT_DIR/platformer/Assets/Inputs"

LAUNCHER="$OUTPUT_DIR/platformer-launcher.sh"
cat > "$LAUNCHER" << 'LAUNCHER_EOF'
#!/bin/bash
# Batocera port launcher for Platformer
# Place this file in /userdata/roms/ports/
# Place the platformer/ directory in /userdata/roms/ports/
# Ensure the binary is executable: chmod +x /userdata/roms/ports/platformer/platformer.kexe

PORT_DIR="$(dirname "$0")/platformer"
cd "$PORT_DIR"

export MODE="${MODE:-PLAY}"
export MAP="${MAP:-1_1}"

"./platformer.kexe"
LAUNCHER_EOF
chmod +x "$LAUNCHER"

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
