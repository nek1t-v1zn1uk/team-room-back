package com.example.teamroomback.services

import com.example.teamroomback.dtos.CreateMaterialRequest
import com.example.teamroomback.dtos.PatchMaterialRequest
import com.example.teamroomback.dtos.PutMaterialRequest
import com.example.teamroomback.entities.Material
import com.example.teamroomback.entities.MaterialMedia
import com.example.teamroomback.entities.MaterialTag
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.MaterialMediaRepository
import com.example.teamroomback.repositories.MaterialRepository
import com.example.teamroomback.repositories.MaterialTagRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.stereotype.Service
import javax.management.InstanceNotFoundException
import javax.print.attribute.standard.Media

@Service
class MaterialService(
    private val materialRepository: MaterialRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val mediaRepository: MaterialMediaRepository,
    private val tagRepository: MaterialTagRepository
) {
    fun createMaterial(username: String, courseId: Long, request: CreateMaterialRequest): Material {
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
        val user = userRepository.findByUsernameValue(username)
            ?: throw InstanceNotFoundException("User with username \"${username}\" not found")

        var material = materialRepository.save(Material(
            topic = request.topic,
            textContent = request.textContent,
            course = course,
            author = user
        ))

        for(mediaRequest in request.media) {
            val media = mediaRepository.save(MaterialMedia(
                name = mediaRequest.name,
                fileUrl = mediaRequest.fileUrl,
                material = material
            ))
        }
        for(tagRequest in request.tags) {
            val tag = tagRepository.save(MaterialTag(
                name = tagRequest.name,
                material = material
            ))
        }

        material = materialRepository.findMaterialById(material.id!!)!!

        return material
    }

    fun getMaterial(materialId: Long): Material {
        val material = materialRepository.findMaterialById(materialId)
            ?: throw InstanceNotFoundException("Material with id \"${materialId}\" not found")
        return material
    }

    fun getCourseMaterials(courseId: Long): List<Material> {
        val materials = materialRepository.findMaterialsByCourseId(courseId)
        return materials
    }

    fun putMaterial(username: String, courseId: Long, materialId: Long, request: PutMaterialRequest): Material {
        var material = materialRepository.findMaterialById(materialId)
            ?: throw InstanceNotFoundException("Material with id \"${materialId}\" not found")

        material.topic = request.topic
        material.textContent = request.textContent

        material = materialRepository.save(material)

        mediaRepository.deleteMaterialMediasByMaterialId(materialId)
        for(mediaRequest in request.media) {
            val media = mediaRepository.save(MaterialMedia(
                name = mediaRequest.name,
                fileUrl = mediaRequest.fileUrl,
                material = material
            ))
        }
        tagRepository.deleteMaterialTagsByMaterialId(materialId)
        for(tagRequest in request.tags) {
            val tag = tagRepository.save(MaterialTag(
                name = tagRequest.name,
                material = material
            ))
        }

        material = materialRepository.findMaterialById(material.id!!)!!

        return material
    }

    fun patchMaterial(username: String, courseId: Long, materialId: Long, request: PatchMaterialRequest): Material {
        var material = materialRepository.findMaterialById(materialId)
            ?: throw InstanceNotFoundException("Material with id \"${materialId}\" not found")

        request.topic?.let { material.topic = it }
        request.textContent?.let { material.textContent = it }

        material = materialRepository.save(material)

        request.media?.let {
            mediaRepository.deleteMaterialMediasByMaterialId(materialId)
            for (mediaRequest in it) {
                val media = mediaRepository.save(
                    MaterialMedia(
                        name = mediaRequest.name,
                        fileUrl = mediaRequest.fileUrl,
                        material = material
                    )
                )
            }
        }
        request.tags?.let {
            tagRepository.deleteMaterialTagsByMaterialId(materialId)
            for (tagRequest in it) {
                val tag = tagRepository.save(
                    MaterialTag(
                        name = tagRequest.name,
                        material = material
                    )
                )
            }
        }

        material = materialRepository.findMaterialById(material.id!!)!!

        return material
    }

    fun deleteMaterial(materialId: Long): Material {
        val material = materialRepository.findMaterialById(materialId)
            ?: throw InstanceNotFoundException("Material with id \"${materialId}\" not found")
        materialRepository.deleteMaterialById(materialId)
        return material
    }
}