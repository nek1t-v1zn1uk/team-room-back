package com.example.teamroomback.utils

import com.example.teamroomback.services.ChatService
import com.example.teamroomback.validation.ChatTypeAspect
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.core.annotation.Order
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(0)
class ChatTypeAspect(
    private val chatService: ChatService
) {

    @Before("@annotation(chatType) && args(id, ..)")
    fun checkChatType(joinPoint: JoinPoint, id: Long, chatType: ChatTypeAspect) {
        if (!chatType.requiredChatType.contains(chatService.getChatById(id).type)) {
            throw AccessDeniedException("Chat must be ${chatType.requiredChatType.map { it.name }}, not ${chatService.getChatById(id).type}")
        }
    }
}