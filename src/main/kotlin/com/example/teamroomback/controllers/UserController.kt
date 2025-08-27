package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ChatMessageResponse
import com.example.teamroomback.dtos.CreateRoomResponse
import com.example.teamroomback.dtos.GetProfileResponse
import com.example.teamroomback.dtos.JoinRoomRequest
import com.example.teamroomback.dtos.JoinRoomResponse
import com.example.teamroomback.dtos.RoomRequest
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.services.MessageService
import com.example.teamroomback.services.RoomService
import com.example.teamroomback.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val userService: UserService,
    private val roomService: RoomService,
    private val messageService: MessageService
) {

    @DeleteMapping("/api/user")
    fun deleteUser(): ResponseEntity<Any> {
        return try{
            val authentication = SecurityContextHolder.getContext().authentication
            userService.deleteUser(authentication.name)

            ResponseEntity.ok(
                SimpleMessageResponse(
                    "User successfully deleted."
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    "User deletion failed: ${e.message}."
                )
            )
        }
    }

    @MessageMapping("/get-initial-data")
    fun getInitialData(headerAccessor: SimpMessageHeaderAccessor) {
        val username = headerAccessor.user!!.name

        val rooms = roomService.getUserRooms(username)

        for(room in rooms) {
            val message = WebSocketBroadcast(WebSocketMessageType.ROOM_CREATED, CreateRoomResponse(
                roomId = room.id!!,
                roomName = room.name,
                photoUrl = room.photoUrl,
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", message)
        }
    }
    @MessageMapping("/get-room-members")
    fun getRoomMembers(request: RoomRequest, headerAccessor: SimpMessageHeaderAccessor){
        val username = headerAccessor.user!!.name
        val roomId = request.roomId
        val members = roomService.getRoomMembers(roomId)

        for(member in members) {
            val message = WebSocketBroadcast(WebSocketMessageType.USER_JOINED, JoinRoomResponse(
                roomId = roomId,
                username = member.user.username,
                profile = GetProfileResponse(
                    firstName = member.user.profile!!.firstName,
                    lastName = member.user.profile!!.lastName,
                    biography = member.user.profile!!.biography,
                    photoUrl = member.user.profile!!.photoUrl
                ),
                role = member.role
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", message)
        }
    }
    @MessageMapping("/get-room-messages")
    fun getRoomMessages(request: RoomRequest, headerAccessor: SimpMessageHeaderAccessor){
        val username = headerAccessor.user!!.name
        val roomId = request.roomId
        val messages = messageService.getRoomMessages(roomId)

        for(msg in messages) {
            val broadcast = WebSocketBroadcast(WebSocketMessageType.CHAT_MESSAGE, ChatMessageResponse(
                senderUsername = msg.sender.username,
                roomId = msg.room.id!!,
                content = msg.content,
                type = msg.type
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", broadcast)
        }
    }
}