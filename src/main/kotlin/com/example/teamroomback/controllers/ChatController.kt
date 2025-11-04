package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.*
import com.example.teamroomback.entities.ChatType
import com.example.teamroomback.services.ChatService
import com.example.teamroomback.validation.ChatTypeAspect
import io.swagger.v3.oas.annotations.Operation
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

    @PostMapping("/private")
    @Operation(summary = "Створити новий приватний чат", description = "Створення нового приватного чату між двома користувачами. Чат не має окремої назви та фото. Можна створити якщо такого чату ще немає або користувач видалив в себе цей чат. Якщо чат видалений лише в одного, то в одного чат користувача чат створюється, а в іншого і далі існує.", tags = ["Чати - керування чатами", "Чати, приватні"])
    @ApiResponse(responseCode = "201", description = "Чат успішно створено")
    fun createPrivateChat(@Valid @RequestBody request: CreatePrivateChatRequest): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val chat = chatService.createPrivateChat(username, request)
            ResponseEntity.status(HttpStatus.CREATED)
                .body(CreateChatResponse(chat.id!!, "Private chat created successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Error creating chat"))
        } catch (e: IllegalArgumentException) {
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
    @ChatTypeAspect(ChatType.GROUP, ChatType.COURSE_CHAT, ChatType.MAIN_COURSE_CHAT)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Повністю оновити чат (ім'я, фото)", description = "Потребує ролі не нижче ADMIN. Чат повинний бути GROUP/COURSE_CHAT.", tags = ["Чати - керування чатами", "Курси, чати - керування чатами курсів", "Чати, курси - керування чатами курсів"])
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
    @ChatTypeAspect(ChatType.GROUP, ChatType.COURSE_CHAT)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Частково оновити чат (ім'я або фото)", description = "Потребує ролі не нижче ADMIN. Чат повинний бути GROUP/COURSE_CHAT.", tags = ["Чати - керування чатами", "Курси, чати - керування чатами курсів", "Чати, курси - керування чатами курсів"])
    @ApiResponse(responseCode = "200", description = "Чат оновлено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    @ApiResponse(responseCode = "404", description = "Чат не знайдено")
    fun patchChat(@PathVariable chatId: Long, @Valid @RequestBody request: PatchChatRequest): ResponseEntity<Any> {
        return try {
            val patchedChat = chatService.patchChat(chatId, request)
            ResponseEntity.ok(SimpleChatResponse(patchedChat.id!!, "Chat patched successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @DeleteMapping("/{chatId}")
    @ChatTypeAspect(ChatType.GROUP, ChatType.COURSE_CHAT)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'DELETE_CHAT')")
    @Operation(summary = "Видалити чат", description = "Потребує ролі не нижче OWNER. Чат повинний бути GROUP/COURSE_CHAT.", tags = ["Чати - керування чатами", "Курси, чати - керування чатами курсів", "Чати, курси - керування чатами курсів"])
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
    @ChatTypeAspect(ChatType.GROUP, ChatType.COURSE_CHAT, ChatType.MAIN_COURSE_CHAT)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримати список учасників чату", description = "Потребує ролі не нижче VIEWER. Чат повинний бути GROUP/COURSE_CHAT.", tags = ["Чати, учасники - керування учасниками чату", "Курси, чати - керування чатами курсів", "Чати, курси - керування чатами курсів"])
    @ApiResponse(responseCode = "200", description = "Список учасників")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено")
    fun getChatMembers(@PathVariable chatId: Long): ResponseEntity<List<ChatMemberDetailsDTO>> {
        val members = chatService.getChatMembers(chatId)
        return ResponseEntity.ok(members)
    }

    @PostMapping("/{chatId}/members")
    @ChatTypeAspect(ChatType.GROUP)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Додати нового учасника в чат", description = "Потребує ролі не нижче ADMIN. Чат повинний бути GROUP.", tags = ["Чати, учасники - керування учасниками чату"])
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
    @ChatTypeAspect(ChatType.GROUP)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Змінити роль учаснику чату", description = "Потребує ролі не нижче ADMIN. Чат повинний бути GROUP.", tags = ["Чати, учасники - керування учасниками чату"])
    @ApiResponse(responseCode = "200", description = "Роль оновлено")
    @ApiResponse(responseCode = "403", description = "Доступ заборонено (недостатньо прав для зміни)")
    @ApiResponse(responseCode = "404", description = "Учасника не знайдено")
    fun updateMemberRole(
        @PathVariable chatId: Long,
        @PathVariable username: String,
        @Valid @RequestBody request: UpdateChatMemberRoleRequest
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
    @ChatTypeAspect(ChatType.GROUP)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'ADMIN')")
    @Operation(summary = "Видалити учасника з чату", description = "Потребує ролі не нижче ADMIN. Чат повинний бути GROUP.", tags = ["Чати, учасники - керування учасниками чату"])
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
    @ChatTypeAspect(ChatType.GROUP)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Вийти з чату", description = "Потребує ролі не нижче VIEWER. Чат повинний бути GROUP.", tags = ["Чати, учасники - керування учасниками чату"])
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

    @DeleteMapping("/private/{chatId}/clear")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'MEMBER')")
    @ChatTypeAspect(ChatType.PRIVATE)
    @Operation(summary = "Очистити приватний чат", description = "Очистити історію повідомлень для себе або для обох. Опцію потрібно вказати в query clearForBoth(за замовчуванням false). Чат повинний бути PRIVATE.", tags = ["Чати, учасники - керування учасниками чату", "Чати, приватні"])
    @ApiResponse(responseCode = "200", description = "Чат успішно очищено")
    @ApiResponse(responseCode = "404", description = "Чат або учасника не знайдено")
    fun clearPrivateChat(@PathVariable chatId: Long, @RequestParam clearForBoth: Boolean = false): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            chatService.clearPrivateChat(chatId, username, clearForBoth)
            ResponseEntity.ok(SimpleMessageResponse("You have successfully cleared the chat"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @PostMapping("/{chatId}/transfer-ownership")
    @ChatTypeAspect(ChatType.GROUP)
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'OWNER')")
    @Operation(summary = "Передати права власності на чат", description = "Потребує ролі не нижче OWNER. Чат повинний бути GROUP.", tags = ["Чати, учасники - керування учасниками чату"])
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

    @GetMapping("/{chatId}/messages")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримання повідомлень в чаті", description = "Є три query параметри: messageId - id повідомлення відносно якого шукати повідомлення, limitBefore - к-сть повідомлень перед повідомленням з messageId для повернення, limitAfter - к-сть повідомлень після повідомлення з messageId для повернення. limitBefore обов`язковий; якщо не вказаний messageId то виведуться повідомлення відносно останнього повідомлення в чаті; якщо limitAfter не вказаний то не буде виведено жодного повідомлення після. Потребує ролі не нижче VIEWER.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно повернено")
    @ApiResponse(responseCode = "404", description = "Чат, повідомлення або учасника не знайдено")
    fun getMessagesInChat(
        @PathVariable chatId: Long,
        @RequestParam limitBefore: Int,
        @RequestParam messageId: Long? = null,
        @RequestParam limitAfter: Int = 0
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val messages = chatService.getMessagesInChat(chatId, messageId, limitBefore, limitAfter, username)

            ResponseEntity.ok(messages)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @GetMapping("/{chatId}/messages/{messageId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримання повідомлення в чаті", description = "Потребує ролі не нижче VIEWER.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно повернено")
    @ApiResponse(responseCode = "404", description = "Чат, повідомлення або учасника не знайдено")
    fun getMessageInChat(
        @PathVariable chatId: Long,
        @PathVariable messageId: Long
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val messages = chatService.getMessageInChat(chatId, messageId, username)

            ResponseEntity.ok(messages)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @GetMapping("/{chatId}/messages/last")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримання останнього повідомлення в чаті", description = "Потребує ролі не нижче VIEWER.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно повернено")
    @ApiResponse(responseCode = "404", description = "Чат або учасника не знайдено")
    fun getLastMessageInChat(
        @PathVariable chatId: Long
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name
            val messages = chatService.getMessageInChat(chatId, null, username)

            ResponseEntity.ok(messages)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }


    @GetMapping("/{chatId}/pinned")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'VIEWER')")
    @Operation(summary = "Отримати всі закріплені повідомлення в чаті", description = "Не повертає ті повідомлення, до яких користувач не має доступу(очистив чат до прикладу). Потребує ролі не нижче VIEWER.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно закріплено")
    @ApiResponse(responseCode = "404", description = "Чат, повідомлення або учасника не знайдено")
    fun getPinnedMessage(
        @PathVariable chatId: Long
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name

            val pinnedMessages = chatService.getPinnedMessages(chatId, username)

            ResponseEntity.ok(pinnedMessages)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        }
    }

    @PostMapping("/{chatId}/pinned")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'PIN_MESSAGE')")
    @Operation(summary = "Закріпити повідомлення в чаті", description = "Для не PRIVATE чатів потребує ролі не нижче MODERATOR.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно закріплено")
    @ApiResponse(responseCode = "400", description = "Повідомлення уже прикріплене")
    @ApiResponse(responseCode = "404", description = "Чат, повідомлення або учасника не знайдено")
    fun pinMessage(
        @PathVariable chatId: Long,
        @Valid @RequestBody request: PinMessageRequest
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name

            val pinnedMessage = chatService.pinMessage(chatId, username, request)

            ResponseEntity.ok(PinnedMessageResponse(pinnedMessage.message.id!!, "Message pinned successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Bad Request"))
        }
    }

    @DeleteMapping("/{chatId}/pinned/{messageId}")
    @PreAuthorize("@chatPermissionEvaluator.hasPermission(authentication, #chatId, 'PIN_MESSAGE')")
    @Operation(summary = "Відкріпити повідомлення в чаті", description = "Для не PRIVATE чатів потребує ролі не нижче MODERATOR.", tags = ["Чати - керування чатами", "Чати, повідомлення - керування повідомленнями чату"])
    @ApiResponse(responseCode = "200", description = "Повідомлення успішно відкріплено")
    @ApiResponse(responseCode = "400", description = "Повідомлення і так не прикріплене")
    @ApiResponse(responseCode = "404", description = "Чат, повідомлення або учасника не знайдено")
    fun pinMessage(
        @PathVariable chatId: Long,
        @PathVariable messageId: Long
    ): ResponseEntity<Any> {
        return try {
            val username = SecurityContextHolder.getContext().authentication.name

            val unpinnedMessage = chatService.unpinMessage(chatId, username, messageId)

            ResponseEntity.ok(PinnedMessageResponse(unpinnedMessage.message.id!!, "Message unpinned successfully"))
        } catch (e: EntityNotFoundException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(SimpleMessageResponse(e.message ?: "Not Found"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SimpleMessageResponse(e.message ?: "Bad Request"))
        }
    }


}
