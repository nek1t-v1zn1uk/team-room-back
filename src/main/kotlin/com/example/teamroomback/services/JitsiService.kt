package com.example.teamroomback.services

import com.example.teamroomback.dtos.JitsiEventDTO
import com.example.teamroomback.dtos.JitsiEventType
import org.springframework.stereotype.Service

@Service
class JitsiService(
    private val conferenceService: ConferenceService
) {

    fun handleEvent(request: JitsiEventDTO) {
        when(request.event) {
            JitsiEventType.user_joined -> conferenceService.userJoinedConference(request)
            JitsiEventType.user_left -> conferenceService.userLeftConference(request)
            JitsiEventType.conference_ended -> conferenceService.conferenceEnded(request)
        }
    }

}