package com.example.teamroomback.dtos

data class WebSocketBroadcast(
    val type: WebSocketMessageType,
    val payload: Any
)
enum class WebSocketMessageType {
    CHAT_MESSAGE,
    USER_JOINED,
    USER_LEFT,
    ROOM_CREATED,

    JOINED_TO_COURSE,
    REMOVED_FROM_COURSE,
    ROLE_CHANGED_IN_COURSE,
    COURSE_UPDATED,
    COURSE_DELETED,

    MATERIAL_CREATED,
    MATERIAL_UPDATED, // material, media, tags
    MATERIAL_DELETED,

    ASSIGNMENT_CREATED,
    ASSIGNMENT_UPDATED,
    ASSIGNMENT_DELETED,
    ASSIGNMENT_RESPONSE_CREATED,
    ASSIGNMENT_RESPONSE_UPDATED,
    ASSIGNMENT_RESPONSE_DELETED,


}