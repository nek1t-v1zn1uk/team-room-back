package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ChatMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    fun sendMessage(@Payload chatMessage: ChatMessage): ChatMessage {
        println(chatMessage.toString())
        return chatMessage
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    fun addUser(@Payload chatMessage: ChatMessage, headerAccessor: SimpMessageHeaderAccessor): ChatMessage {
        headerAccessor.sessionAttributes?.put("username", chatMessage.sender)
        return chatMessage
    }

    /*
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    fun handleMessage(message: ChatMessage): ChatMessage {
        // This is a simple echo service. The received message is returned directly.
        // It will be broadcasted to all clients subscribed to "/topic/messages"
        return message
    }
    */
}
