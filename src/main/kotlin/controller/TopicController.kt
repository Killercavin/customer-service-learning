package app.controller

import app.model.dto.request.CreateTopicRequest
import app.model.dto.request.UpdateTopicRequest
import app.model.mappers.toTopicResponse
import app.service.TopicService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

class TopicController(private val service: TopicService) {

    private val logger = LoggerFactory.getLogger(TopicController::class.java)

    suspend fun getAll(call: ApplicationCall) {
        try {
            val topics = service.getAllTopics()
            call.respond(topics.map { it.toTopicResponse() })
        } catch (e: Exception) {
            logger.error("Failed to fetch topics: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error fetching topics")
        }
    }

    suspend fun create(call: ApplicationCall) {
        try {
            val payload = call.receive<CreateTopicRequest>()
            if (payload.title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Title must not be empty")
                return
            }

            val topic = service.createTopic(payload.title, payload.description)
            call.respond(HttpStatusCode.Created, topic.toTopicResponse())
        } catch (e: Exception) {
            logger.warn("JSON parsing or creation error: ${e.message}", e)
            call.respond(
                HttpStatusCode.BadRequest,
                "Invalid request body. Ensure JSON has 'title' and optional 'description'."
            )
        }
    }

    suspend fun getById(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return
            }

            val topic = service.getTopicById(id)
            if (topic != null) {
                call.respond(HttpStatusCode.OK, topic.toTopicResponse())
            } else {
                call.respond(HttpStatusCode.NotFound, "Topic not found")
            }
        } catch (e: Exception) {
            logger.error("Failed to retrieve topic: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error fetching topic")
        }
    }

    suspend fun delete(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return
            }

            val deleted = service.deleteTopic(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Topic not found")
            }
        } catch (e: Exception) {
            logger.error("Failed to delete topic: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error deleting topic")
        }
    }

    suspend fun update(call: ApplicationCall) {
        try {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                return
            }

            val payload = call.receive<UpdateTopicRequest>()
            val updated = service.updateTopic(id, payload.title, payload.description)

            if (updated != null) {
                call.respond(HttpStatusCode.OK, updated.toTopicResponse())
            } else {
                call.respond(HttpStatusCode.NotFound, "Topic not found")
            }

        } catch (e: Exception) {
            logger.error("Failed to update topic: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error updating topic")
        }
    }

    suspend fun search(call: ApplicationCall) {
        try {
            val query = call.request.queryParameters["q"]
            if (query.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Query parameter 'q' is required")
                return
            }

            val results = service.searchTopics(query)
            call.respond(results.map { it.toTopicResponse() })
        } catch (e: Exception) {
            logger.error("Failed to search topics: ${e.message}", e)
            call.respond(HttpStatusCode.InternalServerError, "Error searching topics")
        }
    }
}