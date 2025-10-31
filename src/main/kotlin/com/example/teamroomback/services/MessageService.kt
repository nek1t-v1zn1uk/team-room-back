package com.example.teamroomback.services

import com.example.teamroomback.dtos.ChatMessageDto
import com.example.teamroomback.dtos.ChatMessageMediaDto
import com.example.teamroomback.dtos.ChatMessageRelatedEntityDto
import com.example.teamroomback.dtos.SendMessageRequest
import com.example.teamroomback.entities.*
import com.example.teamroomback.repositories.ChatMessageRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketNotificationService: WebSocketNotificationService
) {

    @Transactional
    fun postMessage(chatId: Long, username: String, request: SendMessageRequest): ChatMessage {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val user = userRepository.findByUsernameValue(username)
            ?: throw EntityNotFoundException("User with username '$username' not found")

        val replyToMessage = request.replyToMessageId?.let {
            chatMessageRepository.findById(it)
                .orElseThrow { EntityNotFoundException("Replying to message with id $it not found") }
        }

        val message = ChatMessage(
            chat = chat,
            user = user,
            content = request.content,
            replyToMessage = replyToMessage
        )

        val relatedEntities = request.relatedEntities.map {
            ChatMessageRelatedEntity(
                message = message,
                relatedEntityType = it.relatedEntityType,
                relatedEntityId = it.relatedEntityId
            )
        }

        val media = request.media.map {
            ChatMessageMedia(
                message = message,
                fileUrl = it.fileUrl,
                fileName = it.fileName,
                fileType = it.fileType,
                fileSizeBytes = it.fileSizeBytes
            )
        }

        message.relatedEntities = relatedEntities.toMutableList()
        message.media = media.toMutableList()

        val savedMessage = chatMessageRepository.save(message)

        val messageDto = savedMessage.toChatMessageDto()
        webSocketNotificationService.sendChatMessage(chatId, messageDto)

        return savedMessage
    }
}