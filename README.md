# Strawberry Platformer

A 2D platformer built with Kotlin and Raylib to help people new to gaming learn the ropes.

Collect all the Strawberries in each level and reach the flag to advance. Features 9 levels and support for keyboard and controller input.

![Screenshot](screenshot.png)

## Controls

| Action | Keyboard | Controller |
|--------|----------|------------|
| Move | A / D | D-pad or left stick |
| Jump | W | A button |
| Quit | Escape | - |

## Run on Desktop (JVM)

```sh
./gradlew :engine:runJvm
```

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
