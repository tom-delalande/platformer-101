#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/.. && pwd)

SKIP_ASSETS=false
if [[ "${1:-}" == "--skip-assets" ]]; then
    SKIP_ASSETS=true
fi

echo ":: Packaging for Batocera..."

OUTPUT_DIR=$PROJECT_DIR/build/batocera
BATOCERA_HOST=${BATOCERA_HOST:-root@BATOCERA.local}
BATOCERA_PORTS=${BATOCERA_PORTS:-/userdata/roms/ports}

if [[ "$SKIP_ASSETS" == true ]]; then
    echo ":: Deploying binary + launcher only (--skip-assets)..."
    ssh $BATOCERA_HOST "mkdir -p $BATOCERA_PORTS/platformer"
    scp "$OUTPUT_DIR/platformer/platformer.kexe" "$BATOCERA_HOST:$BATOCERA_PORTS/platformer/platformer.kexe"
    scp "$OUTPUT_DIR/platformer-launcher.sh" "$BATOCERA_HOST:$BATOCERA_PORTS/platformer-launcher.sh"
else
    echo ":: Deploying full package (binary + assets)..."
    scp -r "$OUTPUT_DIR/"* "$BATOCERA_HOST:$BATOCERA_PORTS/"
fi

echo ":: Done"
