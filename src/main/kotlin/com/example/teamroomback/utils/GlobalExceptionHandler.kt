package com.example.teamroomback.utils

import com.example.teamroomback.dtos.ErrorResponse
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { error ->
            mapOf(
                "field" to (error.field ?: "unknown"),
                "message" to (error.defaultMessage ?: "Validation error")
            )
        }

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "Validation failed for one or more fields.",
            details = errors
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val errorMessage = ex.message?.let {
            if (it.contains("Missing required creator property")) {
                val fieldName = it.substringAfter("Missing required creator property '").substringBefore("'")
                "Required field '$fieldName' is missing."
            } else {
                "Malformed JSON request."
            }
        } ?: "Malformed JSON request."

        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = errorMessage,
            details = null
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    // Username or email already exists
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "An unexpected conflict occurred."
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        val response = ResponseEntity(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.FORBIDDEN.value(),
                error = HttpStatus.FORBIDDEN.reasonPhrase,
                message = if(ex.message == "Access Denied") "Access Denied: You do not have the required permissions." else ex.message
            ),
            HttpStatus.FORBIDDEN
        )
        return response
    }

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleEntityNotFoundException(ex: Exception): ResponseEntity<ErrorResponse> {
        val response = ResponseEntity(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.reasonPhrase,
                message = if(ex.message == "Not Found") "Not Found: The requested resource was not found." else ex.message
            ),
            HttpStatus.NOT_FOUND
        )
        return response
    }

    // A general handler for any other unexpected exceptions
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllUncaughtException(ex: Exception): ResponseEntity<ErrorResponse> {
        val response = ResponseEntity(
                ErrorResponse(
                    timestamp = LocalDateTime.now(),
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = "An unexpected error occurred. Please try again later."
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        // Log the full stack trace for debugging on the server side
        ex.printStackTrace()
        return response
    }
}
