package com.example.teamroomback.utils

import com.example.teamroomback.services.UserService
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ProfileCheckInterceptor(
    private val userService: UserService,
    private val objectMapper: ObjectMapper
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name

        if(
            request.requestURI == "/api/profile" && request.method == "POST" ||
            request.requestURI == "/api/user" && request.method == "DELETE"
            )
            return true

        if (!userService.hasProfile(username)) {
            val errorResponse = mapOf("message" to "User does not have a profile.")

            response.status = HttpStatus.FORBIDDEN.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE

            response.writer.write(objectMapper.writeValueAsString(errorResponse))

            return false
        }

        return true
    }
}