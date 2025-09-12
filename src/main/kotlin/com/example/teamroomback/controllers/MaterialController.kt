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
class MaterialController (
    private val materialService: MaterialService,
) {

    @PostMapping
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    fun createMaterial(
        @PathVariable id: Long,
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
    fun getMaterial(@PathVariable id: Long, @PathVariable materialId: Long): ResponseEntity<Any> {
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
    fun getCourseMaterials(@PathVariable id: Long): ResponseEntity<Any> {
        return try {
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
    fun putMaterial(
        @PathVariable id: Long, @PathVariable materialId: Long,
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
    fun patchMaterial(
        @PathVariable id: Long, @PathVariable materialId: Long,
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
    fun deleteMaterial(@PathVariable id: Long, @PathVariable materialId: Long): ResponseEntity<Any> {
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
    fun addMedia(
        @PathVariable id: Long, @PathVariable materialId: Long,
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
    fun renameMedia(
        @PathVariable id: Long, @PathVariable materialId: Long, @PathVariable mediaId: Long,
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
    fun deleteMedia(
        @PathVariable id: Long, @PathVariable materialId: Long, @PathVariable mediaId: Long
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
    fun addTag(
        @PathVariable id: Long, @PathVariable materialId: Long,
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
    fun deleteTag(
        @PathVariable id: Long, @PathVariable materialId: Long, @PathVariable tagName: String
    ): ResponseEntity<Any> {
        return try {
            val authentication = SecurityContextHolder.getContext().authentication

            val tag = materialService.deleteTag(tagName)

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