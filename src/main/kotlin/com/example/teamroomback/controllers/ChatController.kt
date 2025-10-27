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
    @Operation(summary = "Отримати чати поточного користувача")
    @ApiResponse(responseCode = "200", description = "Список чатів користувача")
    fun getUserChats(): ResponseEntity<List<UserChatDTO>> {
        val username = SecurityContextHolder.getContext().authentication.name
        val chats = chatService.getUserChats(username)
        return ResponseEntity.ok(chats)
    }

    @PostMapping
    @Operation(summary = "Створити новий груповий чат")
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
    @Operation(summary = "Отримати детальну інформацію про чат")
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
    @Operation(summary = "Повністю оновити чат (ім'я, фото)")
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
    @Operation(summary = "Частково оновити чат (ім'я або фото)")
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
    @Operation(summary = "Видалити чат")
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
}
