package com.example.teamroomback.services

import com.example.teamroomback.dtos.*
import com.example.teamroomback.entities.Assignment
import com.example.teamroomback.entities.AssignmentMedia
import com.example.teamroomback.entities.AssignmentResponse
import com.example.teamroomback.entities.AssignmentResponseMedia
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.repositories.*
import org.apache.coyote.BadRequestException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.management.InstanceNotFoundException

@Service
class AssignmentService(
    private val assignmentRepository: AssignmentRepository,
    private val assignmentMediaRepository: AssignmentMediaRepository,
    private val assignmentResponseRepository: AssignmentResponseRepository,
    private val assignmentResponseMediaRepository: AssignmentResponseMediaRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val webSocketNotificationService: WebSocketNotificationService
) {

    @Transactional
    fun createAssignment(authorUsername: String, courseId: Long, request: CreateAssignmentRequest): Assignment {
        val author = userRepository.findByUsernameValue(authorUsername)
            ?: throw InstanceNotFoundException("User with username $authorUsername not found")
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id $courseId not found")

        val assignment = assignmentRepository.save(Assignment(
            course = course,
            author = author,
            title = request.title,
            description = request.description,
            maxGrade = request.maxGrade,
            deadline = request.deadline
        ))

        for(member in course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentCreation(member.user.username, assignment)
        }

        return assignment
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

        val newAssignment = assignmentRepository.save(assignment)

        for(member in newAssignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentUpdate(member.user.username, newAssignment)
        }

        return newAssignment
    }

    @Transactional
    fun patchAssignment(assignmentId: Long, request: PatchAssignmentRequest): Assignment {
        val assignment = getAssignment(assignmentId)

        request.title?.let { assignment.title = it }
        request.description?.let { assignment.description = it }
        request.maxGrade?.let { assignment.maxGrade = it }
        request.deadline?.let { assignment.deadline = it }

        val newAssignment = assignmentRepository.save(assignment)

        for(member in newAssignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentUpdate(member.user.username, newAssignment)
        }

        return newAssignment
    }

    @Transactional
    fun deleteAssignment(assignmentId: Long): Assignment {
        val assignment = getAssignment(assignmentId)
        assignmentRepository.delete(assignment)

        for(member in assignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentDeletion(member.user.username, assignment)
        }

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

        val newMedia = assignmentMediaRepository.save(media)

        for(member in assignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentUpdate(member.user.username, assignment)
        }

        return newMedia
    }

    private fun getMedia(mediaId: Long): AssignmentMedia {
        return assignmentMediaRepository.findById(mediaId)
            .orElseThrow { InstanceNotFoundException("Assignment media with id $mediaId not found") }
    }

    @Transactional
    fun renameMedia(mediaId: Long, request: RenameAssignmentMediaRequest): AssignmentMedia {
        val media = getMedia(mediaId)
        media.name = request.name

        val newMedia = assignmentMediaRepository.save(media)

        for(member in newMedia.assignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentUpdate(member.user.username, newMedia.assignment)
        }

        return newMedia
    }

    @Transactional
    fun deleteMedia(mediaId: Long): AssignmentMedia {
        val media = getMedia(mediaId)
        assignmentMediaRepository.delete(media)

        for(member in media.assignment.course.courseMembers) {
            webSocketNotificationService.notifyUserAboutAssignmentUpdate(member.user.username, media.assignment)
        }

        return media
    }


    @Transactional
    fun createAssignmentResponse(authorUsername: String, assignmentId: Long, request: CreateAssignmentResponseRequest): AssignmentResponse {
        val author = userRepository.findByUsernameValue(authorUsername)
            ?: throw InstanceNotFoundException("User with username $authorUsername not found")
        val assignment = getAssignment(assignmentId)

        if(assignmentResponseRepository.findByAssignmentIdAndAuthorId(assignmentId, author.id!!) != null)
            throw BadRequestException("Assignment response for assignment $assignmentId already exists")

        val response = AssignmentResponse(
            assignment = assignment,
            author = author
        )

        val savedResponse = assignmentResponseRepository.save(response)

        val mediaList = request.media.map {
            AssignmentResponseMedia(
                assignmentResponse = savedResponse,
                name = it.name,
                fileUrl = it.fileUrl
            )
        }
        assignmentResponseMediaRepository.saveAll(mediaList)

        for(member in assignment.course.courseMembers.filter {
            it.user.username == authorUsername || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseCreation(member.user.username, savedResponse)
        }

        return savedResponse
    }

    fun getAssignmentResponses(assignmentId: Long): List<AssignmentResponse> {
        val assignment = getAssignment(assignmentId)
        return assignment.responses
    }

    fun getAssignmentResponse(responseId: Long): AssignmentResponse {
        return assignmentResponseRepository.findById(responseId)
            .orElseThrow { InstanceNotFoundException("Assignment response with id $responseId not found") }
    }

    fun getMyAssignmentResponse(assignmentId: Long, username: String): AssignmentResponse {
        val user = userRepository.findByUsernameValue(username)
            ?: throw InstanceNotFoundException("User with username $username not found")
        return assignmentResponseRepository.findByAssignmentIdAndAuthorId(assignmentId, user.id!!)
            ?: throw InstanceNotFoundException("Assignment response for assignment $assignmentId by user $username not found")
    }

    fun getAllMyResponses(courseId: Long, username: String): List<AssignmentResponse> {
        val user = userRepository.findByUsernameValue(username)
            ?: throw InstanceNotFoundException("User with username $username not found")
        return assignmentResponseRepository.findByAuthorIdAndAssignmentCourseId(user.id!!, courseId)
    }

    @Transactional
    fun deleteAssignmentResponse(responseId: Long, username: String) {
        val response = getAssignmentResponse(responseId)
        if (response.author.username != username) {
            throw AccessDeniedException("You are not the author of this response.")
        }
        if (response.isGraded) {
            throw IllegalStateException("Cannot delete a graded response.")
        }
        assignmentResponseRepository.delete(response)

        for(member in response.assignment.course.courseMembers.filter {
            it.user.username == response.author.username || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseDeletion(member.user.username, response)
        }
    }

    @Transactional
    fun gradeAssignmentResponse(responseId: Long, request: GradeAssignmentResponseRequest): AssignmentResponse {
        val response = getAssignmentResponse(responseId)
        response.isGraded = true
        response.grade = request.grade
        response.gradeComment = request.gradeComment
        response.isReturned = false
        response.returnComment = null
        val savedResponse = assignmentResponseRepository.save(response)

        for(member in savedResponse.assignment.course.courseMembers.filter {
            it.user.username == savedResponse.author.username || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseUpdate(member.user.username, savedResponse)
        }

        return savedResponse
    }

    @Transactional
    fun returnAssignmentResponse(responseId: Long, request: ReturnAssignmentResponseRequest): AssignmentResponse {
        val response = getAssignmentResponse(responseId)
        if (response.isGraded) {
            throw IllegalStateException("Cannot return a graded response. Cancel the grade first.")
        }
        response.isReturned = true
        response.returnComment = request.returnComment
        val savedResponse = assignmentResponseRepository.save(response)

        for(member in savedResponse.assignment.course.courseMembers.filter {
            it.user.username == savedResponse.author.username || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseUpdate(member.user.username, savedResponse)
        }

        return savedResponse
    }

    @Transactional
    fun cancelGradeAssignmentResponse(responseId: Long): AssignmentResponse {
        val response = getAssignmentResponse(responseId)
        response.isGraded = false
        response.grade = null
        response.gradeComment = null
        val savedResponse = assignmentResponseRepository.save(response)

        for(member in savedResponse.assignment.course.courseMembers.filter {
            it.user.username == savedResponse.author.username || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseUpdate(member.user.username, savedResponse)
        }

        return savedResponse
    }

    @Transactional
    fun cancelReturnAssignmentResponse(responseId: Long): AssignmentResponse {
        val response = getAssignmentResponse(responseId)
        response.isReturned = false
        response.returnComment = null
        val savedResponse = assignmentResponseRepository.save(response)

        for(member in savedResponse.assignment.course.courseMembers.filter {
            it.user.username == savedResponse.author.username || it.role.isAtLeast(CourseMemberRole.PROFESSOR)
        }) {
            webSocketNotificationService.notifyUserAboutAssignmentResponseUpdate(member.user.username, savedResponse)
        }

        return savedResponse
    }
}