package com.example.teamroomback.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User?,

    @Column(name = "content", columnDefinition = "text")
    var content: String? = null,

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "chat_message_type")
    var type: ChatMessageType = ChatMessageType.USER_MESSAGE,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message")
    val replyToMessage: ChatMessage? = null,

    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    val sentAt: LocalDateTime? = null,

    @Column(name = "edited_at")
    var editedAt: LocalDateTime? = null,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    val relatedEntities: List<ChatMessageRelatedEntity> = listOf(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    val media: List<ChatMessageMedia> = listOf(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reactions: List<MessageReaction> = listOf()
)

enum class ChatMessageType {
    USER_JOINED_TO_CHAT,
    USER_LEFT_FROM_CHAT,
    USER_MESSAGE,
    COURSE_OPENED,
    COURSE_CLOSED,
    MATERIAL_CREATED,
    MATERIAL_UPDATED,
    MATERIAL_DELETED,
    ASSIGNMENT_CREATED,
    ASSIGNMENT_UPDATED,
    ASSIGNMENT_DELETED,
    ASSIGNMENT_DEADLINE_IN_24HR,
    ASSIGNMENT_DEADLINE_ENDED,
    CONFERENCE_STARTED,
    CONFERENCE_ENDED
}