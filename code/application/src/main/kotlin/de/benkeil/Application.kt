package de.benkeil

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
  val greeting = System.getenv("APPLICATION_GREETING") ?: "world"
  embeddedServer(Netty, 8080) {
        routing { get("/") { call.respondText("Hello, $greeting!", ContentType.Text.Plain) } }
      }
      .start(wait = true)
}
