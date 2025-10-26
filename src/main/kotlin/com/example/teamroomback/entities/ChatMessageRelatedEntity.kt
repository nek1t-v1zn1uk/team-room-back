package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "chat_message_related_entity", uniqueConstraints = [
    UniqueConstraint(name = "msg_related_entity_key", columnNames = ["message_id", "related_entity_type", "related_entity_id"])
])
data class ChatMessageRelatedEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: ChatMessage,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "related_entity_type", nullable = false, columnDefinition = "chat_message_related_entity_type")
    val relatedEntityType: ChatMessageRelatedEntityType,

    @Column(name = "related_entity_id", nullable = false)
    val relatedEntityId: Long
)

enum class ChatMessageRelatedEntityType {
    MATERIAL,
    ASSIGNMENT,
    CONFERENCE
}