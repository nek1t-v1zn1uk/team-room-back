package com.example.teamroomback.utils

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication(JWT)"

        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Введіть JWT токен, БЕЗ префіксу 'Bearer '")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .info(
                Info()
                    .title("TeamRoom API")
                    .version("v1.0")
            )
    }
}