# Strawberry Platformer

A 2D platformer built with Kotlin and SDL3 to help people new to gaming learn the ropes.

Collect all the Strawberries in each level and reach the flag to advance. Features 9 levels and support for keyboard and controller input.

![Screenshot](screenshot.png)

## Controls

| Action | Keyboard | Controller |
|--------|----------|------------|
| Move | A / D | D-pad or left stick |
| Jump | W | A button |
| Quit | Escape | - |

## Run on iOS (Simulator)

Requires Xcode.

```sh
# Build SDL3 and link the game
./scripts/package-ios.sh

# Install & launch on simulator
xcrun simctl install booted build/iosApp/Platformer101.app
xcrun simctl launch booted com.platformer-101.app
```

Avoid using `./scripts/package-ios.sh` with the `release` config until you've tested with `debug` first. The debug build uses the default config.

## Run on Browser (Wasm)

### Dev server (auto-reload)

```sh
./gradlew :engine:wasmJsBrowserDevelopmentRun
```

Open http://localhost:8080 in your browser.

### Production build

```sh
./gradlew :engine:wasmJsBrowserProductionWebpack
```

Serve `engine/build/dist/wasmJs/productionExecutable/` with any HTTP server.

### Query params

- `?MODE=EDITOR` — launch in editor mode
- `?MODE=EDITOR&MAP=1_2` — edit a specific map

## Attributions

- SFX — [kronbits.itch.io/freesfx](https://kronbits.itch.io/freesfx)
- Sprites
  - Inputs — [juliocacko.itch.io/free-input-prompts](https://juliocacko.itch.io/free-input-prompts)
  - Game — [pixelfrog-assets.itch.io/pixel-adventure-1](https://pixelfrog-assets.itch.io/pixel-adventure-1)
