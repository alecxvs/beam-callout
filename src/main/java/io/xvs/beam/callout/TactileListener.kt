package io.xvs.beam.callout

import pro.beam.interactive.event.EventListener
import pro.beam.interactive.net.packet.Protocol

class TactileListener(val action: (InteractionSummary) -> Protocol.ProgressUpdate.TactileUpdate) {
    companion object Master : EventListener<Protocol.Report> {
        private val listeners = mutableListOf<TactileListener>()
        override fun handle(report: Protocol.Report) {
            currentReport = Protocol.ProgressUpdate.newBuilder()
            if (report.users.connected > 0) {
                for (tactile in report.tactileList) {
                    if (tactile.hasHolding()) {
                        currentReport.addTactile(listeners.getOrNull(tactile.id)?.update(report, tactile))
                        currentReportModified = true
                    }
                }
            }
            if (currentReportModified) {
                robot!!.write(currentReport.build())
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