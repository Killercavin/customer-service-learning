package app.model.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class TopicResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)

