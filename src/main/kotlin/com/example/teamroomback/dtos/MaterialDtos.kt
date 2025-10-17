package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Schema(description = "DTO для відображення повного навчального матеріалу.")
data class MaterialDTO(
    @field:Schema(description = "Унікальний ідентифікатор матеріалу.", example = "501")
    val id: Long,

    @field:Schema(description = "Тема або назва матеріалу.", example = "Лекція 1: Вступ до баз даних")
    val topic: String,

    @field:Schema(description = "Основний текстовий контент матеріалу.", example = "У цій лекції ми розглянемо...", nullable = true)
    val textContent: String? = null,

    @field:Schema(description = "Час створення матеріалу.", example = "2025-10-18T10:00:00")
    val createdAt: LocalDateTime,

    @field:Schema(description = "Список тегів, пов'язаних з матеріалом.")
    val tags: List<TagDTO> = listOf(),

    @field:Schema(description = "Список прикріплених медіафайлів.")
    val media: List<MediaDTO> = listOf(),

    @field:Schema(description = "Логін автора матеріалу.", example = "teacher_smith")
    val authorUsername: String,
)

@Schema(description = "DTO для відображення прикріпленого медіафайлу.")
data class MediaDTO(
    @field:Schema(description = "Унікальний ідентифікатор медіафайлу.", example = "12")
    val id: Long,

    @field:Schema(description = "Назва файлу, надана користувачем.", example = "Слайди до лекції 1", nullable = true)
    val name: String? = null,

    @field:Schema(description = "Пряме посилання на файл.", example = "https://example.com/files/lecture1.pdf", format = "uri")
    val fileUrl: String,
)

@Schema(description = "DTO для відображення тегу.")
data class TagDTO(
    @field:Schema(description = "Назва тегу.", example = "бази_даних", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 20)
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 20, message = "Name must be up to 20 characters")
    val name: String
)

@Schema(description = "DTO для представлення медіафайлу в запитах.")
data class MediaRequest(
    @field:Schema(description = "Назва файлу.", example = "Додаткові матеріали", nullable = true, maxLength = 255)
    @field:Size(max = 255, message = "Name must be up to 255 characters")
    val name: String? = null,

    @field:Schema(description = "Пряме посилання на файл.", example = "https://example.com/files/extra.zip", requiredMode = Schema.RequiredMode.REQUIRED, format = "uri")
    @field:NotBlank(message = "File url cannot be empty")
    val fileUrl: String,
)

data class CreateMaterialRequest(
    @field:Schema(description = "Тема нового матеріалу.", example = "Лекція 2: Моделі даних", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank(message = "Topic cannot be empty")
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String,

    @field:Schema(description = "Текстовий контент.", example = "Розглянемо реляційну та NoSQL моделі...", nullable = true)
    val textContent: String? = null,

    @field:Schema(description = "Початковий список тегів.")
    val tags: List<TagDTO> = listOf(),

    @field:Schema(description = "Початковий список прикріплених медіафайлів.")
    val media: List<MediaRequest> = listOf(),
)

data class CreateMaterialResponse(
    @field:Schema(description = "ID створеного матеріалу.", example = "502")
    val id: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Матеріал успішно створено.")
    val message: String,
)

data class PutMaterialRequest(
    @field:Schema(description = "Нова тема матеріалу.", example = "Лекція 2: Реляційна модель даних", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    @field:NotBlank(message = "Topic cannot be empty")
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String,

    @field:Schema(description = "Новий текстовий контент.", nullable = true)
    val textContent: String? = null,

    @field:Schema(description = "Новий список тегів (повністю замінює старий).")
    val tags: List<TagDTO> = listOf(),

    @field:Schema(description = "Новий список медіафайлів (повністю замінює старий).")
    val media: List<MediaRequest> = listOf(),
)

data class PutMaterialResponse(
    @field:Schema(description = "ID оновленого матеріалу.", example = "502")
    val id: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Матеріал успішно оновлено.")
    val message: String,
)

data class PatchMaterialRequest(
    @field:Schema(description = "Нова тема матеріалу.", nullable = true, maxLength = 100)
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String? = null,

    @field:Schema(description = "Новий текстовий контент.", nullable = true)
    val textContent: String? = null,

    @field:Schema(description = "Новий список тегів (замінює старий).", nullable = true)
    val tags: List<TagDTO>? = null,

    @field:Schema(description = "Новий список медіафайлів (замінює старий).", nullable = true)
    val media: List<MediaRequest>? = null,
)

data class PatchMaterialResponse(
    @field:Schema(description = "ID оновленого матеріалу.", example = "502")
    val id: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Матеріал успішно оновлено.")
    val message: String,
)

data class DeleteMaterialResponse(
    @field:Schema(description = "ID видаленого матеріалу.", example = "502")
    val id: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Матеріал успішно видалено.")
    val message: String,
)

data class AddMediaRequest(
    @field:Schema(description = "Назва файлу.", example = "Презентація", nullable = true, maxLength = 255)
    @field:Size(max = 255, message = "Name must be up to 255 characters")
    val name: String? = null,

    @field:Schema(description = "Пряме посилання на файл.", example = "https://example.com/files/presentation.pptx", requiredMode = Schema.RequiredMode.REQUIRED, format = "uri")
    @field:NotBlank(message = "File url cannot be empty")
    val fileUrl: String,
)

data class AddMediaResponse(
    @field:Schema(description = "ID нового медіафайлу.", example = "13")
    val id: Long,
    @field:Schema(description = "ID матеріалу, до якого додали файл.", example = "502")
    val materialId: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Медіафайл успішно додано.")
    val message: String,
)

data class RenameMediaRequest(
    @field:Schema(description = "Нова назва файлу.", example = "Презентація до лекції 2", nullable = true, maxLength = 255)
    @field:Size(max = 255, message = "Name must be up to 255 characters")
    val name: String? = null
)

data class RenameMediaResponse(
    @field:Schema(description = "ID перейменованого медіафайлу.", example = "13")
    val id: Long,
    @field:Schema(description = "ID відповідного матеріалу.", example = "502")
    val materialId: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Медіафайл успішно перейменовано.")
    val message: String,
)

data class DeleteMediaResponse(
    @field:Schema(description = "ID видаленого медіафайлу.", example = "13")
    val id: Long,
    @field:Schema(description = "ID відповідного матеріалу.", example = "502")
    val materialId: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Медіафайл успішно видалено.")
    val message: String,
)


data class AddTagRequest(
    @field:Schema(description = "Назва тегу.", example = "SQL", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 20)
    @field:NotBlank(message = "Name cannot be empty")
    @field:Size(max = 20, message = "Name must be up to 20 characters")
    val name: String
)

data class AddTagResponse(
    @field:Schema(description = "Назва доданого тегу.", example = "SQL")
    val name: String,
    @field:Schema(description = "ID матеріалу, до якого додали тег.", example = "502")
    val materialId: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Тег успішно додано.")
    val message: String,
)

data class DeleteTagResponse(
    @field:Schema(description = "Назва видаленого тегу.", example = "SQL")
    val name: String,
    @field:Schema(description = "ID відповідного матеріалу.", example = "502")
    val materialId: Long,
    @field:Schema(description = "Повідомлення про результат.", example = "Тег успішно видалено.")
    val message: String,
)