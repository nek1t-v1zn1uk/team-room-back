package com.example.teamroomback.utils

import com.example.teamroomback.dtos.ChatMessage
import com.example.teamroomback.dtos.MessageType
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
@RequiredArgsConstructor
@Slf4j
class WebSocketEventListener(
    private val messageTemplate: SimpMessageSendingOperations
) {

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent){
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username: String = headerAccessor.sessionAttributes?.get("username").toString()
        val roomId: String = headerAccessor.sessionAttributes?.get("roomId").toString()
        if(username != null){
            val msg = ChatMessage(
                roomId = roomId,
                sender = username,
                type = MessageType.LEAVE,
            )
            messageTemplate.convertAndSend("/topic/rooms/$roomId", msg)
        }
    }
}