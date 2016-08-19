package io.xvs.beam.callout

import net.beadsproject.beads.core.AudioContext
import java.util.*

class Callout(context: AudioContext) {
    val sounds: List<Sound> = config.sounds.map { Sound(context, it.path, it.cooldown ?: config.default_cooldown) }

    fun playSound(id: Int): Boolean {
        return sounds[id].play()
    }
}