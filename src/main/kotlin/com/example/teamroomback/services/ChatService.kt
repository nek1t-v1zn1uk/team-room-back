package com.example.teamroomback.services

import com.example.teamroomback.dtos.*
import com.example.teamroomback.entities.*
import com.example.teamroomback.repositories.ChatMemberRepository
import com.example.teamroomback.repositories.ChatMessageRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.repositories.PinnedMessageRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMemberRepository: ChatMemberRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository,
    private val pinnedMessageRepository: PinnedMessageRepository
) {

    @Transactional(readOnly = true)
    fun getUserChats(username: String): List<UserChatDTO> {
        return chatRepository.findChatsByUsername(username)
            .filter {
                if(it.type == ChatType.PRIVATE) {
                    val member = it.members.find { member -> member.user.username == username }!!
                    if(memberHasAccessToChat(it, member))
                        true
                    else
                        false
                } else {
                    true
                }
            }
            .map {
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
            photoUrl = request.photoUrl,
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

    @Transactional
    fun createPrivateChat(creatorUsername: String, request: CreatePrivateChatRequest): Chat {
        val firstUser = userRepository.findByUsernameValue(creatorUsername)
            ?: throw EntityNotFoundException("Creator user not found")
        val secondUser = userRepository.findByUsernameValue(request.username)
            ?: throw EntityNotFoundException("Member user not found")

        val existingChat = chatRepository.findPrivateChatByMembersUsernames(firstUser.username, secondUser.username)

        if(existingChat == null) {
            val chat = Chat(
                type = ChatType.PRIVATE
            )
            val savedChat = chatRepository.save(chat)
            val members = listOf(
                ChatMember(chat = savedChat, user = firstUser, role = ChatMemberRole.MEMBER),
                ChatMember(chat = savedChat, user = secondUser, role = ChatMemberRole.MEMBER)
            )
            chatMemberRepository.saveAll(members)
            return savedChat
        } else {
            val firstMember = chatMemberRepository.findByChatIdAndUserUsernameValue(existingChat.id!!, firstUser.username)
                .orElseThrow { EntityNotFoundException("Member not found") }
            val secondMember = chatMemberRepository.findByChatIdAndUserUsernameValue(existingChat.id!!, secondUser.username)
                .orElseThrow { EntityNotFoundException("Member not found") }

            val lastMessage = chatMessageRepository.findTopByChatIdOrderByIdDesc(existingChat.id!!)

            // if member HAS chat now
            if(memberHasAccessToChat(existingChat, firstMember))
                throw IllegalArgumentException("Private chat already exists")
            // if member DOESNT have chat now
            return existingChat
        }
    }

    @Transactional(readOnly = true)
    fun getChatDetails(chatId: Long, username: String): ChatDetailsDTO {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }

        val currentUserMemberInfo = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
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
        return chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .map { it.role }
            .orElse(null)
    }

    @Transactional(readOnly = true)
    fun getChatMembers(chatId: Long): List<ChatMemberDetailsDTO> {
        return chatMemberRepository.findAllByChatId(chatId).map {
            ChatMemberDetailsDTO(
                username = it.user.username,
                role = it.role,
                joinedAt = it.joinedAt
            )
        }
    }

    @Transactional
    fun addMember(chatId: Long, request: AddChatMemberRequest): ChatMember {
        if (chatMemberRepository.existsByChatIdAndUserUsernameValue(chatId, request.username)) {
            throw IllegalArgumentException("User '${request.username}' is already a member of this chat.")
        }

        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
        val user = userRepository.findByUsernameValue(request.username)
            ?: throw EntityNotFoundException("User with username '${request.username}' not found.")

        if (request.role == ChatMemberRole.OWNER) {
            throw AccessDeniedException("Cannot assign OWNER role directly.")
        }

        val newMember = ChatMember(
            chat = chat,
            user = user,
            role = request.role
        )

        return chatMemberRepository.save(newMember)
    }

    @Transactional
    fun updateMemberRole(chatId: Long, targetUsername: String, newRole: ChatMemberRole, actorUsername: String): ChatMember {
        val actorMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, actorUsername)
            .orElseThrow { AccessDeniedException("Action performer is not a member of the chat.") }

        val targetMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, targetUsername)
            .orElseThrow { EntityNotFoundException("Target user '$targetUsername' is not a member of this chat.") }

        if (targetMember.role == ChatMemberRole.OWNER) {
            throw AccessDeniedException("Cannot change the role of the chat OWNER.")
        }

        if (newRole == ChatMemberRole.OWNER) {
            throw AccessDeniedException("Cannot assign OWNER role. Ownership must be transferred.")
        }

        if (newRole == ChatMemberRole.ADMIN || targetMember.role == ChatMemberRole.ADMIN) {
            if (actorMember.role != ChatMemberRole.OWNER) {
                throw AccessDeniedException("Only the OWNER can manage ADMIN roles.")
            }
        }

        targetMember.role = newRole
        return chatMemberRepository.save(targetMember)
    }

    @Transactional
    fun removeMember(chatId: Long, targetUsername: String, actorUsername: String) {
        val actorMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, actorUsername)
            .orElseThrow { AccessDeniedException("Action performer is not a member of the chat.") }

        val targetMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, targetUsername)
            .orElseThrow { EntityNotFoundException("Target user '$targetUsername' is not a member of this chat.") }

        if (targetMember.role == ChatMemberRole.OWNER) {
            throw AccessDeniedException("Chat OWNER cannot be removed from the chat.")
        }

        if (targetMember.role == ChatMemberRole.ADMIN && actorMember.role != ChatMemberRole.OWNER) {
            throw AccessDeniedException("Only the OWNER can remove an ADMIN.")
        }

        chatMemberRepository.delete(targetMember)
    }

    @Transactional
    fun leaveChat(chatId: Long, username: String) {
        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of this chat.") }

        if (member.role == ChatMemberRole.OWNER) {
            throw AccessDeniedException("The OWNER cannot leave the chat. Delete the chat or transfer ownership first.")
        }

        chatMemberRepository.delete(member)
    }

    @Transactional
    fun clearPrivateChat(chatId: Long, username: String, clearForBoth: Boolean = false) {
        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of this chat.") }

        if(clearForBoth) {
            chatRepository.deleteById(chatId)
        } else {
            val lastMessage = chatMessageRepository.findTopByChatIdOrderByIdDesc(chatId)

            member.lastAccessibleMessage = lastMessage
        }
    }

    @Transactional
    fun transferOwnership(chatId: Long, newOwnerUsername: String, currentOwnerUsername: String) {
        val chat = chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }

        val currentOwnerMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, currentOwnerUsername)
            .orElseThrow { AccessDeniedException("Current owner is not a member of the chat.") }

        if (currentOwnerMember.role != ChatMemberRole.OWNER) {
            throw AccessDeniedException("Only the current owner can transfer ownership.")
        }

        val newOwnerMember = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, newOwnerUsername)
            .orElseThrow { EntityNotFoundException("New owner is not a member of the chat.") }

        currentOwnerMember.role = ChatMemberRole.ADMIN
        newOwnerMember.role = ChatMemberRole.OWNER

        chatMemberRepository.saveAll(listOf(currentOwnerMember, newOwnerMember))
    }

    @Transactional(readOnly = true)
    fun getMessagesInChat(chatId: Long, messageId: Long?, limitBefore: Int, limitAfter: Int, username: String): List<ChatMessageDto> {
        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of this chat.") }
        val messages = if (messageId != null) {
            val messagesBefore = if (limitBefore > 0) {
                chatMessageRepository.findMessagesBefore(chatId, messageId, PageRequest.of(0, limitBefore))
            } else {
                emptyList()
            }

            val centerMessage = chatMessageRepository.findById(messageId).map { listOf(it) }.orElse(emptyList())

            val messagesAfter = if (limitAfter > 0) {
                chatMessageRepository.findMessagesAfter(chatId, messageId, PageRequest.of(0, limitAfter))
            } else {
                emptyList()
            }

            messagesBefore.reversed() + centerMessage + messagesAfter
        } else {
            val lastMessage = chatMessageRepository.findTopByChatIdOrderByIdDesc(chatId)
                ?: return emptyList()
            val messagesBefore = if (limitBefore > 0) {
                chatMessageRepository.findMessagesBefore(chatId, lastMessage.id!!, PageRequest.of(0, limitBefore))
            } else {
                emptyList()
            }
            val centerMessage = chatMessageRepository.findById(lastMessage.id!!).map { listOf(it) }.orElse(emptyList())
            messagesBefore.reversed() + centerMessage
        }
            .filter { member.lastAccessibleMessage == null || it.id!! > member.lastAccessibleMessage!!.id!! }

        return messages.map { it.toChatMessageDto() }
    }

    @Transactional(readOnly = true)
    fun getMessageInChat(chatId: Long, messageId: Long?, username: String): ChatMessageDto {
        val message: ChatMessage = if (messageId == null) {
            chatMessageRepository.findTopByChatIdOrderByIdDesc(chatId)
                ?: throw EntityNotFoundException("Messages in chat with id $chatId not found")
        } else {
            chatMessageRepository.findById(messageId)
                .orElseThrow { EntityNotFoundException("Message with id $messageId not found") }
        }
        if(message.chat.id != chatId)
            throw EntityNotFoundException("Message with id $messageId not found")

        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
            .orElseThrow { EntityNotFoundException("User is not a member of this chat.") }

        if(member.lastAccessibleMessage == null || message.id!! > member.lastAccessibleMessage!!.id!!)
            return message.toChatMessageDto()
        throw EntityNotFoundException("Message with id $messageId not found")
    }

    @Transactional(readOnly = true)
    fun getPinnedMessages(chatId: Long, username: String): List<PinnedMessageDto> {
        val pinnedMessages = pinnedMessageRepository.findAllByChatId(chatId)
        val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username).get()
        return pinnedMessages.filter{ member.lastAccessibleMessage == null || it.message.id!! > member.lastAccessibleMessage!!.id!! }.map { it.toPinnedMessageDto() }
    }

    @Transactional
    fun pinMessage(chatId: Long, username: String, request: PinMessageRequest): PinnedMessage {
        val chat = chatRepository.findById(chatId).get()
        val user = userRepository.findByUsernameValue(username)!!
        val message = chatMessageRepository.findById(request.messageId).get()

        val existingPinnedMessage = pinnedMessageRepository.findPinnedMessageByMessageId(message.id!!)
        if(existingPinnedMessage != null)
            throw IllegalArgumentException("This message is already pinned")

        val pinnedMessage = PinnedMessage(
            chat = chat,
            message = message,
            pinnedByUser = user
        )

        return pinnedMessageRepository.save(pinnedMessage)
    }

    @Transactional
    fun unpinMessage(chatId: Long, username: String, messageId: Long): PinnedMessage {
        val chat = chatRepository.findById(chatId).get()
        val user = userRepository.findByUsernameValue(username)!!
        val message = chatMessageRepository.findById(messageId).get()

        val existingPinnedMessage = pinnedMessageRepository.findPinnedMessageByMessageId(message.id!!)
            ?: throw IllegalArgumentException("This message is not pinned")

        pinnedMessageRepository.delete(existingPinnedMessage)
        return existingPinnedMessage
    }

    fun getChatById(chatId: Long): Chat {
        return chatRepository.findById(chatId)
            .orElseThrow { EntityNotFoundException("Chat with id $chatId not found")  }
    }

    fun memberHasAccessToChat(chat: Chat, member: ChatMember): Boolean {
        val lastMessage = chatMessageRepository.findTopByChatIdOrderByIdDesc(chat.id!!)
        if(member.lastAccessibleMessage == null && lastMessage != null ||
            (member.lastAccessibleMessage != null && lastMessage != null && member.lastAccessibleMessage!!.id!! < lastMessage.id!!)
        )
            return true

        return false
    }
}