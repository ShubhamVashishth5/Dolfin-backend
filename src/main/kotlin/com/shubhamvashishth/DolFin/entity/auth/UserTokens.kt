package com.shubhamvashishth.DolFin.entity.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.Date

@Entity
@Table(name = "user_tokens")
data class UserTokens (

    @Id
    var accessToken: String = "",
    val refreshToken: String="",

    val username: String = "",

    @Column(nullable = false)
    val accessTokenExpiry: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val refreshTokenExpiry: LocalDateTime = LocalDateTime.now()

)