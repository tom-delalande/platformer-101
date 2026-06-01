# This Dockerfile was AI-generated (claude-3.5-sonnet)
FROM fedora:40

ARG BUILD_TYPE=release

RUN dnf install -y \
    curl \
    unzip \
    java-17-openjdk-devel \
    mesa-libGL-devel \
    mesa-libGLU-devel \
    libXrandr-devel \
    libXinerama-devel \
    libXcursor-devel \
    libXi-devel \
    alsa-lib-devel \
    pkgconfig \
    && dnf clean all

RUN curl -L "https://github.com/raysan5/raylib/releases/download/6.0/raylib-6.0_linux_amd64.tar.gz" \
    -o /tmp/raylib.tar.gz \
    && tar -xzf /tmp/raylib.tar.gz -C /usr/local --strip-components=1 \
    && rm /tmp/raylib.tar.gz

WORKDIR /workspace
COPY . .

RUN case "$BUILD_TYPE" in \
        debug) TASK="linkDebugExecutableLinuxX64" ;; \
        release) TASK="linkReleaseExecutableLinuxX64" ;; \
    esac \
    && ./gradlew ":engine:${TASK}" --no-daemon

CMD ["cp", "engine/build/bin/linuxX64/releaseExecutable/engine.kexe", "/output/"]
