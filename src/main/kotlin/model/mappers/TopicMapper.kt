package app.model.mappers

import app.model.dto.response.TopicResponse
import app.model.entity.TopicEntity

fun TopicEntity.toTopicResponse(): TopicResponse = TopicResponse(
    id = this.id.value,
    title = this.title,
    description = this.description,
    createdAt = this.createdAt.toString(),
    updatedAt = this.updatedAt.toString()
)