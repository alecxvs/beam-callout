package io.xvs.beam.callout

import pro.beam.interactive.event.EventListener
import pro.beam.interactive.net.packet.Protocol

class TactileListener(val action: (InteractionSummary) -> Protocol.ProgressUpdate.TactileUpdate) {
    companion object Master : EventListener<Protocol.Report> {
        private val listeners = mutableListOf<TactileListener>()
        override fun handle(report: Protocol.Report) {
            if (report.users.connected > 0) {
                val progress = Protocol.ProgressUpdate.newBuilder()
                for (tactile in report.tactileList) {
                    if (tactile.hasHolding()) {
                        progress.addTactile(listeners.getOrNull(tactile.id)?.update(report, tactile))
                    }
                }
                robot!!.write(progress.build())
            }
        }

        fun onUpdate(action: (InteractionSummary) -> Protocol.ProgressUpdate.TactileUpdate) {
            listeners.add(TactileListener(action))
        }
    }

    fun update(report: Protocol.Report, tactile: Protocol.Report.TactileInfo): Protocol.ProgressUpdate.TactileUpdate {
        return action(InteractionSummary(tactile.id, tactile.holding / report.users.connected))
    }
}