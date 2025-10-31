package com.example.teamroomback.dtos

import com.example.teamroomback.entities.ChatMessageRelatedEntityType
import com.example.teamroomback.entities.ChatMessageType
import java.time.LocalDateTime

data class ChatMessageRelatedEntityDto(
    val relatedEntityType: ChatMessageRelatedEntityType,
    val relatedEntityId: Long
)

data class ChatMessageMediaDto(
    val fileUrl: String,
    val fileName: String? = null,
    val fileType: String? = null,
    val fileSizeBytes: Int? = null
)

data class SendMessageRequest(
    val content: String? = null,
    val replyToMessageId: Long? = null,
    val relatedEntities: List<ChatMessageRelatedEntityDto> = listOf(),
    val media: List<ChatMessageMediaDto> = listOf()
)

data class ChatMessageDto(
    val id: Long,
    val chatId: Long,
    val username: String?,
    val content: String?,
    val type: ChatMessageType,
    val replyToMessageId: Long?,
    val sentAt: LocalDateTime,
    val editedAt: LocalDateTime?,
    val isDeleted: Boolean,
    val relatedEntities: List<ChatMessageRelatedEntityDto> = listOf(),
    val media: List<ChatMessageMediaDto> = listOf()
)
