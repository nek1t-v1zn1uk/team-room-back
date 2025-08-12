package com.example.teamroomback.utils

import com.example.teamroomback.services.UserService
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class WebSocketJwtChannelInterceptor(
    private val jwtUtils: JwtUtils,
    private val userService: UserService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (accessor?.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val jwt = authHeader.substring("Bearer ".length)
                val username = jwtUtils.extractUsername(jwt)

                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails: UserDetails = userService.loadUserByUsername(username)

                    if (jwtUtils.validateToken(jwt, userDetails)) {
                        val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        SecurityContextHolder.getContext().authentication = authToken
                        accessor.user = authToken
                    }
                }
            }
        }
        return message
    }
}