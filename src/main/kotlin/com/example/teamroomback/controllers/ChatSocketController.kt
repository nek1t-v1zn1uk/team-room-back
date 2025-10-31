package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.SendMessageRequest
import com.example.teamroomback.services.MessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class ChatSocketController(
    private val messageService: MessageService
) {

    @MessageMapping("/chat/{chatId}/send")
    fun sendMessage(
        @DestinationVariable chatId: Long,
        @Payload request: SendMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.postMessage(chatId, username, request)
    }
}