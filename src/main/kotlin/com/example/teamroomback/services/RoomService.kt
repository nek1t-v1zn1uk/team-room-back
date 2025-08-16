package com.example.teamroomback.services

import com.example.teamroomback.dtos.CreateRoomRequest
import com.example.teamroomback.dtos.CreateRoomResponse
import com.example.teamroomback.dtos.JoinRoomRequest
import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.Room
import com.example.teamroomback.entities.RoomMember
import com.example.teamroomback.entities.RoomMemberRole
import com.example.teamroomback.entities.RoomMessage
import com.example.teamroomback.entities.RoomMessageType
import com.example.teamroomback.repositories.MessageRepository
import com.example.teamroomback.repositories.RoomMemberRepository
import com.example.teamroomback.repositories.RoomRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import javax.management.InstanceNotFoundException

@Service
class RoomService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val roomRepository: RoomRepository,
    private val roomMemberRepository: RoomMemberRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) {
    fun createRoom(username: String, request: CreateRoomRequest) {
        val newRoom = roomRepository.save(Room(
            name = request.roomName,
            photoUrl = request.photoUrl,
        ))

        val user = userRepository.findByUsernameValue(username) ?: throw UsernameNotFoundException("User not found")

        val member = RoomMember(
            room = newRoom,
            user = user,
            role = RoomMemberRole.OWNER,
        )
        roomMemberRepository.save(member)

        val message = WebSocketBroadcast(WebSocketMessageType.ROOM_CREATED, CreateRoomResponse(
            roomId = newRoom.id!!,
            roomName = newRoom.name,
            photoUrl = newRoom.photoUrl
        ))
        simpMessagingTemplate.convertAndSendToUser(user.id.toString(), "/queue/notifications", message)
    }

    fun joinUser(username: String, request: JoinRoomRequest) {
        val user = userRepository.findByUsernameValue(username) ?: throw UsernameNotFoundException("User not found")
        val room = roomRepository.findRoomById(request.roomId) ?: throw InstanceNotFoundException("Room not found")

        val member = roomMemberRepository.save(RoomMember(
            room = room,
            user = user,
            role = RoomMemberRole.MEMBER,
        ))
        val roomMessage = messageRepository.save(RoomMessage(
            room = room,
            sender = user,
            content = "",
            type = RoomMessageType.JOIN
        ))
        val message = WebSocketBroadcast(WebSocketMessageType.USER_JOINED, roomMessage)
        simpMessagingTemplate.convertAndSend("/topic/rooms/${room.id}", message)
    }
}