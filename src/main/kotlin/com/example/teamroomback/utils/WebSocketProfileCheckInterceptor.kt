package com.example.teamroomback.utils

import com.example.teamroomback.services.UserService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class WebSocketProfileCheckInterceptor(
    private val userService: UserService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor?.command == StompCommand.CONNECT) {
            val user = accessor.user
            val username = user?.name
            if (username != null && !userService.hasProfile(username)) {
                throw IllegalStateException("User does not have a profile.")
            }
        }
        return message
    }
}