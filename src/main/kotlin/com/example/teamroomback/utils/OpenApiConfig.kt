package com.example.teamroomback.utils

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springdoc.core.customizers.OpenApiCustomizer

@Configuration
@OpenAPIDefinition(
    info = Info(title = "TeamRoom API", version = "v1.0", description = "API документація для додатку TeamRoom."),
    security = [SecurityRequirement(name = "bearerAuth")],
    tags = [
        Tag(name = "Аутентифікація"),
        Tag(name = "Базові Тести Доступу"),
        Tag(name = "Користувач"),
        Tag(name = "Профіль користувача"),
        Tag(name = "Хмарне сховище"),
        Tag(name = "Курси"),
        Tag(name = "Матеріали курсу"),

        Tag(name = "Завдання курсу"),
        Tag(name = "Завдання - керування завданнями курсу"),
        Tag(name = "Завдання, медіа - керування медіа в завданнях курсу"),
        Tag(name = "Завдання, відповіді - керування відповідями в завданнях курсу"),

        Tag(name = "Чати"),
        Tag(name = "Чати - керування чатами"),
        Tag(name = "Чати, учасники - керування учасниками чату"),
        Tag(name = "Чати, приватні"),
    ]
)
@SecurityScheme(
    name = "bearerAuth",
    description = "Введіть JWT токен, БЕЗ префіксу 'Bearer '",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig

@Configuration
class SwaggerTagOrderConfig {

    @Bean
    fun reorderTags(): OpenApiCustomizer {
        val tagOrder = listOf(
            "Аутентифікація",
            "Базові Тести Доступу",
            "Користувач",
            "Профіль користувача",
            "Хмарне сховище",
            "Курси",
            "Матеріали курсу",
            "Завдання курсу",
            "Завдання - керування завданнями курсу",
            "Завдання, медіа - керування медіа в завданнях курсу",
            "Завдання, відповіді - керування відповідями в завданнях курсу",
            "Чати",
            "Чати - керування чатами",
            "Чати, учасники - керування учасниками чату",
            "Чати, приватні",
        )

        return OpenApiCustomizer { openApi ->
            val currentTags = openApi.tags ?: return@OpenApiCustomizer
            openApi.tags = currentTags.sortedBy { tag ->
                tagOrder.indexOf(tag.name).takeIf { it >= 0 } ?: Int.MAX_VALUE
            }
        }
    }
}