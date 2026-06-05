@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package game

@JsFun("(p) => globalThis.platform_readTextFile(p)")
external fun platform_readTextFile(p: String): String?

@JsFun("(p, t) => globalThis.platform_writeTextFile(p, t)")
external fun platform_writeTextFile(p: String, t: String)

actual fun readTextFile(path: String): String? = platform_readTextFile(path)

actual fun writeTextFile(path: String, text: String) = platform_writeTextFile(path, text)
