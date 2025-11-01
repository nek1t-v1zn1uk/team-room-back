package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.*
import com.example.teamroomback.services.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Чати", description = "Ендпоїнти для керування чатами")
@SecurityRequirement(name = "bearerAuth")
class ChatController(private val chatService: ChatService) {

    @GetMapping
    @Operation(summary = "Отримати чати поточного користувача", description = "", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "200", description = "Список чатів користувача")
    fun getUserChats(): ResponseEntity<List<UserChatDTO>> {
        val username = SecurityContextHolder.getContext().authentication.name
        val chats = chatService.getUserChats(username)
        return ResponseEntity.ok(chats)
    }

    @PostMapping
    @Operation(summary = "Створити новий груповий чат", description = "", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "201", description = "Чат успішно створено")
    fun createGroupChat(@Valid @RequestBody request: CreateGroupChatRequest): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val chat = chatService.createGroupChat(username, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateChatResponse(chat.id!!, "Group chat created successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Error creating chat"))
        }
    }

    @GetMapping("/{chatId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримати детальну інформацію про чат", description = "Потребує ролі не нижче VIEWER.", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "200", description = "Деталі чату")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Чат не знайдено")
    fun getChatDetails(@PathVariable chatId: Long): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val chatDetails = chatService.getChatDetails(chatId, username)
            ResponseEntity.ok(chatDetails)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: IllegalAccessError) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }

    @PutMapping("/{chatId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Повністю оновити чат (ім'я, фото)", description = "Потребує ролі не нижче ADMIN.", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "200", description = "Чат оновлено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Чат не знайдено")
    fun updateChat(@PathVariable chatId: Long, @Valid @RequestBody request: UpdateChatRequest): ResponseEntity<Any> {
        return try {
            val updatedChat = chatService.updateChat(chatId, request)
            ResponseEntity.ok(SimpleChatResponse(updatedChat.id!!, "Chat updated successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @PatchMapping("/{chatId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Частково оновити чат (ім'я або фото)", description = "Потребує ролі не нижче ADMIN.", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "200", description = "Чат оновлено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Чат не знайдено")
    fun patchChat(@PathVariable chatId: Long, @RequestBody request: PatchChatRequest): ResponseEntity<Any> {
        return try {
            val patchedChat = chatService.patchChat(chatId, request)
            ResponseEntity.ok(SimpleChatResponse(patchedChat.id!!, "Chat patched successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @DeleteMapping("/{chatId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'OWNER')")
    @Operation(summary = "Видалити чат", description = "Потребує ролі не нижче OWNER.", tags = ["Чати - керування чатами"])
    @ApiResponse(responseCode = "200", description = "Чат видалено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Чат не знайдено")
    fun deleteChat(@PathVariable chatId: Long): ResponseEntity<Any> {
        return try {
            chatService.deleteChat(chatId)
            ResponseEntity.ok(SimpleChatResponse(chatId, "Chat deleted successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @GetMapping("/{chatId}/members")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримати список учасників чату", description = "Потребує ролі не нижче VIEWER.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Список учасників")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    fun getChatMembers(@PathVariable chatId: Long): ResponseEntity<List<ChatMemberDetailsDTO>> {
        val members = chatService.getChatMembers(chatId)
        return ResponseEntity.ok(members)
    }

    @PostMapping("/{chatId}/members")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Додати нового учасника в чат", description = "Потребує ролі не нижче ADMIN.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "201", description = "Учасника додано")
    @ApiResponse(responseCode = "400", description = "Невірний запит (користувач вже в чаті або не існує)")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    fun addChatMember(@PathVariable chatId: Long, @Valid @RequestBody request: AddChatMemberRequest): ResponseEntity<Any> {
        return try {
            val newMember = chatService.addMember(chatId, request)
            ResponseEntity.status(HttpStatus.CREATED).body(
                ChatMemberDetailsDTO(
                    username = newMember.user.username,
                    role = newMember.role,
                    joinedAt = newMember.joinedAt
                )
            )
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Bad Request"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Bad Request"))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }

    @PutMapping("/{chatId}/members/{username}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Змінити роль учаснику чату", description = "Потребує ролі не нижче ADMIN.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Роль оновлено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено (недостатньо прав для зміни)")
    @ApiResponse(responseCode = "404", description = "Учасника не знайдено")
    fun updateMemberRole(
        @PathVariable chatId: Long,
        @PathVariable username: String,
        @RequestBody request: UpdateChatMemberRoleRequest
    ): ResponseEntity<Any> {
        return try {
            val actorUsername = SecurityContextHolder.getContext().authentication.name
            val updatedMember = chatService.updateMemberRole(chatId, username, request.role, actorUsername)
            ResponseEntity.ok(ChatMemberDetailsDTO(updatedMember.user.username, updatedMember.role, updatedMember.joinedAt))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }

    @DeleteMapping("/{chatId}/members/{username}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Видалити учасника з чату", description = "Потребує ролі не нижче ADMIN.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Учасника видалено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Учасника не знайдено")
    fun removeMember(@PathVariable chatId: Long, @PathVariable username: String): ResponseEntity<Any> {
        return try {
            val actorUsername = SecurityContextHolder.getContext().authentication.name
            chatService.removeMember(chatId, username, actorUsername)
            ResponseEntity.ok(SimpleMessageResponse("User '$username' removed from chat successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }

    @DeleteMapping("/{chatId}/members/me")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Вийти з чату", description = "Потребує ролі не нижче VIEWER.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Ви успішно покинули чат")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено (власник не може покинути чат)")
    @ApiResponse(responseCode = "404", description = "Чат або учасника не знайдено")
    fun leaveChat(@PathVariable chatId: Long): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            chatService.leaveChat(chatId, username)
            ResponseEntity.ok(SimpleMessageResponse("You have successfully left the chat"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }

    @PostMapping("/{chatId}/transfer-ownership")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'OWNER')")
    @Operation(summary = "Передати права власності на чат", description = "Потребує ролі не нижче OWNER.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Права власності успішно передано")
    @ApiResponse(responseCode = "400", description = "Невірний запит (новий власник не є учасником чату)")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено (тільки поточний власник може передати права)")
    @ApiResponse(responseCode = "404", description = "Чат або учасника не знайдено")
    fun transferOwnership(
        @PathVariable chatId: Long,
        @Valid @RequestBody request: TransferOwnershipRequest
    ): ResponseEntity<Any> {
        return try {
            val currentOwnerUsername = SecurityContextHolder.getContext().authentication.name
            chatService.transferOwnership(chatId, request.newOwnerUsername, currentOwnerUsername)

            ResponseEntity.ok(SimpleMessageResponse("Ownership transferred successfully to ${request.newOwnerUsername}"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: AccessDeniedException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(SimpleMessageResponse(e.message ?: "Forbidden"))
        }
    }
}
