package com.example.teamroomback.controllers

import com.example.teamroomback.dtos.CreateMaterialRequest
import com.example.teamroomback.dtos.CreateMaterialResponse
import com.example.teamroomback.dtos.DeleteMaterialResponse
import com.example.teamroomback.dtos.MaterialDTO
import com.example.teamroomback.dtos.MediaDTO
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
                materials.map { material -> MaterialDTO(
                    id = material.id!!,
                    topic = material.topic,
                    textContent = material.textContent,
                    createdAt = material.createdAt,
                    authorUsername = material.author.username,
                ) }
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
    fun putMaterial(@PathVariable id: Long, @PathVariable materialId: Long){

    }
    @PatchMapping("/{materialId}")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    fun patchMaterial(@PathVariable id: Long, @PathVariable materialId: Long){

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
    fun addMedia(@PathVariable id: Long, @PathVariable materialId: Long){

    }
    @DeleteMapping("/{materialId}/media")
    @PreAuthorize("hasPermission(#id, 'PROFESSOR')")
    @CourseOpenStatus
    fun deleteMedia(@PathVariable id: Long, @PathVariable materialId: Long){

    }
}