package app.service

import app.model.entity.TopicEntity
import org.jetbrains.exposed.sql.transactions.transaction

class TopicService {
    fun getAllTopics(): List<TopicEntity> = transaction {
        TopicEntity.all().toList()
    }

    fun createTopic(title: String, description: String?): TopicEntity = transaction {
        TopicEntity.new {
            this.title = title
            this.description = description
        }
    }

    fun getTopicById(id: Int): TopicEntity? = transaction {
        TopicEntity.findById(id)
    }

    fun deleteTopic(id: Int): Boolean = transaction {
        TopicEntity.findById(id)?.delete() != null
    }
}
