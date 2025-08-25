package com.example.teamroomback.dtos

import com.example.teamroomback.entities.RoomMessageType
import jakarta.validation.constraints.NotBlank

data class ChatMessageRequest(
    @field:NotBlank(message = "Missed room id")
    val roomId: Long,
    @field:NotBlank(message = "Missed content")
    val content: String,
    @field:NotBlank(message = "Missed message type")
    val type: RoomMessageType,
)
data class ChatMessageResponse(
    val senderUsername: String,
    val roomId: Long,
    val content: String,
    val type: RoomMessageType,
)