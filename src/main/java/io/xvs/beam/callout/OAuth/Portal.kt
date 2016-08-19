package io.xvs.beam.callout.OAuth

import com.google.api.client.auth.oauth2.BrowserClientRequestUrl
import org.jetbrains.ktor.application.call
import org.jetbrains.ktor.host.ApplicationHost
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.respondText
import org.jetbrains.ktor.http.url
import org.jetbrains.ktor.jetty.embeddedJettyServer
import org.jetbrains.ktor.routing.get
import java.awt.Desktop
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object Portal {
    val chronoTrigger: CountDownLatch = CountDownLatch(1)
    var oauthToken: String? = null
    var webServer: ApplicationHost? = null

    fun startWebServer() {
        var attempts = 0
        println("Starting micro web server...")
        while (webServer == null) {
            val port = 20000 + Random(System.currentTimeMillis()).nextInt(10000)
            try {
                webServer = embeddedJettyServer(port) {
                    get("/") {
                        call.respondText( ContentType.Text.Html,
                                Portal::class.java.classLoader.getResourceAsStream("redirect.html")
                                        .bufferedReader()
                                        .readText()
                        )
                    }
                    get("/oauth") {
                        Portal.oauthToken = call.request.parameters["access_token"]
                        chronoTrigger.countDown()
                        call.respondText( ContentType.Text.Html,
                                Portal::class.java.classLoader.getResourceAsStream("oauth.html")
                                        .bufferedReader()
                                        .readText()
                        )
                    }
                }
            } catch (e: Exception) {
                attempts++
                if (attempts > 5)
                    throw e
            }
        }
        webServer!!.start()
    }

//    fun (val port: Int) {
//        init {
//            addRoute("/oauth", TokenReceiver::class.java)
//            start()
//        }
////
//        class TokenReceiver: UriResponder {
//            override fun put(p0: UriResource?, p1: MutableMap<String, String>?, p2: IHTTPSession?): Response {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "", "")
//            }
//
//            override fun other(p0: String?, p1: UriResource?, p2: MutableMap<String, String>?, p3: IHTTPSession?): Response {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "", "")
//            }
//
//            override fun post(p0: UriResource?, p1: MutableMap<String, String>?, p2: IHTTPSession?): Response {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "", "")
//            }
//
//            override fun delete(p0: UriResource?, p1: MutableMap<String, String>?, p2: IHTTPSession?): Response {
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "", "")
//            }
//
//            override fun get(uriResource: UriResource, urlParams: MutableMap<String, String>, session: IHTTPSession?): Response {
//                println(urlParams)
//                println(Regex("(?<=#access_token=)(\\w+)"))
//                println(Regex("(?<=#access_token=)(\\w+)").find(uriResource.uri))
//                Portal.oauthToken = Regex("(?<=#access_token=)(\\w+)").matchEntire(uriResource.uri).toString()
//                println("Access token: ${Portal.oauthToken}")
//                Portal.chronoTrigger.countDown()
//                return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "You may close this window")
//            }
//
//        }
//    }

    fun requestAccess(): String {
        startWebServer()
        val url = BrowserClientRequestUrl("https://beam.pro/oauth/authorize", "77181b02818e83292571af3746260787ed855a00e9b4a363")
                .setRedirectUri("http://localhost:${webServer!!.hostConfig.port}")
                .setScopes(listOf("interactive:robot:self"))
                .build()

        if(Desktop.isDesktopSupported())
            Desktop.getDesktop().browse(URI(url))

        Portal.chronoTrigger.await(60, TimeUnit.SECONDS)
        return Portal.oauthToken!!
    }
}