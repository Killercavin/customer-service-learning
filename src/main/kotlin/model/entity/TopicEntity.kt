package app.model.entity

import app.model.table.TopicTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TopicEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TopicEntity>(TopicTable)

    var title by TopicTable.title
    var description by TopicTable.description
    var createdAt by TopicTable.createdAt
    var updatedAt by TopicTable.updatedAt
}