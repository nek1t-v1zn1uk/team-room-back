package com.example.teamroomback.services

import com.example.teamroomback.dtos.DeleteMessageDto
import com.example.teamroomback.dtos.DeleteMessageRequest
import com.example.teamroomback.dtos.EditMessageRequest
import com.example.teamroomback.dtos.MessageReactionDto
import com.example.teamroomback.dtos.ReactionRequest
import com.example.teamroomback.dtos.SendMessageRequest
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.*
import com.example.teamroomback.repositories.ChatMemberRepository
import com.example.teamroomback.repositories.ChatMessageRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.repositories.MessageReactionRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class MessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val chatRepository: ChatRepository,
    private val chatMemberRepository: ChatMemberRepository,
    private val userRepository: UserRepository,
    private val webSocketNotificationService: WebSocketNotificationService,
    private val messageReactionRepository: MessageReactionRepository
) {

    @Transactional
    fun postMessage(chatId: Long, username: String, request: SendMessageRequest): ChatMessage {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val user = userRepository.findByUsernameValue(username)
            ?: throw EntityNotFoundException("User with username '$username' not found")

        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of the chat") }

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
        webSocketNotificationService.sendChatMessage(chatId, WebSocketMessageType.USER_MESSAGE, messageDto)

        return savedMessage
    }

    @Transactional
    fun reactToMessage(chatId: Long, username: String, request: ReactionRequest) {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val message = chatMessageRepository.findById(request.messageId)
            .orElseThrow { EntityNotFoundException("Message with id ${request.messageId} not found") }

        val user = userRepository.findByUsernameValue(username)
            ?: throw EntityNotFoundException("User with username '$username' not found")

        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of the chat") }

        val existingReaction = messageReactionRepository.findByMessageIdAndUserUsernameValue(request.messageId, username)

        var notificationMessage: Any
        if (existingReaction != null) {
            if (existingReaction.reactionEmoji == request.emoji) {
                messageReactionRepository.delete(existingReaction)
                notificationMessage = MessageReactionDto(
                    messageId = request.messageId,
                    username = username,
                    emoji = null,
                    reactionTime = null
                )
            } else {
                existingReaction.reactionEmoji = request.emoji
                val updatedMessageReaction = messageReactionRepository.save(existingReaction)
                notificationMessage = MessageReactionDto(
                    messageId = request.messageId,
                    username = username,
                    emoji = request.emoji,
                    reactionTime = updatedMessageReaction.reactionTime,
                )
            }
        } else {
            val newReaction = messageReactionRepository.save(MessageReaction(
                message = message,
                user = user,
                reactionEmoji = request.emoji
            ))
            notificationMessage = MessageReactionDto(
                messageId = request.messageId,
                username = username,
                emoji = request.emoji,
                reactionTime = newReaction.reactionTime,
            )
        }

        webSocketNotificationService.sendChatMessage(chatId, WebSocketMessageType.REACTION_UPDATE, notificationMessage)
    }

    @Transactional
    fun editMessage(chatId: Long, username: String, request: EditMessageRequest) {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val message = chatMessageRepository.findById(request.messageId)
            .orElseThrow { EntityNotFoundException("Message with id ${request.messageId} not found") }

        val user = userRepository.findByUsernameValue(username)
            ?: throw EntityNotFoundException("User with username '$username' not found")

        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of the chat") }

        if (message.user?.id != user.id && !member.role.isAtLeast(ChatMemberRole.ADMIN))
            throw IllegalArgumentException("User is not the author of the message and he is not an admin")


        message.content = request.content
        message.editedAt = LocalDateTime.now()

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
        webSocketNotificationService.sendChatMessage(chatId, WebSocketMessageType.MESSAGE_UPDATE, messageDto)
    }

    @Transactional
    fun deleteMessage(chatId: Long, username: String, request: DeleteMessageRequest) {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val message = chatMessageRepository.findById(request.messageId)
            .orElseThrow { EntityNotFoundException("Message with id ${request.messageId} not found") }

        val user = userRepository.findByUsernameValue(username)
            ?: throw EntityNotFoundException("User with username '$username' not found")

        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of the chat") }

        if (message.user?.id != user.id && !member.role.isAtLeast(ChatMemberRole.ADMIN))
            throw IllegalArgumentException("User is not the author of the message and he is not an admin")

        message.isDeleted = true
        message.editedAt = LocalDateTime.now()
        chatMessageRepository.save(message)

        webSocketNotificationService.sendChatMessage(chatId, WebSocketMessageType.MESSAGE_DELETED,
            DeleteMessageDto(
                messageId = request.messageId,
                deletedAt = message.editedAt!!
            )
        )
    }

}