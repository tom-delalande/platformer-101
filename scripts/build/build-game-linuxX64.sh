#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
PROJECT_DIR=$(cd $SCRIPT_DIR/../.. && pwd)

export JAVA_HOME="${JAVA_HOME:-$HOME/Library/Java/JavaVirtualMachines/temurin-21.0.5/Contents/Home}"

echo ":: Building Kotlin/Native executable for Linux x86_64..."
cd $PROJECT_DIR
./gradlew clean :engine:linkReleaseExecutableLinuxX64 --no-daemon
