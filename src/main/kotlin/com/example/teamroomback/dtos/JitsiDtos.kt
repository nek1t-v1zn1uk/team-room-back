package com.example.teamroomback.dtos

data class JitsiEventDTO(
    val event: JitsiEventType,
    val userId: String?,
    val roomName: String,
    val roomDomain: String
)

enum class JitsiEventType {
    user_joined,
    user_left,
    conference_ended
}