package game


enum class Input {
    Mouse1,
    Mouse2,
    KeyboardS,
    KeyboardL,
    KeyboardP,
    KeyboardE,

    KeyboardW,
    KeyboardA,
    KeyboardD,
}

fun Input.isNewlyPressed() = GameState.isPressed.contains(this) && !GameState.wasPressed.contains(this)
fun Input.isPressed() = GameState.isPressed.contains(this)