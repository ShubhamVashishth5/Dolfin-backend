package com.shubhamvashishth.DolFin.repository

import com.shubhamvashishth.DolFin.entity.auth.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
}
