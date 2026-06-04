import game.Audio
import game.Sprite

object Assets {
    private val cachedTextures = mutableMapOf<String, Texture>()

    val textures by lazy {
        Sprite.sprites.map {
            it.value to cachedTextures.getOrPut(it.value.texture) { Platform.loadTexture(it.value.texture) }
        }
    }

    val sounds by lazy {
        Audio.audio.map { it.value to Platform.loadSound(it.value.file) }
    }

    fun fromSprite(sprite: Sprite.Sprite) = textures.find { it.first == sprite }!!.second
    fun fromClip(sound: Audio.Clip) = sounds.find { it.first == sound }!!.second
}
