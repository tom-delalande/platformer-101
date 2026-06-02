# Building and Deploying to Batocera (ARM64)

[Batocera](https://batocera.org) is a retro-gaming Linux distribution. This guide covers how to build the Linux ARM64 binary and deploy it as a Batocera **port**.

Target devices: Raspberry Pi 3B+, 4, 5, and other ARM64 SBCs running Batocera.

## Prerequisites

- **Batocera v39+** (ARM64) on your target machine
- SSH access (enable in System Settings → Network → SSH)

### For native ARM64 Linux builds (e.g. on the Pi itself)
- JDK 17+: `sudo apt install openjdk-17-jdk`
- raylib development libraries: `sudo apt install libraylib-dev libgl1-mesa-dev`

### For Docker builds (macOS / any platform)
- Docker Desktop with `linux/arm64` platform support (Apple Silicon Macs run ARM64 containers natively)

## Quick start

### 1. Build the Linux ARM64 binary

**On an ARM64 Linux system natively:**
```bash
./scripts/build-linux.sh release
```

**On macOS (using Docker):**
```bash
./scripts/build-linux.sh release
```

The script auto-detects the platform and uses Docker when not on native ARM64 Linux.

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
    ├── platformer.kexe      # The game binary
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

## Troubleshooting

| Problem | Solution |
|---|---|
| `linkReleaseExecutableLinuxArm64` not found | Run on ARM64 Linux or use the Docker build |
| `raylib not found` | Install raylib dev package for your ARM64 Linux distro |
| Game doesn't appear in Ports | Ensure the `.sh` launcher is executable: `chmod +x /userdata/roms/ports/platformer-launcher.sh` |
| Game crashes on launch | Run it via SSH: `/userdata/roms/ports/platformer/platformer.kexe` to see error output |
