package com.example.teamroomback.security

import com.example.teamroomback.entities.ChatMemberRole
import com.example.teamroomback.services.ChatService
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.io.Serializable

@Component("chatPermissionEvaluator")
class ChatPermissionEvaluator(
    private val chatService: ChatService,
) : PermissionEvaluator {

    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any?): Boolean {
        if (authentication.name == null || targetDomainObject !is Long || permission !is String) {
            return false
        }

        val chatId = targetDomainObject
        val requiredRole = ChatMemberRole.valueOf(permission)
        val userRole = chatService.getRoleInChat(authentication.name, chatId) ?: return false

        return userRole.isAtLeast(requiredRole)
    }

    override fun hasPermission(authentication: Authentication, targetId: Serializable?, targetType: String?, permission: Any?): Boolean {
        return false // Not used
    }
}