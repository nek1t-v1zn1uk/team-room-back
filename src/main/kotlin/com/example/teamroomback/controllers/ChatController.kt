package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ChatMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/chat.sendMessage")
    fun sendMessage(@Payload chatMessage: ChatMessage) {
        val roomId = chatMessage.roomId
        val destination = "/topic/rooms/$roomId"
        simpMessagingTemplate.convertAndSend(destination, chatMessage)
    }

    @MessageMapping("/chat.addUser")
    fun addUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor) {
        headerAccessor.sessionAttributes?.put("username", chatMessage.sender)
        val roomId = chatMessage.roomId
        headerAccessor.sessionAttributes?.put("roomId", roomId)
        val destination = "/topic/rooms/$roomId"
        simpMessagingTemplate.convertAndSend(destination, chatMessage)
    }
}
