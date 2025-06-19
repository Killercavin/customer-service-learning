package app

import app.route.topicRoutes
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {

        routing {
            staticResources("/", "static") // Serve static files (HTML, CSS, JS)

            route("api") {
                topicRoutes()
            }
        }
    }
}
