package com.example.teamroomback.services

import com.example.teamroomback.dtos.ChatMessageRequest
import com.example.teamroomback.dtos.ChatMessageResponse
import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.RoomMessage
import com.example.teamroomback.repositories.MessageRepository
import com.example.teamroomback.repositories.RoomRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import javax.management.InstanceNotFoundException

@Service
class MessageService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    val messageRepository: MessageRepository,
    val roomRepository: RoomRepository,
    val userService: UserService,
) {

    fun sendMessage(username: String, chatMessage: ChatMessageRequest) {
        val room = roomRepository.findRoomById(chatMessage.roomId) ?: throw InstanceNotFoundException("Room not found")
        val sender = userService.findByUsername(username) ?: throw UsernameNotFoundException("User not found")
        val newMessage = messageRepository.save(RoomMessage(
            room = room,
            sender = sender,
            content = chatMessage.content,
            type = chatMessage.type,
        ))

        val broadcast = WebSocketBroadcast(
            type = WebSocketMessageType.CHAT_MESSAGE,
            payload = ChatMessageResponse(
                senderUsername = newMessage.sender.username,
                roomId = newMessage.room.id!!,
                content = newMessage.content,
                type = newMessage.type,
            ),
        )
        simpMessagingTemplate.convertAndSend("/topic/rooms/${chatMessage.roomId}", broadcast)
    }

    fun getRoomMessages(roomId: Long): List<RoomMessage> {
        return messageRepository.findRoomMessagesByRoomId(roomId)
    }

}