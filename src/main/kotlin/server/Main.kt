package server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("server.Main")

fun main(args: Array<String>) {
    log.debug("Program arguments: ${args.joinToString()}")

    embeddedServer(Netty, port = 4207) {
        serverApplication(log)
    }.start(wait = true)
}

fun Application.serverApplication(log: Logger) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            log.error("Server Error", cause)
            call.respondText(
                text = "500: $cause",
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    routing {
        get("/") {
            call.respondText("Server Responding")
        }
    }
}