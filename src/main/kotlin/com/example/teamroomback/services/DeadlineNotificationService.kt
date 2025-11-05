package com.example.teamroomback.services

import com.example.teamroomback.entities.Assignment
import com.example.teamroomback.entities.ChatMessage
import com.example.teamroomback.entities.ChatMessageRelatedEntityType
import com.example.teamroomback.entities.ChatMessageType
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import org.springframework.messaging.simp.SimpMessagingTemplate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture

@Service
class DeadlineNotificationService(
    private val taskScheduler: TaskScheduler,
    private val webSocketNotificationService: WebSocketNotificationService
) {
    private val warningTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()
    private val expirationTasks = ConcurrentHashMap<Long, ScheduledFuture<*>>()

    fun scheduleNotifications(assignment: Assignment) {

        val deadline: LocalDateTime = assignment.deadline
        val appZone = ZoneId.of("Europe/Kyiv")

        val expirationMoment: Instant = deadline.atZone(appZone).toInstant()
        val warningMoment: Instant = deadline.minusHours(24).atZone(appZone).toInstant()

        val now = Instant.now()

        cancelNotifications(assignment.id!!)

        if (warningMoment.isAfter(now)) {
            val warningTask = taskScheduler.schedule({
                sendNotification(assignment, "WARNING_24H")
            }, warningMoment)
            warningTasks[assignment.id!!] = warningTask
        }

        if (expirationMoment.isAfter(now)) {
            val expirationTask = taskScheduler.schedule({
                sendNotification(assignment, "EXPIRED")
            }, expirationMoment)
            expirationTasks[assignment.id!!] = expirationTask
        }
    }

    fun cancelNotifications(assignmentId: Long) {
        warningTasks.remove(assignmentId)?.cancel(false)
        expirationTasks.remove(assignmentId)?.cancel(false)
    }

    private fun sendNotification(assignment: Assignment, type: String) {
        webSocketNotificationService.saveAndSendSystemMessageInMainCourseChat(assignment.course.id!!,
            if(type == "EXPIRED") ChatMessageType.ASSIGNMENT_DEADLINE_ENDED else ChatMessageType.ASSIGNMENT_DEADLINE_IN_24HR,
            ChatMessageRelatedEntityType.ASSIGNMENT,
            assignment.id!!,
            mapOf("assignmentTitle" to assignment.title)
            )

        if (type == "WARNING_24H") warningTasks.remove(assignment.id)
        if (type == "EXPIRED") expirationTasks.remove(assignment.id)
    }
}