package app.controller

import app.model.dto.request.CreateTopicRequest
import app.model.mappers.toResponse
import app.service.TopicService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TopicController(private val service: TopicService) {
    suspend fun getAll(call: ApplicationCall) {
        val topics = service.getAllTopics()
        call.respond(topics.map { it.toResponse() })
    }

    suspend fun create(call: ApplicationCall) {
        val payload = call.receive<CreateTopicRequest>()
        val topic = service.createTopic(payload.title, payload.description)
        call.respond(topic.toResponse())
    }

    suspend fun getById(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
        val topic = id?.let { service.getTopicById(it) }
        if (topic != null) call.respond(topic.toResponse())
        else call.respondText("Topic not found", status = HttpStatusCode.NotFound)
    }

    suspend fun delete(call: ApplicationCall) {
        val id = call.parameters["id"]?.toIntOrNull()
        val deleted = id?.let { service.deleteTopic(it) } ?: false
        call.respondText(if (deleted) "Deleted" else "Not found")
    }
}
