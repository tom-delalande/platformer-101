<!-- This documentation was AI-generated (claude-3.5-sonnet) -->
# Building and Deploying to Batocera

[Batocera](https://batocera.org) is a retro-gaming Linux distribution. This guide covers how to build the Linux x64 binary of this platformer game and deploy it as a Batocera **port**.

## Prerequisites

- **Batocera v39+** (x86_64) running on your target machine
- SSH access to your Batocera machine (enable it in System Settings → Network → SSH)

### For native Linux builds
- JDK 17+
- raylib development libraries: `sudo apt install libraylib-dev libgl1-mesa-dev`

### For Docker builds (macOS / any platform)
- Docker with `linux/amd64` platform support

## Quick start

### 1. Build the Linux binary

**On Linux natively:**
```bash
./scripts/build-linux.sh release
```

**On macOS / any platform (using Docker):**
```bash
./scripts/build-linux.sh release
```

The script auto-detects the platform and uses Docker when not on Linux.

### 2. Package for Batocera

```bash
./scripts/package-for-batocera.sh
```

This creates a `build/batocera/` directory containing:
- `platformer/platformer.kexe` — the game binary
- `platformer/Assets/` — game assets (maps, sprites)
- `platformer-launcher.sh` — Batocera port launcher script
- `gamelist.xml` — Batocera gamelist metadata

### 3. Deploy to Batocera

**Via SSH:**
```bash
# Replace 192.168.1.100 with your Batocera machine's IP
scp -r build/batocera/* root@192.168.1.100:/userdata/roms/ports/
```

**Via USB:**
1. Copy the contents of `build/batocera/` to a USB drive (FAT32 or ext4)
2. Plug it into your Batocera machine
3. Use the Batocera file manager to copy the files to `/userdata/roms/ports/`

### 4. Run the game

- Restart EmulationStation or go to **GAMES → PORTS**
- Launch **Platformer**

## Batocera port structure

After deployment, your Batocera machine should have:

```
/userdata/roms/ports/
├── platformer-launcher.sh   # Launcher script (.sh = port)
├── gamelist.xml             # Metadata for the port
└── platformer/
    ├── platformer.kexe       # The game binary
    └── Assets/
        ├── Maps/
        └── Inputs/
```

## Environment variables

The game supports these env vars (set them in the launcher script):

| Variable | Default | Description |
|---|---|---|
| `MODE` | `PLAY` | Set to `EDITOR` to start in level editor mode |
| `MAP` | `1_1` | Map file to load (e.g. `1_2`, `1_3`) |

## Cross-compilation notes

Kotlin/Native can cross-compile from macOS to Linux x64. The Docker-based build handles this automatically by running the Gradle build inside a Linux container.

If you prefer to set up a local cross-compilation toolchain on macOS:
1. Install a Linux sysroot (e.g. via [cross-link](https://github.com/JetBrains/kotlin-cross-link))
2. Build raylib for Linux x64
3. Point `-I` and `-L` flags in `engine/build.gradle.kts` to the sysroot paths

## Troubleshooting

| Problem | Solution |
|---|---|
| `linkDebugExecutableLinuxX64` not found | Run on Linux or use the Docker build |
| `raylib not found` | Install raylib dev package for your Linux distro |
| Game doesn't appear in Ports | Ensure the `.sh` launcher is executable: `chmod +x /userdata/roms/ports/platformer-launcher.sh` |
| Game crashes on launch | Run it via SSH: `/userdata/roms/ports/platformer/platformer.kexe` to see error output |
