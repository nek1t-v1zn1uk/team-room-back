package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateRoomRequest
import com.example.teamroomback.dtos.JoinRoomRequest
import com.example.teamroomback.dtos.LeaveRoomRequest
import com.example.teamroomback.services.MessageService
import com.example.teamroomback.services.RoomService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller

@Controller
class RoomController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val messageService: MessageService,
    private val roomService: RoomService,
) {
    @MessageMapping("/room.create")
    fun createRoom(request: CreateRoomRequest, headerAccessor: SimpMessageHeaderAccessor) {
        val username = headerAccessor.user!!.name
        roomService.createRoom(username, request)
    }

    @MessageMapping("/room.join")
    fun joinRoom(request: JoinRoomRequest, headerAccessor: SimpMessageHeaderAccessor) {
        val authentication = SecurityContextHolder.getContext().authentication
        val username: String = authentication.name

        roomService.joinUser(username, request)

    }

    @MessageMapping("/room.leave")
    fun leaveRoom(request: LeaveRoomRequest, headerAccessor: SimpMessageHeaderAccessor) {
        //GET
        // SSN: user
        // RQST: roomId
        //MAKE
        // remove user from room
        // if he was last user - delete room
        // if he was owner - change owner to someone else(with highest role)
        //RETURN
        // MSG: User left room
    }

    // change user role
    // edit room profile
}