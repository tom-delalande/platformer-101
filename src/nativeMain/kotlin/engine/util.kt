package engine

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import raylib.Color

@OptIn(ExperimentalForeignApi::class)
fun color(r: Int, g: Int, b: Int, a: Int = 255) = cValue<Color> {
    this.r = r.toUByte()
    this.g = g.toUByte()
    this.b = b.toUByte()
    this.a = a.toUByte()
}
