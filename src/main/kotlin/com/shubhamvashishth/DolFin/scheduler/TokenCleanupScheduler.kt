package com.shubhamvashishth.DolFin.scheduler

import com.shubhamvashishth.DolFin.respository.UserTokenRepository
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TokenCleanupScheduler(val tokenRepository: UserTokenRepository) {

    @Transactional
    @Scheduled(fixedRate = 60 * 60 * 1000) // Runs every hour
    fun deleteExpiredTokens(){
        tokenRepository.deleteAllExpiredTokens()
    }

}