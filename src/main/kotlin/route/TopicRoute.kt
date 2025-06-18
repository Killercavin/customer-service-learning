package app.route

import app.controller.TopicController
import app.service.TopicService
import io.ktor.server.routing.*

fun Route.topicRoutes() {
    val controller = TopicController(TopicService())

    route("/topics") {
        get { controller.getAll(call) }
        post { controller.create(call) }
        get("/{id}") { controller.getById(call) }
        delete("/{id}") { controller.delete(call) }
    }
}
