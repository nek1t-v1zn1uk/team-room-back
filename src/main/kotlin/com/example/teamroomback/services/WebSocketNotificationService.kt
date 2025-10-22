package com.example.teamroomback.services

import com.example.teamroomback.dtos.WebSocketBroadcast
import com.example.teamroomback.dtos.WebSocketMessageType
import com.example.teamroomback.entities.CourseMember
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketNotificationService(
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val userService: UserService
) {

    private fun sentAsUserNotification(username: String, payload: WebSocketBroadcast) {
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
        sentAsUserNotification(member.user.username, message)
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
        sentAsUserNotification(member.user.username, message)
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
        sentAsUserNotification(member.user.username, message)
    }

    fun notifyUserAboutCourseDeletion(member: CourseMember) {
        val message = WebSocketBroadcast(
            type = WebSocketMessageType.COURSE_DELETED,
            payload = mapOf(
                "course_id" to member.course.id,
                "course_name" to member.course.name,
                "course_photoUrl" to member.course.photoUrl,
                "role" to member.role,
            )
        )
        sentAsUserNotification(member.user.username, message)
    }

}