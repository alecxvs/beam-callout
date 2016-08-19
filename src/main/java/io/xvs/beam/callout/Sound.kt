package io.xvs.beam.callout

import net.beadsproject.beads.core.AudioContext
import net.beadsproject.beads.data.SampleManager
import net.beadsproject.beads.ugens.SamplePlayer
import java.util.*

class Sound(val context: AudioContext, val path: String, val cooldown: Int) {
    private var cooldownUntil = Date()

    fun play(): Boolean {
        if (Date().after(cooldownUntil)) {
            println("Playing sound $path")
            context.out.addInput(SamplePlayer(context, SampleManager.sample(path)))

            cooldownFor(cooldown.times(1000))
            return true
        }
        return false
    }

    fun cooldownFor(milliseconds: Int) {
        println("Cooling down for $milliseconds ms")
        cooldownUntil = Date(Date().time + milliseconds)
    }
}