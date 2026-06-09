#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

# Use the latest matching release branches
SDL_VERSION="release-3.2.10"
SDL_IMAGE_VERSION="release-3.2.4"
SDL_TTF_VERSION="release-3.2.2"

TMP_DIR=$(mktemp -d)
trap "rm -rf $TMP_DIR" INT TERM

clone_repo() {
    local name=$1
    local url=$2
    local version=$3
    local dir="$TMP_DIR/$name"
    if [ ! -d "$dir" ]; then
        echo ":: Cloning $name ($version)..."
        git clone --depth 1 --branch "$version" "$url" "$dir"
    fi
}

# Clone all repos once (shared across architecture builds)
clone_repo "SDL" "https://github.com/libsdl-org/SDL.git" "$SDL_VERSION"
clone_repo "SDL_image" "https://github.com/libsdl-org/SDL_image.git" "$SDL_IMAGE_VERSION"
clone_repo "SDL_ttf" "https://github.com/libsdl-org/SDL_ttf.git" "$SDL_TTF_VERSION"

# SDL_ttf needs vendored submodules for freetype, harfbuzz, plutosvg
if [ ! -f "$TMP_DIR/SDL_ttf/external/freetype/CMakeLists.txt" ]; then
    echo ":: Downloading SDL_ttf vendored dependencies..."
    (cd "$TMP_DIR/SDL_ttf" && bash external/download.sh)
fi

