package com.example.teamroomback.dtos

import com.example.teamroomback.entities.RoomMemberRole

data class CreateRoomRequest(
    val roomName: String,
    val photoUrl: String?,
)
data class CreateRoomResponse(
    val roomId: Long,
    val roomName: String,
    val photoUrl: String?,
)

data class RoomRequest(
    val roomId: Long,
)

data class JoinRoomRequest(
    val roomId: Long,
    val username: String,
)
data class JoinRoomResponse(
    val roomId: Long,
    val username: String,
    val profile: GetProfileResponse,
    val role: RoomMemberRole,
)

data class LeaveRoomRequest(
    val id: Long,
)