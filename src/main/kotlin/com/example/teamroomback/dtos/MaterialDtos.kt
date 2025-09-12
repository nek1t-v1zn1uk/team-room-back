package com.example.teamroomback.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class MaterialDTO(
    val id: Long,
    val topic: String,
    val textContent: String? = null,
    val createdAt: LocalDateTime,
    val tags: List<TagDTO> = listOf(),
    val media: List<MediaDTO> = listOf(),
    val authorUsername: String,
)
data class MediaDTO(
    val id: Long,
    val name: String? = null,
    val fileUrl: String,
)
data class TagDTO(
    @field:Size(max = 20, message = "Name must be up to 20 characters")
    val name: String
)

data class MediaRequest(
    @field:Size(max = 255, message = "Name must be up to 255 characters")
    val name: String? = null,
    @NotBlank(message = "File url cannot be empty")
    val fileUrl: String,
)

data class CreateMaterialRequest(
    @NotBlank(message = "Topic cannot be empty")
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String,
    val textContent: String? = null,
    val tags: List<TagDTO> = listOf(),
    val media: List<MediaRequest> = listOf(),
)
data class CreateMaterialResponse(
    val id: Long,
    val message: String,
)

data class PutMaterialRequest(
    @NotBlank(message = "Topic cannot be empty")
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String,
    val textContent: String? = null,
    val tags: List<TagDTO> = listOf(),
    val media: List<MediaRequest> = listOf(),
)
data class PutMaterialResponse(
    val id: Long,
    val message: String,
)

data class PatchMaterialRequest(
    @field:Size(max = 100, message = "Topic must be up to 100 characters")
    val topic: String? = null,
    val textContent: String? = null,
    val tags: List<TagDTO>? = null,
    val media: List<MediaRequest>? = null,
)
data class PatchMaterialResponse(
    val id: Long,
    val message: String,
)

data class DeleteMaterialResponse(
    val id: Long,
    val message: String,
)

data class AddMediaRequest(
    @field:Size(max = 255, message = "Topic must be up to 255 characters")
    val name: String? = null,
    @NotBlank(message = "File url cannot be empty")
    val fileUrl: String,
)
data class AddMediaResponse(
    val id: Long,
    val materialId: Long,
    val message: String,
)

data class DeleteMediaResponse(
    val id: Long,
    val materialId: Long,
    val message: String,
)
