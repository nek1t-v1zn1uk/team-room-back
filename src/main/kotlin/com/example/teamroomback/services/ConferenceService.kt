package com.example.teamroomback.services

import com.example.teamroomback.dtos.ConferenceDTO
import com.example.teamroomback.dtos.ConferenceJoinDetails
import com.example.teamroomback.dtos.CreateConferenceRequest
import com.example.teamroomback.dtos.JitsiEventDTO
import com.example.teamroomback.dtos.ShortConferenceDTO
import com.example.teamroomback.entities.Conference
import com.example.teamroomback.entities.ConferenceParticipant
import com.example.teamroomback.entities.ConferenceParticipantRole
import com.example.teamroomback.entities.ConferenceStatus
import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.entities.User
import com.example.teamroomback.repositories.ConferenceParticipantRepository
import com.example.teamroomback.repositories.ConferenceRepository
import com.example.teamroomback.repositories.CourseMemberRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ConferenceService(
    private val jitsiJwtService: JitsiJwtService,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val conferenceRepository: ConferenceRepository,
    private val conferenceParticipantRepository: ConferenceParticipantRepository,
    private val courseMemberRepository: CourseMemberRepository,
) {
    val waitingConferences = mutableListOf<Conference>()

    private fun createConferenceJoinDetails(user: User, courseMember: CourseMember, roomName: String): ConferenceJoinDetails {
        val role = courseMember.role.toConferenceParticipantRole()

        val jitsiUser = JitsiJwtService.JitsiUser(
            user.username,
            user.profile!!.fullName,
            user.email,
            user.profile!!.photoUrl,
            role
        )
        val roomName = roomName

        val jwt = jitsiJwtService.generateToken(jitsiUser, roomName)

        return ConferenceJoinDetails(
            jwt,
            roomName,
            role
        )
    }

    @Transactional
    fun createConference(username: String, courseId: Long, request: CreateConferenceRequest): ConferenceJoinDetails {
        val course = courseRepository.findById(courseId)
            .orElseThrow{ EntityNotFoundException("Course with id $courseId not found") }
        val user = userRepository.findByUsernameValue(username)!!
        val courseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)!!

        val conference = Conference(
            course = course,
            subject = request.subject,
            status = ConferenceStatus.ACTIVE,
        )
        waitingConferences.add(conference)

        return createConferenceJoinDetails(user, courseMember, conference.roomName)
    }

    @Transactional(readOnly = true)
    fun getConferenceJoinDetails(username: String, courseId: Long, conferenceId: Long): ConferenceJoinDetails {
        val user = userRepository.findByUsernameValue(username)!!
        val courseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)!!
        val conference = conferenceRepository.findById(conferenceId)
            .orElseThrow{ EntityNotFoundException("Conference with id $conferenceId not found") }

        return createConferenceJoinDetails(user, courseMember, conference.roomName)
    }

    @Transactional(readOnly = true)
    fun getConferenceDTO(conferenceId: Long): ConferenceDTO {
        val conference = conferenceRepository.findById(conferenceId)
            .orElseThrow{ EntityNotFoundException("Conference with id $conferenceId not found") }

        return conference.toConferenceDTO()
    }

    @Transactional(readOnly = true)
    fun getCourseConferencesDTOs(courseId: Long): List<ConferenceDTO> {
        val conferences = conferenceRepository.findAllByCourseId(courseId)

        return conferences.map { it.toConferenceDTO() }
    }



    @Transactional
    fun userJoinedConference(request: JitsiEventDTO) {
        val user = userRepository.findByUsernameValue(request.userId!!)
            ?: throw EntityNotFoundException("User with username ${request.userId} not found")
        var conference = conferenceRepository.findByRoomName(request.roomName)
        if(conference == null) {
            val conf = waitingConferences.find { it.roomName == request.roomName }
            if(conf == null)
                throw EntityNotFoundException("Conference with roomName ${request.roomName} not found")

            conference = conferenceRepository.save(conf)
            waitingConferences.remove(conf)

            //TODO  notificate all course members about started conference
        }
        val courseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(request.userId, conference.course.id!!)
            ?: throw EntityNotFoundException("Course member with username ${request.userId} in course with id ${conference.course.id} not found")

        val existingParticipant = conferenceParticipantRepository.findByConferenceIdAndUserUsernameValue(conference.id!!, user.username)

        val conferenceParticipant =
            if(existingParticipant == null) conferenceParticipantRepository.save(ConferenceParticipant(
                user = user,
                conference = conference,
                role = courseMember.role.toConferenceParticipantRole()
            ))
            else {
                existingParticipant.leftAt = null
                existingParticipant.role = courseMember.role.toConferenceParticipantRole()
                conferenceParticipantRepository.save(existingParticipant)
            }

        //TODO notificate all course members about change of participant list in conference
    }

    @Transactional
    fun userLeftConference(request: JitsiEventDTO) {
        val user = userRepository.findByUsernameValue(request.userId!!)
            ?: throw EntityNotFoundException("User with username ${request.userId} not found")
        val conference = conferenceRepository.findByRoomName(request.roomName)
            ?: throw EntityNotFoundException("Conference with roomName ${request.roomName} not found")
        val courseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(request.userId, conference.course.id!!)
            ?: throw EntityNotFoundException("Course member with username ${request.userId} in course with id ${conference.course.id} not found")

        var existingParticipant = conferenceParticipantRepository.findByConferenceIdAndUserUsernameValue(conference.id!!, user.username)
            ?: throw EntityNotFoundException("Conference participant with username ${user.username} in conference with id ${conference.id} not found")


        existingParticipant.leftAt = LocalDateTime.now()

        existingParticipant = conferenceParticipantRepository.save(existingParticipant)


        //TODO notificate all course members about change of participant list in conference
    }

    @Transactional
    fun conferenceEnded(request: JitsiEventDTO) {
        val conference = conferenceRepository.findByRoomName(request.roomName)
            ?: throw EntityNotFoundException("Conference with roomName ${request.roomName} not found")

        conferenceParticipantRepository.saveAll(conference.participants.map {
            it.leftAt = LocalDateTime.now()
            it
        })

        conference.status = ConferenceStatus.ENDED
        conference.endedAt = LocalDateTime.now()

        conferenceRepository.save(conference)



        //TODO notificate all course members about end of conference
    }

}