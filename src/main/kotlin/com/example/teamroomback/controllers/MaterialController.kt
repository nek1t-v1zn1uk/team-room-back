package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.AddMediaRequest
import com.example.teamroomback.dtos.AddMediaResponse
import com.example.teamroomback.dtos.AddTagRequest
import com.example.teamroomback.dtos.AddTagResponse
import com.example.teamroomback.dtos.CreateMaterialRequest
import com.example.teamroomback.dtos.CreateMaterialResponse
import com.example.teamroomback.dtos.DeleteMaterialResponse
import com.example.teamroomback.dtos.DeleteMediaResponse
import com.example.teamroomback.dtos.DeleteTagResponse
import com.example.teamroomback.dtos.ErrorResponse
import com.example.teamroomback.dtos.MaterialDTO
import com.example.teamroomback.dtos.MediaDTO
import com.example.teamroomback.dtos.PatchMaterialRequest
import com.example.teamroomback.dtos.PatchMaterialResponse
import com.example.teamroomback.dtos.PutMaterialRequest
import com.example.teamroomback.dtos.PutMaterialResponse
import com.example.teamroomback.dtos.RenameMediaRequest
import com.example.teamroomback.dtos.RenameMediaResponse
import com.example.teamroomback.dtos.SimpleMessageResponse
import com.example.teamroomback.dtos.TagDTO
import com.example.teamroomback.entities.Material
import com.example.teamroomback.services.MaterialService
import com.example.teamroomback.validation.CourseOpenStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.SchemaProperty
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.management.InstanceNotFoundException

