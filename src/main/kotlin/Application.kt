package com.musicapi

import com.musicapi.routes.artistRoutes
import com.musicapi.routes.trackRoutes
import com.musicapi.database.DatabaseFactory
import com.musicapi.routes.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.request.path
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Inicializar base de datos
    DatabaseFactory.init(environment.config)

    // Configurar plugins
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(
                text = "500: ${cause.message}",
                status = HttpStatusCode.InternalServerError
            )
        }
    }

    // Configurar rutas
    routing {
        get("/") {
            call.respond(
                mapOf(
                    "message" to "API de Catálogo Musical",
                    "version" to "1.0.0",
                    "endpoints" to mapOf(
                        "artistas" to "/artistas",
                        "albumes" to "/albumes",
                        "tracks" to "/tracks"
                    )
                )
            )
        }

        artistRoutes()
        albumRoutes()
        trackRoutes()
    }
}