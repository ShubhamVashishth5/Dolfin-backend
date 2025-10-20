package com.shubhamvashishth.DolFin.respository

import com.shubhamvashishth.DolFin.entity.auth.UserTokens
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface UserTokenRepository : JpaRepository<UserTokens, String> {

    fun findByAccessToken(accessToken: String): UserTokens?

    fun findByRefreshToken(refreshToken: String): UserTokens?

    fun findByUsername(id: String): UserTokens?

    fun deleteByAccessToken(accessToken: String)

    fun deleteByRefreshToken(refreshToken: String)

    fun deleteByUsername(id: String)

    @Modifying
    @Query("DELETE FROM UserTokens t WHERE t.refreshTokenExpiry < :currentTime")
    fun deleteAllExpiredTokens(currentTime: LocalDateTime = LocalDateTime.now())

}