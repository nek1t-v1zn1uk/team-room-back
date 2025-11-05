package com.example.teamroomback.services

import com.example.teamroomback.dtos.AddCourseMemberRequest
import com.example.teamroomback.dtos.CreateCourseChatRequest
import com.example.teamroomback.dtos.CreateCourseRequest
import com.example.teamroomback.dtos.PatchCourseRequest
import com.example.teamroomback.dtos.PutCourseMemberRoleRequest
import com.example.teamroomback.dtos.PutCourseRequest
import com.example.teamroomback.dtos.UserChatDTO
import com.example.teamroomback.entities.Chat
import com.example.teamroomback.entities.ChatMember
import com.example.teamroomback.entities.ChatMemberRole
import com.example.teamroomback.entities.ChatMessage
import com.example.teamroomback.entities.ChatMessageType
import com.example.teamroomback.entities.ChatType
import com.example.teamroomback.entities.Course
import com.example.teamroomback.entities.CourseMember
import com.example.teamroomback.entities.CourseMemberRole
import com.example.teamroomback.repositories.ChatMemberRepository
import com.example.teamroomback.repositories.ChatMessageRepository
import com.example.teamroomback.repositories.ChatRepository
import com.example.teamroomback.repositories.CourseMemberRepository
import com.example.teamroomback.repositories.CourseRepository
import com.example.teamroomback.repositories.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.management.InstanceNotFoundException

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val courseMemberRepository: CourseMemberRepository,
    private val userRepository: UserRepository,
    private val profileService: ProfileService,
    private val webSocketNotificationService: WebSocketNotificationService,
    private val chatRepository: ChatRepository,
    private val chatMemberRepository: ChatMemberRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {

    fun getRoleInCourse(username: String, courseId: Long): CourseMemberRole? {
        return courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)?.role
    }

    fun createCourse(username: String, request: CreateCourseRequest): Course {
        val course = courseRepository.save(
            Course(
                name = request.name,
                photoUrl = request.photoUrl
            )
        )

        val user = userRepository.findByUsernameValue(username)
        val courseMember = courseMemberRepository.save(
            CourseMember(
                user = user!!,
                course = course,
                role = CourseMemberRole.OWNER,
            )
        )

        val mainCourseChat = Chat(
            name = null,
            photoUrl = null,
            type = ChatType.MAIN_COURSE_CHAT,
            course = course
        )
        val savedChat = chatRepository.save(mainCourseChat)
        val member = ChatMember(chat = savedChat, user = user, role = ChatMemberRole.OWNER)
        chatMemberRepository.save(member)

        return course
    }

    fun getCourseById(courseId: Long): Course {
        return courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
    }

    fun putCourse(courseId: Long, request: PutCourseRequest): Course {
        val course = getCourseById(courseId)
        course.name = request.name
        course.photoUrl = request.photoUrl

        val newCourse = courseRepository.save(course)

        for(member in course.courseMembers) {
            webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }

        return newCourse
    }

    fun patchCourse(courseId: Long, request: PatchCourseRequest): Course {
        val course = getCourseById(courseId)
        request.name?.let { course.name = it }
        request.photoUrl?.let { course.photoUrl = it }

        val newCourse = courseRepository.save(course)

        for(member in course.courseMembers) {
            webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }

        return newCourse
    }

    fun openCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
        course.isOpen = true
        courseRepository.save(course)

        webSocketNotificationService.saveAndSendSystemMessageInMainCourseChat(courseId, ChatMessageType.COURSE_OPENED)

        for(member in course.courseMembers) {
            webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }
    }

    fun closeCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)
            ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found")
        course.isOpen = false
        courseRepository.save(course)

        webSocketNotificationService.saveAndSendSystemMessageInMainCourseChat(courseId, ChatMessageType.COURSE_CLOSED)

        for(member in course.courseMembers) {
            webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }
    }

    fun deleteCourse(courseId: Long) {
        val course = courseRepository.findCourseById(courseId)

        courseRepository.deleteById(courseId)

        if(course != null) {
            for(member in course.courseMembers) {
                webSocketNotificationService.notifyUserAboutCourseDeletion(member)
            }
        }
    }

    fun getUserCourses(username: String): List<Course> {
        val courses = courseRepository.findCoursesByUsername(username)
        return courses
    }

    fun addCourseMember(username: String, courseId: Long, request: AddCourseMemberRequest): CourseMember {
        // no more than one owner
        if (request.role == CourseMemberRole.OWNER)
            throw IllegalArgumentException("You dont have permission to add members with role \"${request.role}\"")

        // user(who is being added) must exist
        val user = userRepository.findByUsernameValue(request.username)
            ?: throw InstanceNotFoundException("User with username \"${request.username}\" not found")

        // user(who is being added) must have profile
        if (!profileService.hasProfile(request.username))
            throw InstanceNotFoundException("Profile for user with username \"${request.username}\" not found")

        val currentMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
            ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to add user
        if (!currentMember.role.canManage(request.role))
            throw IllegalAccessException("You dont have permission to add members with role \"${request.role}\"")

        // user(who is being added) must not be a member of course
        if (courseMemberRepository.findByUserUsernameValueAndCourseId(request.username, courseId) != null)
            throw IllegalArgumentException("User with username \"${request.username}\" is already a member of course with id \"${courseId}\"")


        val courseMember = courseMemberRepository.save(
            CourseMember(
                user = user,
                course = courseRepository.findCourseById(courseId)
                    ?: throw InstanceNotFoundException("Course with id \"${courseId}\" not found"),
                role = request.role,
            )
        )


        val chats = chatRepository.findChatsByCourseId(courseId)
        val chatRole = when(courseMember.role) {
            CourseMemberRole.OWNER -> ChatMemberRole.OWNER
            CourseMemberRole.PROFESSOR -> ChatMemberRole.ADMIN
            CourseMemberRole.LEADER -> ChatMemberRole.MODERATOR
            CourseMemberRole.STUDENT -> ChatMemberRole.MEMBER
            CourseMemberRole.VIEWER -> ChatMemberRole.VIEWER
        }
        for(chat in chats) {
            val member = ChatMember(chat = chat, user = user, role = chatRole)
            chatMemberRepository.save(member)
        }


        webSocketNotificationService.notifyUserAboutJoiningToCourse(courseMember)
        for(member in courseMember.course.courseMembers) {
            if(member.id != courseMember.id)
                webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }

        return courseMember
    }

    fun changeCourseMemberRole(username: String, courseId: Long, request: PutCourseMemberRoleRequest): CourseMember {
        // no more than one owner
        if (request.role == CourseMemberRole.OWNER)
            throw IllegalArgumentException("You dont have permission to set role \"${request.role}\"")

        val currentMember = courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
            ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to change member`s role
        if (!currentMember.role.canManage(request.role))
            throw IllegalAccessException("You dont have permission to set member`s role to \"${request.role}\"")

        val changingCourseMember = courseMemberRepository.findByUserUsernameValueAndCourseId(request.username, courseId)
            ?: throw IllegalArgumentException("User with username \"${request.username}\" must be a member of course with id \"${courseId}\"")

        // user must have enough rights to change member with role
        if (!currentMember.role.canManage(changingCourseMember.role))
            throw IllegalAccessException("You dont have permission to change member with role \"${request.role}\"")

        val oldRole = changingCourseMember.role
        changingCourseMember.role = request.role
        val newMember = courseMemberRepository.save(changingCourseMember)


        val membershipInCourseChats = chatMemberRepository.findChatsByChatCourseIdAndUserUsernameValue(courseId, request.username)
        val chatRole = when(newMember.role) {
            CourseMemberRole.OWNER -> ChatMemberRole.OWNER
            CourseMemberRole.PROFESSOR -> ChatMemberRole.ADMIN
            CourseMemberRole.LEADER -> ChatMemberRole.MODERATOR
            CourseMemberRole.STUDENT -> ChatMemberRole.MEMBER
            CourseMemberRole.VIEWER -> ChatMemberRole.VIEWER
        }
        for(member in membershipInCourseChats) {
            member.role = chatRole
            chatMemberRepository.save(member)
        }


        webSocketNotificationService.notifyUserAboutRoleChangeInCourse(newMember, oldRole.name)
        for(member in newMember.course.courseMembers) {
            if(member.id != newMember.id)
                webSocketNotificationService.notifyUserAboutCourseUpdate(member)
        }

        return newMember
    }

    fun deleteCourseMember(adminUsername: String, courseId: Long, memberUsername: String) {
        val adminMember = courseMemberRepository.findByUserUsernameValueAndCourseId(adminUsername, courseId)
            ?: throw InstanceNotFoundException("User with username \"$adminUsername\" is not a member of course with id \"${courseId}\"")
        val member =  courseMemberRepository.findByUserUsernameValueAndCourseId(memberUsername, courseId)
        ?: throw InstanceNotFoundException("User with username \"$memberUsername\" is not a member of course with id \"${courseId}\"")
        // invitor must have enough rights to delete member
        if (!adminMember.role.canManage(member.role))
            throw IllegalAccessException("You dont have permission to delete member with role \"${member.role}\"")

        courseMemberRepository.delete(member)


        val membershipInChat = chatMemberRepository.findChatsByChatCourseIdAndUserUsernameValue(courseId, memberUsername)
        for(member in membershipInChat) {
            chatMemberRepository.delete(member)
        }


        webSocketNotificationService.notifyUserAboutRemovalFromCourse(member)
        for(m in member.course.courseMembers) {
            if(m.id != member.id)
                webSocketNotificationService.notifyUserAboutCourseUpdate(m)
        }
    }

    fun leaveCourseByMember(username: String, courseId: Long) {
        val member =  courseMemberRepository.findByUserUsernameValueAndCourseId(username, courseId)
        ?: throw InstanceNotFoundException("User with username \"$username\" is not a member of course with id \"${courseId}\"")

        courseMemberRepository.delete(member)


        val membershipInCourseChats = chatMemberRepository.findChatsByChatCourseIdAndUserUsernameValue(courseId, username)
        for(member in membershipInCourseChats) {
            chatMemberRepository.delete(member)
        }


        webSocketNotificationService.notifyUserAboutRemovalFromCourse(member)
        for(m in member.course.courseMembers) {
            if(m.id != member.id)
                webSocketNotificationService.notifyUserAboutCourseUpdate(m)
        }
    }


    @Transactional
    fun createChatInCourseDTO(courseId: Long, creatorUsername: String, request: CreateCourseChatRequest): Chat {
        val creator = userRepository.findByUsernameValue(creatorUsername)
            ?: throw EntityNotFoundException("Creator user not found")
        val course = courseRepository.findCourseById(courseId)
            ?: throw EntityNotFoundException("Course not found")

        val chat = Chat(
            name = request.name,
            photoUrl = request.photoUrl,
            type = ChatType.COURSE_CHAT,
            course = course
        )
        val savedChat = chatRepository.save(chat)

        val members = course.courseMembers.map { courseMember ->
            val user = userRepository.findByUsernameValue(courseMember.user.username)!!
            val role = when(courseMember.role) {
                CourseMemberRole.OWNER -> ChatMemberRole.OWNER
                CourseMemberRole.PROFESSOR -> ChatMemberRole.ADMIN
                CourseMemberRole.LEADER -> ChatMemberRole.MODERATOR
                CourseMemberRole.STUDENT -> ChatMemberRole.MEMBER
                CourseMemberRole.VIEWER -> ChatMemberRole.VIEWER
            }
            ChatMember(chat = savedChat, user = user, role = role)
        }

        chatMemberRepository.saveAll(members)
        return savedChat
    }

    @Transactional(readOnly = true)
    fun getCourseChatsDTOs(chatId: Long): List<UserChatDTO> {
        val chats = chatRepository.findChatsByCourseId(chatId)
        return chats.map { it.toUserChatDTO() }
    }

}