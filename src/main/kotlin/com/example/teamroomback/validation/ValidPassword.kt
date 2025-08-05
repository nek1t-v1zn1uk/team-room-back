package com.example.teamroomback.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordConstraintValidator::class])
annotation class ValidPassword(
    val message: String = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class PasswordConstraintValidator : ConstraintValidator<ValidPassword, String> {

    private val hasDigit = ".*\\d.*".toRegex()
    private val hasLowercase = ".*[a-z].*".toRegex()
    private val hasUppercase = ".*[A-Z].*".toRegex()
    private val hasSpecialChar = ".*[^a-zA-Z0-9].*".toRegex()

    override fun isValid(password: String?, context: ConstraintValidatorContext?): Boolean {

        if (password.isNullOrBlank()) {
            return true
        }

        val isValid = password.matches(hasDigit) &&
                password.matches(hasLowercase) &&
                password.matches(hasUppercase) &&
                password.matches(hasSpecialChar)

        return isValid
    }
}