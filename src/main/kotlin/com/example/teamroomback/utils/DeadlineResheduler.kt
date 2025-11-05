package com.example.teamroomback.utils

import com.example.teamroomback.repositories.AssignmentRepository
import com.example.teamroomback.services.DeadlineNotificationService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class DeadlineRescheduler(
    private val assignmentRepository: AssignmentRepository,
    private val deadlineNotificationService: DeadlineNotificationService
) {

    @EventListener(ApplicationReadyEvent::class)
    fun rescheduleAllFutureDeadlines() {

        val futureAssignments = assignmentRepository.findByDeadlineAfter(LocalDateTime.now())

        futureAssignments.forEach { assignment ->
            deadlineNotificationService.scheduleNotifications(assignment)
        }
    }
}