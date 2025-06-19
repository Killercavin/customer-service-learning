package app

import app.config.DatabaseConfig
import app.config.FlywayConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    // Configure CORS to allow cross-origin requests from example localhost:8080 or any server url
    install(CORS) {
        allowHost("localhost:8080", schemes = listOf("http"))
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
    }

    // Init DB and run migrations
    DatabaseConfig.init()
    FlywayConfig.migrate()

    // JSON serialization with custom serializers
    configureSerialization()

    // Routing
    configureRouting()
}
