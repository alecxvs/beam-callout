package io.xvs.beam.callout

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import io.xvs.beam.callout.OAuth.Portal
import net.beadsproject.beads.core.AudioContext
import pro.beam.api.BeamAPI
import pro.beam.interactive.net.packet.Protocol
import pro.beam.interactive.robot.Robot
import pro.beam.interactive.robot.RobotBuilder
import java.nio.file.Files
import java.nio.file.Paths

// Load configuration
val mapper = {
    val out = ObjectMapper(YAMLFactory())
    out.registerModule(KotlinModule())
    out
}()

val configPath = Paths.get("config.yml")!!
val config: Config = {
    if (!Files.exists(configPath)) {
        val resource = Config::class.java.classLoader.getResourceAsStream("config.yml.default")
        Files.copy(resource, configPath)
        println("Warning: Config does not exist, copying from default")
    }
    println("Loading config from $configPath")
    Files.newBufferedReader(configPath).use { mapper.readValue(it, Config::class.java) }
}()

var robot: Robot? = null

fun main(args : Array<String>) {
    // Load Beam API
    if (config.auth_key == null) {
        println("Getting access for ${config.username}...")
        config.auth_key = Portal.requestAccess()
        Files.newBufferedWriter(configPath).use { mapper.writeValue(it, config) }
    }

    val key = config.auth_key
    val beam = BeamAPI(key)

    // Build Robot for handling interactions
    val robotFuture = RobotBuilder().username(config.username).channel(config.channel_id).build(beam, false)

    println("Starting up...")
    println("")
    // This is a rudimentary example of how to listen for a callback on a ListenableFuture.
    Futures.addCallback(robotFuture, object : FutureCallback<Robot> {
        override fun onSuccess(r: Robot?) {
            println("Connected to Beam (interactively)")
            // The robot is connected and ready to be used.
            robot = r
            robot!!.on(Protocol.Report::class.java, TactileListener.Master)
            Portal.webServer?.stop()
        }

        override fun onFailure(throwable: Throwable) {
            // There was an error connecting to the socket.
            System.err.println("Error experienced in connecting to Beam.")
            throwable.printStackTrace()
        }
    })
    println("Connecting to Beam")

    println("Creating AudioContext")
    println("== Audio Stats ==")
    val context = AudioContext()
    context.postAudioFormatInfo()
    context.start()
    println("")

    println("Initializing Player")
    val caller = Callout(context)
    caller.sounds.forEach {
        TactileListener.Master.onUpdate{
            val progress = Protocol.ProgressUpdate.TactileUpdate.newBuilder()
            progress.id = it.id
            progress.progress = it.percent / 0.3
            if (it.percent > 0.3) {
                if (caller.playSound(it.id)) {
                    progress.progress = 1.0
                    progress.cooldown = caller.sounds[it.id].cooldown.times(1000)
                    progress.fired = true
                }
            }
            progress.build()
        }
    }
}
