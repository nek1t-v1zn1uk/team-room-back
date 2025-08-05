package com.example.teamroomback.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UsernameConstraintValidator::class])
annotation class ValidUsername(
    val message: String = "Username must not contain anys whitespaces.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UsernameConstraintValidator : ConstraintValidator<ValidUsername, String> {

    private val noWhitespace = "^\\S*$".toRegex()

    override fun isValid(username: String?, context: ConstraintValidatorContext?): Boolean {

        if (username.isNullOrBlank()) {
            return true
        }

        val isValid = username.matches(noWhitespace)

        return isValid
    }
}