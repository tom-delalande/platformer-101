(function () {
  "use strict"

  let canvas = null
  let ctx = null

  // Texture store
  let nextTexId = 1
  const textures = {}

  // Sound store
  let nextSndId = 1
  const sounds = {}

  // Input state
  const keyboardState = {}
  const mouseButtonState = {}
  let mouseX = 0
  let mouseY = 0

  // Key-code mapping: raylib (ASCII) -> event.code
  const keyToCode = {
    87: "KeyW", 65: "KeyA", 83: "KeyS", 68: "KeyD",
    69: "KeyE", 76: "KeyL", 80: "KeyP",
  }

  // Gamepad button mapping: raylib index -> browser index
  const gpadBtn = {
    4: 12, // D-pad up
    5: 13, // D-pad down
    6: 14, // D-pad left
    7: 15, // D-pad right
    8: 16, // Guide / middle
  }

  // Mouse button mapping: raylib -> browser
  const mouseMap = { 1: 2 }

  // ── Window ────────────────────────────────────────────
  globalThis.platform_initWindow = function (w, h, title) {
    canvas = document.createElement("canvas")
    canvas.width = w
    canvas.height = h
    canvas.style.display = "block"
    canvas.style.margin = "0 auto"
    canvas.tabIndex = 0
    document.body.style.margin = "0"
    document.body.style.overflow = "hidden"
    document.body.style.background = "#000"
    document.title = title
    document.body.appendChild(canvas)
    ctx = canvas.getContext("2d")

    document.addEventListener("keydown", function (e) {
      keyboardState[e.code] = true
      if (e.code.startsWith("Key") || e.code === "Escape") e.preventDefault()
    })
    document.addEventListener("keyup", function (e) {
      keyboardState[e.code] = false
    })
    canvas.addEventListener("mousedown", function (e) {
      mouseButtonState[e.button] = true
    })
    canvas.addEventListener("mouseup", function (e) {
      mouseButtonState[e.button] = false
    })
    canvas.addEventListener("mousemove", function (e) {
      var r = canvas.getBoundingClientRect()
      mouseX = e.clientX - r.left
      mouseY = e.clientY - r.top
    })
  }

  globalThis.platform_windowShouldClose = function () { return false }
  globalThis.platform_closeWindow = function () {
    if (canvas && canvas.parentNode) canvas.parentNode.removeChild(canvas)
  }

  globalThis.platform_setWindowSize = function (w, h) {
    if (!canvas) return
    canvas.width = w
    canvas.height = h
    canvas.style.width = w + "px"
    canvas.style.height = h + "px"
  }

  globalThis.platform_setWindowPosition = function (x, y) {
    if (!canvas) return
    canvas.style.position = "absolute"
    canvas.style.left = x + "px"
    canvas.style.top = y + "px"
  }

  globalThis.platform_setConfigFlags = function () {}
  globalThis.platform_setTargetFPS = function () {}

  // ── Drawing ──────────────────────────────────────────
  globalThis.platform_beginDrawing = function () { ctx.save() }
  globalThis.platform_endDrawing = function () { ctx.restore() }

  globalThis.platform_clearBackground = function (r, g, b, a) {
    ctx.fillStyle = "rgba(" + r + "," + g + "," + b + "," + (a / 255) + ")"
    ctx.fillRect(0, 0, canvas.width, canvas.height)
  }

  globalThis.platform_drawRectangle = function (x, y, w, h, r, g, b, a) {
    ctx.fillStyle = "rgba(" + r + "," + g + "," + b + "," + (a / 255) + ")"
    ctx.fillRect(x, y, w, h)
  }

  globalThis.platform_drawText = function (text, x, y, fontSize, r, g, b, a) {
    if (text == null) return
    ctx.fillStyle = "rgba(" + r + "," + g + "," + b + "," + (a / 255) + ")"
    ctx.font = "bold " + fontSize + "px monospace"
    ctx.textBaseline = "top"
    ctx.fillText(text, x, y)
  }

  globalThis.platform_drawTexturePro = function (
    texId, srcX, srcY, srcW, srcH,
    dstX, dstY, dstW, dstH,
    originX, originY, rotation,
    tintR, tintG, tintB, tintA,
  ) {
    var img = textures[texId]
    if (!img || !img.complete) return

    ctx.save()

    var flipX = srcW < 0 ? -1 : 1
    ctx.translate(dstX + originX, dstY + originY)
    ctx.rotate(rotation)
    ctx.scale(flipX, 1)

    if (tintA < 255) ctx.globalAlpha = (ctx.globalAlpha || 1) * (tintA / 255)

    ctx.drawImage(
      img,
      Math.abs(srcX), Math.abs(srcY), Math.abs(srcW), Math.abs(srcH),
      -originX, -originY, dstW, dstH,
    )

    ctx.restore()
  }

  // ── Textures ─────────────────────────────────────────
  globalThis.platform_loadTexture = function (path) {
    var id = nextTexId++
    var img = new Image()
    img.onload = function () { textures[id] = img }
    textures[id] = img
    img.src = path
    return id
  }

  globalThis.platform_unloadTexture = function (id) { delete textures[id] }

  // ── Audio ────────────────────────────────────────────
  globalThis.platform_initAudio = function () {}

  globalThis.platform_loadSound = function (path) {
    var id = nextSndId++
    var a = new Audio(path)
    a.preload = "auto"
    sounds[id] = a
    return id
  }

  globalThis.platform_playSound = function (id) {
    var a = sounds[id]
    if (!a) return
    a.currentTime = 0
    a.play().catch(function () {})
  }

  globalThis.platform_isSoundPlaying = function (id) {
    var a = sounds[id]
    return a ? !a.paused && !a.ended : false
  }

  globalThis.platform_unloadSound = function (id) { delete sounds[id] }

  // ── Input ────────────────────────────────────────────
  globalThis.platform_isKeyDown = function (keyCode) {
    var code = keyToCode[keyCode]
    return code ? !!keyboardState[code] : false
  }

  globalThis.platform_isMouseButtonDown = function (btn) {
    var b = mouseMap[btn] !== undefined ? mouseMap[btn] : btn
    return !!mouseButtonState[b]
  }

  globalThis.platform_getMouseX = function () { return mouseX | 0 }
  globalThis.platform_getMouseY = function () { return mouseY | 0 }

  globalThis.platform_isGamepadAvailable = function (gamepad) {
    var gps = navigator.getGamepads()
    return gps[gamepad] != null
  }

  globalThis.platform_isGamepadButtonDown = function (gamepad, btn) {
    var gp = navigator.getGamepads()[gamepad]
    if (!gp) return false
    var b = gpadBtn[btn] !== undefined ? gpadBtn[btn] : btn
    return gp.buttons[b] ? gp.buttons[b].pressed : false
  }

  globalThis.platform_getGamepadAxis = function (gamepad, axis) {
    var gp = navigator.getGamepads()[gamepad]
    if (!gp || !gp.axes[axis]) return 0
    return gp.axes[axis]
  }

  // ── Monitor ──────────────────────────────────────────
  globalThis.platform_getCurrentMonitor = function () { return 0 }
  globalThis.platform_getMonitorWidth = function () { return window.screen.width }
  globalThis.platform_getMonitorHeight = function () { return window.screen.height }

  // ── File I/O ─────────────────────────────────────────
  globalThis.platform_readTextFile = function (path) {
    var cached = localStorage.getItem("map_" + path)
    if (cached !== null) return cached
    try {
      var xhr = new XMLHttpRequest()
      xhr.open("GET", path, false)
      xhr.overrideMimeType("text/plain")
      xhr.send()
      if (xhr.status === 200 || xhr.status === 0) return xhr.responseText
    } catch (e) { console.warn("readTextFile error:", path, e) }
    return null
  }

  globalThis.platform_writeTextFile = function (path, text) {
    try { localStorage.setItem("map_" + path, text) }
    catch (e) { console.warn("writeTextFile error:", path, e) }
  }

  // ── Environment ──────────────────────────────────────
  globalThis.platform_getenv = function (name) {
    var params = new URLSearchParams(window.location.search)
    return params.get(name)
  }
})()
