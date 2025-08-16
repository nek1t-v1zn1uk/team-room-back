package com.example.teamroomback.dtos

data class WebSocketBroadcast(
    val type: WebSocketMessageType,
    val payload: Any
)
enum class WebSocketMessageType {
    CHAT_MESSAGE,
    USER_JOINED,
    USER_LEFT,
    ROOM_CREATED
}