@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.toKString
import platform.posix.getenv

actual fun getEnv(name: String): String? = getenv(name)?.toKString()
