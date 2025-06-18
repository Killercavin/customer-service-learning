package app.model.mappers

import app.model.dto.response.TopicResponse
import app.model.entity.TopicEntity

fun TopicEntity.toResponse(): TopicResponse = TopicResponse(
    id = id.value,
    title = title,
    description = description,
    createdAt = createdAt.toString()
)