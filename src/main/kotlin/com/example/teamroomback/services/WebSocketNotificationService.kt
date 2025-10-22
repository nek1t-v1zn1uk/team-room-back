package com.example.teamroomback.services

import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.Assignment
import com.example.teamroomback.entities.AssignmentResponse
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.Material
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketNotificationService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
) {

    private fun sendAsUserNotification(username: String, payload: WebSocketBroadcast) {
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/notifications", payload)
    }

    fun notifyUserAboutJoiningToCourse(member: CourseMember) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.JOINED_TO_COURSE,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "role" to member.role,
                "joined_at" to member.createdAt
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutRemovalFromCourse(member: CourseMember) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.REMOVED_FROM_COURSE,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutRoleChangeInCourse(member: CourseMember, oldRole: String) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ROLE_CHANGED_IN_COURSE,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "old_role" to oldRole,
                "new_role" to member.role,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutCourseUpdate(member: CourseMember) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.COURSE_UPDATED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutCourseDeletion(member: CourseMember) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.COURSE_DELETED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }


    fun notifyUserAboutMaterialCreation(member: CourseMember, material: Material) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.MATERIAL_CREATED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "material_id" to material.id,
                "material_topic" to material.topic,
                "author_username" to material.author.username,
                "author_first_name" to material.author.profile!!.firstName,
                "author_last_name" to material.author.profile!!.lastName,
                "author_photo_url" to material.author.profile!!.photoUrl,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutMaterialUpdate(member: CourseMember, material: Material) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.MATERIAL_UPDATED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "material_id" to material.id,
                "material_topic" to material.topic,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutMaterialDeletion(member: CourseMember, material: Material) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.MATERIAL_DELETED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "material_id" to material.id,
                "material_topic" to material.topic,
            )
        )
        sendAsUserNotification(member.user.username, message)
    }


    fun notifyUserAboutAssignmentCreation(username: String, assignment: Assignment) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_CREATED,
            payload = mapOf(
                "course_id" to assignment.course.id,
                "course_name" to assignment.course.name,
                "course_photoUrl" to assignment.course.photoUrl,
                "assignment_id" to assignment.id,
                "assignment_title" to assignment.title,
                "author_username" to assignment.author.username,
                "author_first_name" to assignment.author.profile!!.firstName,
                "author_last_name" to assignment.author.profile!!.lastName,
                "author_photo_url" to assignment.author.profile!!.photoUrl,
            )
        )
        sendAsUserNotification(username, message)
    }

    fun notifyUserAboutAssignmentUpdate(username: String, assignment: Assignment) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_UPDATED,
            payload = mapOf(
                "course_id" to assignment.course.id,
                "course_name" to assignment.course.name,
                "course_photoUrl" to assignment.course.photoUrl,
                "assignment_id" to assignment.id,
                "assignment_title" to assignment.title
            )
        )
        sendAsUserNotification(username, message)
    }

    fun notifyUserAboutAssignmentDeletion(username: String, assignment: Assignment) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_DELETED,
            payload = mapOf(
                "course_id" to assignment.course.id,
                "course_name" to assignment.course.name,
                "course_photoUrl" to assignment.course.photoUrl,
                "assignment_id" to assignment.id,
                "assignment_title" to assignment.title
            )
        )
        sendAsUserNotification(username, message)
    }

    fun notifyUserAboutAssignmentResponseCreation(username: String, assignmentResponse: AssignmentResponse) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_RESPONSE_CREATED,
            payload = mapOf(
                "course_id" to assignmentResponse.assignment.course.id,
                "course_name" to assignmentResponse.assignment.course.name,
                "course_photoUrl" to assignmentResponse.assignment.course.photoUrl,
                "assignment_id" to assignmentResponse.assignment.id,
                "assignment_title" to assignmentResponse.assignment.title,
                "response_assignment_id" to assignmentResponse.id,
                "response_author_username" to assignmentResponse.author.username,
                "response_author_first_name" to assignmentResponse.author.profile!!.firstName,
                "response_author_last_name" to assignmentResponse.author.profile!!.lastName,
                "response_author_photo_url" to assignmentResponse.author.profile!!.photoUrl,
            )
        )
        sendAsUserNotification(username, message)
    }

    fun notifyUserAboutAssignmentResponseUpdate(username: String, assignmentResponse: AssignmentResponse) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_RESPONSE_UPDATED,
            payload = mapOf(
                "course_id" to assignmentResponse.assignment.course.id,
                "course_name" to assignmentResponse.assignment.course.name,
                "course_photoUrl" to assignmentResponse.assignment.course.photoUrl,
                "assignment_id" to assignmentResponse.assignment.id,
                "assignment_title" to assignmentResponse.assignment.title,
                "response_assignment_id" to assignmentResponse.id,
            )
        )
        sendAsUserNotification(username, message)
    }

    fun notifyUserAboutAssignmentResponseDeletion(username: String, assignmentResponse: AssignmentResponse) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.ASSIGNMENT_RESPONSE_DELETED,
            payload = mapOf(
                "course_id" to assignmentResponse.assignment.course.id,
                "course_name" to assignmentResponse.assignment.course.name,
                "course_photoUrl" to assignmentResponse.assignment.course.photoUrl,
                "assignment_id" to assignmentResponse.assignment.id,
                "assignment_title" to assignmentResponse.assignment.title,
                "response_assignment_id" to assignmentResponse.id,
                "response_author_username" to assignmentResponse.author.username,
                "response_author_first_name" to assignmentResponse.author.profile!!.firstName,
                "response_author_last_name" to assignmentResponse.author.profile!!.lastName,
                "response_author_photo_url" to assignmentResponse.author.profile!!.photoUrl,
            )
        )
        sendAsUserNotification(username, message)
    }


}