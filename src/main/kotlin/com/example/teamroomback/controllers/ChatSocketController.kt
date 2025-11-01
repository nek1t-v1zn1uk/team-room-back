package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.DeleteMessageRequest
import com.example.teamroomback.dtos.EditMessageRequest
import com.example.teamroomback.dtos.ReactionRequest
import com.example.teamroomback.dtos.ReadMessageRequest
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

    @MessageMapping("/chat/{chatId}/react")
    fun reactToMessage(
        @DestinationVariable chatId: Long,
        @Payload request: ReactionRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.reactToMessage(chatId, username, request)
    }

    @MessageMapping("/chat/{chatId}/edit")
    fun editMessage(
        @DestinationVariable chatId: Long,
        @Payload request: EditMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.editMessage(chatId, username, request)
    }

    @MessageMapping("/chat/{chatId}/delete")
    fun deleteMessage(
        @DestinationVariable chatId: Long,
        @Payload request: DeleteMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.deleteMessage(chatId, username, request)
    }

    @MessageMapping("/chat/{chatId}/typing/start")
    fun startTyping(
        @DestinationVariable chatId: Long,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.startTyping(chatId, username)
    }

    @MessageMapping("/chat/{chatId}/typing/stop")
    fun stopTyping(
        @DestinationVariable chatId: Long,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.stopTyping(chatId, username)
    }

    @MessageMapping("/chat/{chatId}/read")
    fun readMessage(
        @DestinationVariable chatId: Long,
        @Payload request: ReadMessageRequest,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val username = headerAccessor.user!!.name

        messageService.readMessage(chatId, username, request)
    }
}