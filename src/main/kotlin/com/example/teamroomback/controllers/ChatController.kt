package com.example.teamroomback.controllers

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class ChatController(
    private val simpMessagingTemplate: SimpMessagingTemplate
) {


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