build_arch() {
    local TARGET=$1
    local ARCH=$2
    local SDK=$3
    local DEPLOYMENT_TARGET=$4
    local INSTALL_PREFIX="$TMP_DIR/install-$TARGET"
    local OUTPUT_DIR="$PROJECT_DIR/native/${TARGET}/lib"

    mkdir -p "$OUTPUT_DIR" "$INSTALL_PREFIX"

    # ---- SDL3 ----
    echo ":: Building SDL3 ($TARGET)..."
    cmake -S "$TMP_DIR/SDL" -B "$TMP_DIR/build-SDL-$TARGET" \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_SYSTEM_NAME=iOS \
        -DCMAKE_OSX_SYSROOT="$SDK" \
        -DCMAKE_OSX_ARCHITECTURES="$ARCH" \
        -DCMAKE_OSX_DEPLOYMENT_TARGET="$DEPLOYMENT_TARGET" \
        -DCMAKE_INSTALL_PREFIX="$INSTALL_PREFIX" \
        -DSDL_SHARED=OFF \
        -DSDL_STATIC=ON \
        -DSDL_TESTS=OFF \
        -DSDL_HIDAPI=ON \
        -DSDL_VULKAN=OFF
    cmake --build "$TMP_DIR/build-SDL-$TARGET" --config Release
    cmake --install "$TMP_DIR/build-SDL-$TARGET" --config Release
    find "$INSTALL_PREFIX" -name "libSDL3.a" -exec cp {} "$OUTPUT_DIR/libSDL3.a" \;
    if [ ! -f "$OUTPUT_DIR/libSDL3.a" ]; then
        echo ":: ERROR: libSDL3.a not found for $TARGET"
        exit 1
    fi

    # ---- SDL3_image ----
    echo ":: Building SDL3_image ($TARGET)..."
    cmake -S "$TMP_DIR/SDL_image" -B "$TMP_DIR/build-SDL_image-$TARGET" \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_SYSTEM_NAME=iOS \
        -DCMAKE_OSX_SYSROOT="$SDK" \
        -DCMAKE_OSX_ARCHITECTURES="$ARCH" \
        -DCMAKE_OSX_DEPLOYMENT_TARGET="$DEPLOYMENT_TARGET" \
        -DCMAKE_INSTALL_PREFIX="$INSTALL_PREFIX" \
        -DSDL3_DIR="$INSTALL_PREFIX/lib/cmake/SDL3" \
        -DBUILD_SHARED_LIBS=OFF \
        -DSDLIMAGE_VENDORED=OFF \
        -DSDLIMAGE_BACKEND_STB=ON \
        -DSDLIMAGE_BACKEND_IMAGEIO=OFF \
        -DSDLIMAGE_AVIF=OFF \
        -DSDLIMAGE_JXL=OFF \
        -DSDLIMAGE_TIF=OFF \
        -DSDLIMAGE_WEBP=OFF \
        -DSDLIMAGE_DEPS_SHARED=OFF \
        -DSDLIMAGE_TESTS=OFF \
        -DSDLIMAGE_SAMPLES=OFF
    cmake --build "$TMP_DIR/build-SDL_image-$TARGET" --config Release
    cmake --install "$TMP_DIR/build-SDL_image-$TARGET" --config Release
    find "$TMP_DIR/build-SDL_image-$TARGET" -name "libSDL3_image.a" -exec cp {} "$OUTPUT_DIR/libSDL3_image.a" \;
    if [ ! -f "$OUTPUT_DIR/libSDL3_image.a" ]; then
        find "$INSTALL_PREFIX" -name "libSDL3_image.a" -exec cp {} "$OUTPUT_DIR/libSDL3_image.a" \;
    fi
    if [ ! -f "$OUTPUT_DIR/libSDL3_image.a" ]; then
        echo ":: ERROR: libSDL3_image.a not found for $TARGET"
        exit 1
    fi

    # ---- SDL3_ttf ----
    echo ":: Building SDL3_ttf ($TARGET)..."
    cmake -S "$TMP_DIR/SDL_ttf" -B "$TMP_DIR/build-SDL_ttf-$TARGET" \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_SYSTEM_NAME=iOS \
        -DCMAKE_OSX_SYSROOT="$SDK" \
        -DCMAKE_OSX_ARCHITECTURES="$ARCH" \
        -DCMAKE_OSX_DEPLOYMENT_TARGET="$DEPLOYMENT_TARGET" \
        -DCMAKE_INSTALL_PREFIX="$INSTALL_PREFIX" \
        -DSDL3_DIR="$INSTALL_PREFIX/lib/cmake/SDL3" \
        -DBUILD_SHARED_LIBS=OFF \
        -DSDLTTF_VENDORED=ON \
        -DSDLTTF_HARFBUZZ=ON \
        -DSDLTTF_PLUTOSVG=ON \
        -DSDLTTF_SAMPLES=OFF
    cmake --build "$TMP_DIR/build-SDL_ttf-$TARGET" --config Release
    cmake --install "$TMP_DIR/build-SDL_ttf-$TARGET" --config Release
    find "$TMP_DIR/build-SDL_ttf-$TARGET" -name "libSDL3_ttf.a" -exec cp {} "$OUTPUT_DIR/libSDL3_ttf.a" \;
    if [ ! -f "$OUTPUT_DIR/libSDL3_ttf.a" ]; then
        find "$INSTALL_PREFIX" -name "libSDL3_ttf.a" -exec cp {} "$OUTPUT_DIR/libSDL3_ttf.a" \;
    fi
    if [ ! -f "$OUTPUT_DIR/libSDL3_ttf.a" ]; then
        echo ":: ERROR: libSDL3_ttf.a not found for $TARGET"
        exit 1
    fi
}

echo ":: Building SDL3 iOS libraries..."
echo ":: TMP_DIR=$TMP_DIR"

# Build for iOS device (arm64)
build_arch "iosArm64" "arm64" "iphoneos" "13.0"

# Build for iOS Simulator (arm64, Apple Silicon)
build_arch "iosSimulatorArm64" "arm64" "iphonesimulator" "13.0"

echo ":: Done! SDL3 iOS libraries built to:"
echo "   - $PROJECT_DIR/native/iosArm64/lib/"
echo "   - $PROJECT_DIR/native/iosSimulatorArm64/lib/"
