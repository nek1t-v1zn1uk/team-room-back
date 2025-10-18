package com.example.teamroomback.services

import com.example.teamroomback.dtos.AddAssignmentMediaRequest
import com.example.teamroomback.dtos.CreateAssignmentRequest
import com.example.teamroomback.dtos.PatchAssignmentRequest
import com.example.teamroomback.dtos.PutAssignmentRequest
import com.example.teamroomback.dtos.RenameAssignmentMediaRequest
import com.example.teamroomback.entities.Assignment
import com.example.teamroomback.entities.AssignmentMedia
import com.example.teamroomback.repositories.AssignmentMediaRepository
import com.example.teamroomback.repositories.AssignmentRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.management.InstanceNotFoundException

@Service
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val assignmentMediaRepository: AssignmentMediaRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {

    @Transactional
    fun createAssignment(authorUsername: String, courseId: Long, request: CreateAssignmentRequest): Assignment {
        val author = userRepository.findByUsernameValue(authorUsername)
            ?: throw InstanceNotFoundException("User with username $authorUsername not found")
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id $courseId not found")

        val assignment = Assignment(
            course = course,
            author = author,
            title = request.title,
            description = request.description,
            maxGrade = request.maxGrade,
            deadline = request.deadline
        )
        return assignmentRepository.save(assignment)
    }

    fun getCourseAssignments(courseId: Long): List<Assignment> {
        if (!courseRepository.existsById(courseId)) {
            throw InstanceNotFoundException("Course with id $courseId not found")
        }
        return assignmentRepository.findByCourseId(courseId)
    }

    fun getAssignment(assignmentId: Long): Assignment {
        return assignmentRepository.findById(assignmentId)
            .orElseThrow { InstanceNotFoundException("Assignment with id $assignmentId not found") }
    }

    @Transactional
    fun putAssignment(assignmentId: Long, request: PutAssignmentRequest): Assignment {
        val assignment = getAssignment(assignmentId)

        assignment.title = request.title
        assignment.description = request.description
        assignment.maxGrade = request.maxGrade
        assignment.deadline = request.deadline

        return assignmentRepository.save(assignment)
    }

    @Transactional
    fun patchAssignment(assignmentId: Long, request: PatchAssignmentRequest): Assignment {
        val assignment = getAssignment(assignmentId)

        request.title?.let { assignment.title = it }
        request.description?.let { assignment.description = it }
        request.maxGrade?.let { assignment.maxGrade = it }
        request.deadline?.let { assignment.deadline = it }

        return assignmentRepository.save(assignment)
    }

    @Transactional
    fun deleteAssignment(assignmentId: Long): Assignment {
        val assignment = getAssignment(assignmentId)
        assignmentRepository.delete(assignment)
        return assignment
    }

    @Transactional
    fun addMedia(assignmentId: Long, request: AddAssignmentMediaRequest): AssignmentMedia {
        val assignment = getAssignment(assignmentId)
        val media = AssignmentMedia(
            assignment = assignment,
            name = request.name,
            fileUrl = request.fileUrl
        )
        return assignmentMediaRepository.save(media)
    }

    private fun getMedia(mediaId: Long): AssignmentMedia {
        return assignmentMediaRepository.findById(mediaId)
            .orElseThrow { InstanceNotFoundException("Assignment media with id $mediaId not found") }
    }

    @Transactional
    fun renameMedia(mediaId: Long, request: RenameAssignmentMediaRequest): AssignmentMedia {
        val media = getMedia(mediaId)
        media.name = request.name
        return assignmentMediaRepository.save(media)
    }

    @Transactional
    fun deleteMedia(mediaId: Long): AssignmentMedia {
        val media = getMedia(mediaId)
        assignmentMediaRepository.delete(media)
        return media
    }
}