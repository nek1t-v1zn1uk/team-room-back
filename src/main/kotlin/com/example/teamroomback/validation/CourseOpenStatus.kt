package com.example.teamroomback.validation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CourseOpenStatus(val requiredOpenStatus: Boolean = true)