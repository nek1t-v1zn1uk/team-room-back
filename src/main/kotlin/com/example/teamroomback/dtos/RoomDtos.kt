package com.example.teamroomback.dtos

data class CreateRoomRequest(
    val roomName: String,
    val photoUrl: String?,
)
data class CreateRoomResponse(
    val roomId: Long,
    val roomName: String,
    val photoUrl: String?,
)

data class JoinRoomRequest(
    val roomId: Long,
    //val username: String,
)
data class LeaveRoomRequest(
    val id: Long,
)