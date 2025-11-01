package com.example.teamroomback.entities

import com.example.teamroomback.dtos.ChatMessageDto
import com.example.teamroomback.dtos.ChatMessageMediaDto
import com.example.teamroomback.dtos.ChatMessageRelatedEntityDto
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
    var relatedEntities: MutableList<ChatMessageRelatedEntity> = mutableListOf(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    var media: MutableList<ChatMessageMedia> = mutableListOf(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reactions: List<MessageReaction> = listOf()
) {
    fun toChatMessageDto(): ChatMessageDto {
        return ChatMessageDto(
            id = id!!,
            chatId = chat.id!!,
            username = user?.username,
            content = content,
            type = type,
            replyToMessageId = replyToMessage?.id,
            sentAt = sentAt!!,
            editedAt = editedAt,
            isDeleted = isDeleted,
            relatedEntities = relatedEntities.map {
                ChatMessageRelatedEntityDto(it.relatedEntityType, it.relatedEntityId)
            },
            media = media.map {
                ChatMessageMediaDto(it.fileUrl, it.fileName, it.fileType, it.fileSizeBytes)
            }
        )
    }
}

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