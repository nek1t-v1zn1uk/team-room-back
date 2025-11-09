package com.example.teamroomback.services

import com.example.teamroomback.entities.Conference
import com.example.teamroomback.entities.ConferenceParticipantRole
import com.example.teamroomback.entities.CourseMemberRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Service
class JitsiJwtService(
    @Value("\${JITSI_APP_ID}")
    private val jitsiAppId: String,
    @Value("\${JITSI_APP_SECRET}")
    private val jitsiSecret: String,
    @Value("\${JITSI_DOMAIN}")
    private val jitsiDomain: String,
    @Value("\${JITSI_TOKEN_LIFETIME}")
    private val tokenLifetime: Long
) {
    private lateinit var hmacKey: SecretKey

    @PostConstruct
    fun init() {
        this.hmacKey = Keys.hmacShaKeyFor(jitsiSecret.toByteArray())
    }

    data class JitsiUser(
        val username: String,
        val name: String,
        val email: String,
        val avatarUrl: String? = null,
        val role: ConferenceParticipantRole
    )


    fun generateToken(user: JitsiUser, roomName: String): String {

        val now = Instant.now()

        return Jwts.builder()
            .signWith(hmacKey, SignatureAlgorithm.HS256)
            .setHeaderParam("typ", "JWT")

            .setIssuer(jitsiAppId)
            .setAudience("jitsi")
            .setSubject(jitsiDomain)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plusSeconds(tokenLifetime)))

            .claim("room", roomName)

            .claim("context", mapOf(
                "user" to mapOf(
                    "id" to user.username,
                    "name" to user.name,
                    "email" to user.email,
                    "avatar" to user.avatarUrl,
                    "moderator" to when(user.role) {
                        ConferenceParticipantRole.MODERATOR -> true
                        ConferenceParticipantRole.MEMBER -> false
                        ConferenceParticipantRole.VIEWER -> false
                    }
                ),
            ))

            .compact()
    }

}