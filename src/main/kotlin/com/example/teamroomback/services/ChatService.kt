package com.example.teamroomback.services

import com.example.teamroomback.dtos.*
import com.example.teamroomback.entities.*
import com.example.teamroomback.repositories.ChatMemberRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMemberRepository: ChatMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getUserChats(username: String): List<UserChatDTO> {
        return chatRepository.findChatsByUsername(username).map {
            UserChatDTO(
                id = it.id!!,
                name = it.name,
                photoUrl = it.photoUrl,
                type = it.type,
                courseId = it.course?.id
            )
        }
    }

    @Transactional
    fun createGroupChat(creatorUsername: String, request: CreateGroupChatRequest): Chat {
        val creator = userRepository.findByUsernameValue(creatorUsername)
            ?: throw EntityNotFoundException("Creator user not found")

        val chat = Chat(
            name = request.name,
            type = ChatType.GROUP
        )
        val savedChat = chatRepository.save(chat)

        val memberUsernames = (request.memberUsernames + creatorUsername).toSet()
        val members = memberUsernames.map { username ->
            val user = userRepository.findByUsernameValue(username)
                ?: throw EntityNotFoundException("User with username '$username' not found.")
            val role = if (user.id == creator.id) ChatMemberRole.OWNER else ChatMemberRole.MEMBER
            ChatMember(chat = savedChat, user = user, role = role)
        }

        chatMemberRepository.saveAll(members)
        return savedChat
    }

    @Transactional(readOnly = true)
    fun getChatDetails(chatId: Long, username: String): ChatDetailsDTO {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }

        val currentUserMemberInfo = chatMemberRepository.findByChatIdAndUserUsername(chatId, username)
            .orElseThrow { IllegalAccessError("User is not a member of this chat") }

        return ChatDetailsDTO(
            id = chat.id!!,
            name = chat.name,
            photoUrl = chat.photoUrl,
            type = chat.type,
            courseId = chat.course?.id,
            members = chat.members.map { ChatMemberDTO(it.user.username, it.role) },
            currentUserInfo = CurrentUserChatInfoDTO(
                role = currentUserMemberInfo.role,
                joinedAt = currentUserMemberInfo.joinedAt,
                lastReadMessageId = currentUserMemberInfo.lastReadMessage?.id
            )
        )
    }

    @Transactional
    fun updateChat(chatId: Long, request: UpdateChatRequest): Chat {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }

        chat.name = request.name
        chat.photoUrl = request.photoUrl

        return chatRepository.save(chat)
    }

    @Transactional
    fun patchChat(chatId: Long, request: PatchChatRequest): Chat {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }

        request.name?.let { chat.name = it }
        request.photoUrl?.let { chat.photoUrl = it }

        return chatRepository.save(chat)
    }

    @Transactional
    fun deleteChat(chatId: Long) {
        if (!chatRepository.existsById(chatId)) {
            throw EntityNotFoundException("Chat with id $chatId not found")
        }
        chatRepository.deleteById(chatId)
    }

    @Transactional(readOnly = true)
    fun getRoleInChat(username: String, chatId: Long): ChatMemberRole? {
        if (!chatRepository.existsById(chatId)) {
            return null
        }
        return chatMemberRepository.findByChatIdAndUserUsername(chatId, username)
            .map { it.role }
            .orElse(null)
    }
}