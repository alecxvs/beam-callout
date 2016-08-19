package io.xvs.beam.callout

data class Config(val username: String,
             val channel_id: Int,
             var default_cooldown: Int = 30) {

    var auth_key: String? = null

    data class Group(var sounds: Set<Int>, var cooldown: Int)
    val groups = listOf<Group>()

    data class Sound(var path: String, var cooldown: Int?)
    val sounds = listOf<Config.Sound>()
}