package io.xvs.beam.callout

import net.beadsproject.beads.core.AudioContext
import pro.beam.interactive.net.packet.Protocol

class Callout(context: AudioContext) {
    val sounds: List<Sound> = config.sounds.map { Sound(context, it.path, it.cooldown) }

    fun playSound(id: Int): Boolean {
        val played = sounds[id].play()
        if (played) {
            config.groups.forEach { group ->
                if (group.sounds.contains(id)) {
                    group.sounds.forEach {
                        if (it != id) {
                            this.sounds[it].cooldownFor(group.cooldown.times(1000))
//                            currentReport.addTactile(
//                                    Protocol.ProgressUpdate.TactileUpdate.newBuilder().setId(it).setCooldown(group.cooldown)
//                            )
                        }
                    }
                }
            }
        }
        return played
    }
}