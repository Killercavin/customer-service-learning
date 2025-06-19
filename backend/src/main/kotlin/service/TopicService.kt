package app.service

import app.model.entity.TopicEntity
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
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
        TopicEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    fun updateTopic(id: Int, title: String?, description: String?): TopicEntity? = transaction {
        val topic = TopicEntity.findById(id)
        topic?.apply {
            title?.let { this.title = it }
            description?.let { this.description = it }
        }
    }

    fun searchTopics(query: String): List<TopicEntity> = transaction {
        TopicEntity.find {
            app.model.table.TopicTable.title like "%$query%"
        }.toList()
    }
}