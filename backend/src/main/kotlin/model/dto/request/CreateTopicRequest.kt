package app.model.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateTopicRequest(val title: String, val description: String? = null)
