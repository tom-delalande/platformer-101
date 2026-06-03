#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)

$SCRIPT_DIR/build-raylib-linuxArm64.sh
$SCRIPT_DIR/build-game-linuxArm64.sh
$SCRIPT_DIR/package-for-batocera.sh
$SCRIPT_DIR/deploy-batocera-linuxArm64.sh