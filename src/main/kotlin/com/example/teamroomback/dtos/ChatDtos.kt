package com.example.teamroomback.dtos

import com.example.teamroomback.entities.ChatMemberRole
import com.example.teamroomback.entities.ChatType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ChatMemberDTO(
    val username: String,
    val role: ChatMemberRole
)

data class ChatMemberDetailsDTO(
    val username: String,
    val role: ChatMemberRole,
    val joinedAt: LocalDateTime?
)

data class CurrentUserChatInfoDTO(
    val role: ChatMemberRole,
    val joinedAt: LocalDateTime?,
    val lastReadMessageId: Long?
)

data class ChatDetailsDTO(
    val id: Long,
    val name: String?,
    val photoUrl: String?,
    val type: ChatType,
    val courseId: Long?,
    val members: List<ChatMemberDTO>,
    val currentUserInfo: CurrentUserChatInfoDTO
)

data class UserChatDTO(
    val id: Long,
    val name: String?,
    val photoUrl: String?,
    val type: ChatType,
    val courseId: Long?
)

data class CreateGroupChatRequest(
    @field:NotBlank(message = "Chat name cannot be blank")
    @field:Size(min = 1, max = 255, message = "Chat name must be between 1 and 255 characters")
    val name: String,

    val memberUsernames: List<String>
)

data class UpdateChatRequest(
    @field:NotBlank(message = "Chat name cannot be blank")
    @field:Size(min = 1, max = 255, message = "Chat name must be between 1 and 255 characters")
    val name: String,
    val photoUrl: String?
)

data class PatchChatRequest(
    @field:Size(min = 1, max = 255, message = "Chat name must be between 1 and 255 characters")
    val name: String?,
    val photoUrl: String?
)

data class AddChatMemberRequest(
    @field:NotBlank(message = "Username cannot be blank")
    val username: String,
    val role: ChatMemberRole = ChatMemberRole.MEMBER
)

data class UpdateChatMemberRoleRequest(
    val role: ChatMemberRole
)

data class TransferOwnershipRequest(
    @field:NotBlank(message = "New owner username cannot be blank")
    val newOwnerUsername: String
)

data class CreateChatResponse(val chatId: Long, val message: String)
data class SimpleChatResponse(val chatId: Long, val message: String)