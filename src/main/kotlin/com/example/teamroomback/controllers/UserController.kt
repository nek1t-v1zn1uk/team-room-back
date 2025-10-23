package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.ChatMessageResponse
import com.example.teamroomback.dtos.CreateRoomResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.GetProfileResponse
import com.example.teamroomback.dtos.JoinRoomRequest
import com.example.teamroomback.dtos.JoinRoomResponse
import com.example.teamroomback.dtos.RoomRequest
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.UserResponse
import com.example.teamroomback.dtos.UsersListResponse
import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.User
import com.example.teamroomback.services.MessageService
import com.example.teamroomback.services.RoomService
import com.example.teamroomback.services.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Користувач", description = "Операції, пов'язані з поточним аутентифікованим користувачем.")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val userService: UserService,
    private val roomService: RoomService,
    private val messageService: MessageService
) {

    @DeleteMapping("/api/user")
    @Operation(summary = "Видалити поточного користувача.", description = "Видаляє профіль, дані та всі пов'язані записи для аутентифікованого користувача. Ця дія є незворотною.",)
    @ApiResponse(responseCode = "200", description = "Користувача успішно видалено.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований. Потрібно надати дійсний JWT токен.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено. Недостатньо прав для виконання операції.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteUser(): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication
            userService.deleteUser(authentication.name)

            ResponseEntity.ok(
                SimpleMessageResponse(
                    "User successfully deleted."
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    "User deletion failed: ${e.message}."
                )
            )
        }
    }

    @GetMapping("/api/user/search")
    @Operation(summary = "Шукати користувачів за частиною логіну.", description = "Повертає список усіх користувачів, чий логін містить вказаний текст. Ігнорується регістр тексту. Якщо таких користувачів немає, поверне пустий список.",)
    @ApiResponse(responseCode = "200", description = "Користувачів успішно повернуто.", content = [Content(schema = Schema(implementation = SimpleMessageResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований. Потрібно надати дійсний JWT токен.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено. Недостатньо прав для виконання операції.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun findUsersByPartialUsername(
        @Parameter(description = "Частина логіну за якою потрібно шукати.") @RequestParam partialUsername: String
    ): ResponseEntity<Any> {
        return try {
            val users = userService.finUsersByPartialUsername(partialUsername)


            ResponseEntity.ok(
                UsersListResponse(
                    users.map {
                        UserResponse(
                            it.usernameValue,
                            it.email,
                            GetProfileResponse(
                                it.profile!!.firstName,
                                it.profile!!.lastName,
                                it.profile!!.biography,
                                photoUrl = it.profile!!.photoUrl
                            )
                        )
                    }
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    "Users search failed: ${e.message}."
                )
            )
        }
    }


    @MessageMapping("/get-initial-data")
    fun getInitialData(headerAccessor: SimpMessageHeaderAccessor) {
        val username = headerAccessor.user!!.name

        val rooms = roomService.getUserRooms(username)

        for(room in rooms) {
            val message = WebSocketBroadcast(WebSocketMessageType.ROOM_CREATED, CreateRoomResponse(
                roomId = room.id!!,
                roomName = room.name,
                photoUrl = room.photoUrl,
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", message)
        }
    }
    @MessageMapping("/get-room-members")
    fun getRoomMembers(request: RoomRequest, headerAccessor: SimpMessageHeaderAccessor){
        val username = headerAccessor.user!!.name
        val roomId = request.roomId
        val members = roomService.getRoomMembers(roomId)

        for(member in members) {
            val message = WebSocketBroadcast(WebSocketMessageType.USER_JOINED, JoinRoomResponse(
                roomId = roomId,
                username = member.user.username,
                profile = GetProfileResponse(
                    firstName = member.user.profile!!.firstName,
                    lastName = member.user.profile!!.lastName,
                    biography = member.user.profile!!.biography,
                    photoUrl = member.user.profile!!.photoUrl
                ),
                role = member.role
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", message)
        }
    }
    @MessageMapping("/get-room-messages")
    fun getRoomMessages(request: RoomRequest, headerAccessor: SimpMessageHeaderAccessor){
        val username = headerAccessor.user!!.name
        val roomId = request.roomId
        val messages = messageService.getRoomMessages(roomId)

        for(msg in messages) {
            val broadcast = WebSocketBroadcast(WebSocketMessageType.CHAT_MESSAGE, ChatMessageResponse(
                senderUsername = msg.sender.username,
                roomId = msg.room.id!!,
                content = msg.content,
                type = msg.type
            ))
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", broadcast)
        }
    }
}