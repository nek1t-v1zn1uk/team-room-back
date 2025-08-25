package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ChatMessageRequest
import com.example.teamroomback.services.MessageService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val messageService: MessageService,
) {

    @MessageMapping("/chat.sendMessage")
    fun sendMessage(chatMessage: ChatMessageRequest, headerAccessor: SimpMessageHeaderAccessor) {
        val username = headerAccessor.user!!.name
        messageService.sendMessage(username, chatMessage)
    }

/*    @MessageMapping("/chat.sendMessage")
    fun sendMessage(@Payload chatMessage: ChatMessage) {
        val roomId = chatMessage.roomId
        val destination = "/topic/rooms/$roomId"
        simpMessagingTemplate.convertAndSend(destination, chatMessage)
    }*/

/*    @MessageMapping("/chat.addUser")
    fun addUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor) {
        val senderId = chatMessage.senderId
        val roomId = chatMessage.roomId

        headerAccessor.sessionAttributes?.put("userId", senderId)
        headerAccessor.sessionAttributes?.put("roomId", roomId)



        val destination = "/topic/rooms/$roomId"
        simpMessagingTemplate.convertAndSend(destination, chatMessage)
    }*/
}
