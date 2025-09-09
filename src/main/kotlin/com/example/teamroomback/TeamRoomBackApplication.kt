package com.example.teamroomback

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
class TeamRoomBackApplication

fun main(args: Array<String>) {
    try {
        dotenv {
            directory = "./"
            filename = ".env"
        }.entries().forEach { entry ->
            System.setProperty(entry.key, entry.value)
        }
    } catch (ex: Exception) {}
    runApplication<TeamRoomBackApplication>(*args)
}
