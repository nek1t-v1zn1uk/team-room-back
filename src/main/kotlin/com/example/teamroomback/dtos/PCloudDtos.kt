package com.example.teamroomback.dtos

import io.swagger.v3.oas.annotations.media.Schema

data class UploadLinkResponse(
    @field:Schema(
        description = "Унікальне посилання для завантаження файлу в хмарне сховище.",
        example = "https://upload.pcloud.com/upload?id=12345&code=AbCdEfGhIjK"
    )
    val link: String
)

data class DownloadLinkResponse(
    @field:Schema(
        description = "Публічне посилання для доступу (перегляду/завантаження) до файлу.",
        example = "https://file.pcloud.com/d/AbCdEfGhIjK"
    )
    val link: String
)