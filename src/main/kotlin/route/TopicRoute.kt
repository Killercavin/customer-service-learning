package app.route

import app.controller.TopicController
import app.service.TopicService
import io.ktor.server.routing.*

fun Route.topicRoutes() {
    val controller = TopicController(TopicService())

    route("/topics") {
        get { controller.getAll(call) }
        post { controller.create(call) }

        // New search endpoint
        get("/search") { controller.search(call) }

        route("/{id}") {
            get { controller.getById(call) }
            delete { controller.delete(call) }

            // New update endpoint
            put { controller.update(call) }
            // patch {  controller.update(call) }
        }
    }
}
