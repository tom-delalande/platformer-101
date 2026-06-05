@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

@JsFun("(n) => globalThis.platform_getenv(n)")
external fun platform_getenv(n: String): String?

actual fun getenv(name: String): String? = platform_getenv(name)
