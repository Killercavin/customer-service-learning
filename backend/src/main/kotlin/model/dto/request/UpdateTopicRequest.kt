package app.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTopicRequest(
    val title: String? = null,
    val description: String? = null
)

