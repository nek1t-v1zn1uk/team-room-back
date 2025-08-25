package com.example.teamroomback.utils

import com.example.teamroomback.dtos.ChatMessageResponse
import com.example.teamroomback.entities.RoomMessageType
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
        val userId: Long = headerAccessor.sessionAttributes?.get("userId") as Long
        val roomId: Long = headerAccessor.sessionAttributes?.get("roomId") as Long
        /*if(userId != null){
            val msg = ChatMessageResponse(
                senderUsername = ,
                roomId = roomId,
                content = "",
                type = RoomMessageType.LEAVE,
            )
            messageTemplate.convertAndSend("/topic/rooms/$roomId", msg)
        }*/
    }
}