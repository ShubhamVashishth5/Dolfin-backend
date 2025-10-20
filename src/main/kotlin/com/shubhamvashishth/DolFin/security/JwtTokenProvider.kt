package com.shubhamvashishth.DolFin.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("79eb78fb0c09e4e70cc03983add2de1794f77ad2d8e113abc6881b411abb730c") private val jwtSecret: String
) {

    private val accessTokenValidity = 15 * 60 * 1000 // 15 minutes
    private val refreshTokenValidity = 7 * 24 * 60 * 60 * 1000 // 7 days

    private val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateAccessToken(username: String, roles: List<String>): Pair<String, LocalDateTime> {

        val now = System.currentTimeMillis()
        val expiry = now + refreshTokenValidity

        val token = Jwts.builder()
            .setSubject(username)
            .claim("roles", roles)
            .setIssuedAt(Date())
            .setExpiration(Date(expiry)) // 15 min
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return Pair(token, Instant.ofEpochMilli(expiry).atZone(ZoneId.systemDefault()).toLocalDateTime())


    }

    fun generateRefreshToken(username: String): Pair<String, LocalDateTime> {
        val now = System.currentTimeMillis()
        val expiry = now + refreshTokenValidity

        val token = Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(expiry)) // 7 days
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return Pair(token, Instant.ofEpochMilli(expiry).atZone(ZoneId.systemDefault()).toLocalDateTime())


    }

    fun validateToken(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (ex: Exception) {
        false
    }

    fun getUsernameFromToken(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body.subject

    fun getRolesFromToken(token: String): List<String> {
        val claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).body
        val roles = claims["roles"] as? List<*> ?: emptyList<String>()
        return roles.filterIsInstance<String>()
    }
}
