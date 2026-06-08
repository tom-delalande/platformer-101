import game.Audio
import game.Sprite
import kotlinx.cinterop.*
import sdl.*
import sdl_image.*

@OptIn(ExperimentalForeignApi::class)
data class SoundData(
    val audioBuf: CPointer<Uint8Var>?,
    val audioLen: UInt,
)

@OptIn(ExperimentalForeignApi::class)
object Assets {
    private val cachedTextures = mutableMapOf<String, CPointer<sdl.SDL_Texture>>()

    val textures by lazy {
        println("RESOLVING TEXTURES")
        Sprite.sprites.map { (_, sprite) ->
            sprite to cachedTextures.getOrPut(sprite.texture) {
                val texture = IMG_LoadTexture(Engine.renderer, sprite.texture)!!
                SDL_SetTextureScaleMode(texture.reinterpret(), SDL_SCALEMODE_NEAREST)
                texture.reinterpret()
            }
        }
    }

    val sounds by lazy {
        Audio.audio.map { (name, clip) ->
            memScoped {
                val dstSpec = alloc<SDL_AudioSpec>().apply {
                    format = SDL_AUDIO_S16
                    channels = AUDIO_CHANNELS
                    freq = AUDIO_FREQ
                }
                val buf = alloc<CPointerVar<Uint8Var>>()
                val len = alloc<UIntVar>()
                val success = SDL_LoadWAV(clip.file, dstSpec.ptr, buf.ptr, len.ptr)
                if (success) {
                    name to SoundData(audioBuf = buf.value, audioLen = len.value)
                } else {
                    name to SoundData(audioBuf = null, audioLen = 0u)
                }
            }
        }
    }

    fun fromSprite(sprite: Sprite.Sprite): CPointer<sdl.SDL_Texture> =
        textures.find { it.first == sprite }!!.second

    fun fromClip(sound: Audio.Clip): SoundData {
        val idx = Audio.audio.values.indexOf(sound)
        return sounds[idx].second
    }
}

private const val AUDIO_CHANNELS = 2
private const val AUDIO_FREQ = 44100
