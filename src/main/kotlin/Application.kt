package app

import app.config.DatabaseConfig
import app.config.FlywayConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Init DB and run migrations
    DatabaseConfig.init()
    FlywayConfig.migrate()

    // JSON serialization with custom serializers
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    configureRouting()
}