@RestController
@RequestMapping("/api/course/{id}/materials")
@Tag(name = "Матеріали курсу", description = "Ендпоїнти для керування навчальними матеріалами в межах курсу.")
@SecurityRequirement(name = "bearerAuth")
class MaterialController (
    private val materialService: MaterialService,
) {

    @PostMapping
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Створити новий матеріал у курсі.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Матеріал успішно створено.", content = [Content(schema = Schema(implementation = CreateMaterialResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun createMaterial(
        @Parameter(description = "ID курсу, до якого додається матеріал.", example = "101") @PathVariable id: Long,
        @Valid @RequestBody request: CreateMaterialRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val material = materialService.createMaterial(authentication.name, id, request)

            ResponseEntity.ok(
                CreateMaterialResponse(
                    id = material.id!!,
                    message = "Material created successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Material creation failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Material creation failed: ${e.message}.",
                )
            )
        }
    }

    @GetMapping("/{materialId}")
    @PreAuthorize("hasPermission(#id, 'VIEWER')")
    @Operation(summary = "Отримати матеріал за ID.", description = "Повертає один матеріал з усіма деталями. Потребує ролі не нижче VIEWER.")
    @ApiResponse(responseCode = "200", description = "Матеріал успішно отримано.", content = [Content(schema = Schema(implementation = MaterialDTO::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getMaterial(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val material = materialService.getMaterial(materialId)

            ResponseEntity.ok(
                MaterialDTO(
                    id = material.id!!,
                    topic = material.topic,
                    textContent = material.textContent,
                    createdAt = material.createdAt,
                    tags = material.tags.map { tag -> TagDTO(tag.name) },
                    media = material.media.map { media -> MediaDTO(
                        id = media.id!!,
                        name = media.name,
                        fileUrl = media.fileUrl,
                    ) },
                    authorUsername = material.author.username,
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Material searching and sharing failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Material searching and sharing failed: ${e.message}.",
                )
            )
        }
    }

    @GetMapping
    @PreAuthorize("hasPermission(#id, 'VIEWER')")
    @Operation(summary = "Отримати всі матеріали курсу.", description = "Повертає список всіх матеріалів курсу. Потребує ролі не нижче VIEWER.")
    @ApiResponse(
        responseCode = "200",
        description = "Матеріали курсу успішно отримано.",
        content = [Content(
            mediaType = "application/json",
            schemaProperties = [
                SchemaProperty(
                    name = "materials",
                    schema = Schema(type = "array", implementation = MaterialDTO::class)
                )
            ]
        )]
    )
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun getCourseMaterials(@Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long): ResponseEntity<Any> {    return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val materials = materialService.getCourseMaterials(id)

            ResponseEntity.ok(
                mapOf("materials" to
                    materials.map { material -> MaterialDTO(
                        id = material.id!!,
                        topic = material.topic,
                        textContent = material.textContent,
                        createdAt = material.createdAt,
                        authorUsername = material.author.username,
                    ) }
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Materials searching and sharing failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Materials searching and sharing failed: ${e.message}.",
                )
            )
        }
    }

    @PutMapping("/{materialId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Повністю оновити матеріал.", description = "Замінює всі дані матеріалу на нові. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Матеріал успішно оновлено.", content = [Content(schema = Schema(implementation = PutMaterialResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun putMaterial(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Valid @RequestBody request: PutMaterialRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val material = materialService.putMaterial(
                authentication.name,
                id,
                materialId,
                request
            )

            ResponseEntity.ok(
                PutMaterialResponse(
                    id = material.id!!,
                    message = "Material put successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Material putting failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Material putting failed: ${e.message}.",
                )
            )
        }
    }

    @PatchMapping("/{materialId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Частково оновити матеріал.", description = "Оновлює лише передані поля матеріалу. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Матеріал успішно оновлено.", content = [Content(schema = Schema(implementation = PatchMaterialResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun patchMaterial(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Valid @RequestBody request: PatchMaterialRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val material = materialService.patchMaterial(
                authentication.name,
                id,
                materialId,
                request
            )

            ResponseEntity.ok(
                PatchMaterialResponse(
                    id = material.id!!,
                    message = "Material patched successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Material patching failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Material patching failed: ${e.message}.",
                )
            )
        }
    }

    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Видалити матеріал.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Матеріал успішно видалено.", content = [Content(schema = Schema(implementation = DeleteMaterialResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteMaterial(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val material = materialService.deleteMaterial(materialId)

            ResponseEntity.ok(
                DeleteMaterialResponse(
                    id = material.id!!,
                    message = "Material deleted successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Material deletion failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Material deletion failed: ${e.message}.",
                )
            )
        }
    }


    @PostMapping("/{materialId}/media")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Додати медіафайл до матеріалу.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Медіафайл успішно додано.", content = [Content(schema = Schema(implementation = AddMediaResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun addMedia(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Valid @RequestBody request: AddMediaRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val media = materialService.addMedia(materialId, request)

            ResponseEntity.ok(
                AddMediaResponse(
                    id = media.id!!,
                    materialId = materialId,
                    message = "Media created successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Media creation failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Media creation failed: ${e.message}.",
                )
            )
        }
    }

    @PatchMapping("/{materialId}/media/{mediaId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Перейменувати медіафайл.", description = "Оновлює назву існуючого медіафайлу. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Медіафайл перейменовано.", content = [Content(schema = Schema(implementation = RenameMediaResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс, матеріал або медіафайл не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun renameMedia(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Parameter(description = "ID медіафайлу.", example = "12") @PathVariable mediaId: Long,
        @Valid @RequestBody request: RenameMediaRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val media = materialService.renameMedia(mediaId, request)

            ResponseEntity.ok(
                RenameMediaResponse(
                    id = media.id!!,
                    materialId = materialId,
                    message = "Media renamed successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Media renaming failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Media renaming failed: ${e.message}.",
                )
            )
        }
    }

    @DeleteMapping("/{materialId}/media/{mediaId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Видалити медіафайл.", description = "Видаляє медіафайл з матеріалу. Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Медіафайл видалено.", content = [Content(schema = Schema(implementation = DeleteMediaResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс, матеріал або медіафайл не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteMedia(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Parameter(description = "ID медіафайлу.", example = "12") @PathVariable mediaId: Long
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val media = materialService.deleteMedia(mediaId)

            ResponseEntity.ok(
                DeleteMediaResponse(
                    id = media.id!!,
                    materialId = materialId,
                    message = "Media deleted successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Media deletion failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Media deletion failed: ${e.message}.",
                )
            )
        }
    }


    @PostMapping("/{materialId}/tags")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Додати тег до матеріалу.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Тег успішно додано.", content = [Content(schema = Schema(implementation = AddTagResponse::class))])
    @ApiResponse(responseCode = "400", description = "Помилка валідації.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс або матеріал не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "409", description = "Конфлікт. Такий тег вже існує у матеріалі.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun addTag(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Valid @RequestBody request: AddTagRequest
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val tag = materialService.addTag(materialId, request)

            ResponseEntity.ok(
                AddTagResponse(
                    name = tag.name,
                    materialId = materialId,
                    message = "Tag added successfully",
                )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Tag adding failed: ${e.message}.",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Tag adding failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Tag adding failed: ${e.message}.",
                )
            )
        }
    }


    @DeleteMapping("/{materialId}/tags/{tagName}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    @Operation(summary = "Видалити тег з матеріалу.", description = "Потребує ролі не нижче PROFESSOR. Курс повинний бути відкритим.")
    @ApiResponse(responseCode = "200", description = "Тег успішно видалено.", content = [Content(schema = Schema(implementation = DeleteTagResponse::class))])
    @ApiResponse(responseCode = "401", description = "Неавторизований.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "403", description = "Доступ заборонено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "404", description = "Курс, матеріал або тег не знайдено.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    @ApiResponse(responseCode = "500", description = "Внутрішня помилка сервера.", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    fun deleteTag(
        @Parameter(description = "ID курсу.", example = "101") @PathVariable id: Long,
        @Parameter(description = "ID матеріалу.", example = "501") @PathVariable materialId: Long,
        @Parameter(description = "Назва тегу для видалення.", example = "бази_даних") @PathVariable tagName: String
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val tag = materialService.deleteTag(tagName, materialId)

            ResponseEntity.ok(
                DeleteTagResponse(
                    name = tag.name,
                    materialId = materialId,
                    message = "Tag deleted successfully",
                )
            )
        } catch (e: InstanceNotFoundException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                SimpleMessageResponse(
                    message = "Tag deletion failed: ${e.message}.",
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                SimpleMessageResponse(
                    message = "Tag deletion failed: ${e.message}.",
                )
            )
        }
    }

}