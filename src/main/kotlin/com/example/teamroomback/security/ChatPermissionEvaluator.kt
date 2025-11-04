package com.example.teamroomback.security

import com.example.teamroomback.entities.ChatMemberRole
import com.example.teamroomback.entities.ChatType
import com.example.teamroomback.repositories.ChatMemberRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.services.ChatService
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component("chatPermissionEvaluator")
class ChatPermissionEvaluator(
    private val chatService: ChatService,
    private val chatRepository: ChatRepository,
    private val chatMemberRepository: ChatMemberRepository
) : PermissionEvaluator {

    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any?): Boolean {
        if (authentication.name == null || targetDomainObject !is Long || permission !is String) {
            return false
        }

        val username = authentication.name
        val chatId = targetDomainObject

        if (permission == "PIN_MESSAGE") {
            val chat = chatRepository.findById(chatId)
                .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
            val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
                .orElse(null) ?: return false

            return when (chat.type) {
                ChatType.GROUP -> member.role.isAtLeast(ChatMemberRole.MODERATOR)
                ChatType.PRIVATE -> member.role.isAtLeast(ChatMemberRole.MEMBER)
                else -> false
            }
        } else if (permission == "DELETE_CHAT") {
            val chat = chatRepository.findById(chatId)
                .orElseThrow { EntityNotFoundException("Chat with id $chatId not found") }
            val member = chatMemberRepository.findByChatIdAndUserUsernameValue(chatId, username)
                .orElse(null) ?: return false

            return when (chat.type) {
                ChatType.GROUP -> member.role.isAtLeast(ChatMemberRole.OWNER)
                ChatType.COURSE_CHAT -> member.role.isAtLeast(ChatMemberRole.ADMIN)
                else -> false
            }
        }

        // logic for simple checking for minimum role
        val requiredRole = ChatMemberRole.valueOf(permission)
        val userRole = chatService.getRoleInChat(authentication.name, chatId)
            ?: return false

        return userRole.isAtLeast(requiredRole)
    }

    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?): Boolean {
        return false // Not used
    }
}