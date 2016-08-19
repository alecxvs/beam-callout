package io.xvs.beam.callout

data class InteractionReport(
    val id: Int,
    var percent: Double,
    var cooldown: Int? = null
)