package com.example.teamroomback.validation

import com.example.teamroomback.entities.ChatType

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChatTypeAspect(vararg val requiredChatType: ChatType)