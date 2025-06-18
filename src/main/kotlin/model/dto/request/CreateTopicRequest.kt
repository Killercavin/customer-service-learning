package app.model.dto.request

data class CreateTopicRequest(val title: String, val description: String? = null)
