package com.shubhamvashishth.DolFin.controller
import com.shubhamvashishth.DolFin.entity.auth.UserEntity
import com.shubhamvashishth.DolFin.entity.auth.UserTokens
import com.shubhamvashishth.DolFin.model.LoginRequest
import com.shubhamvashishth.DolFin.model.LogoutRequest
import com.shubhamvashishth.DolFin.model.RefreshRequest
import com.shubhamvashishth.DolFin.model.RegisterRequest
import com.shubhamvashishth.DolFin.model.TokenResponse
import com.shubhamvashishth.DolFin.repository.UserRepository
import com.shubhamvashishth.DolFin.respository.UserTokenRepository
import com.shubhamvashishth.DolFin.security.JwtTokenProvider
import com.shubhamvashishth.DolFin.service.CustomUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val userTokenRepository: UserTokenRepository,
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
        if (userRepository.findByUsername(req.username) != null) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Username already exists"))
        }

        val user = UserEntity(
            username = req.username,
            password = passwordEncoder.encode(req.password),
            roles = listOf("ROLE_USER")
        )

        userRepository.save(user)
        return ResponseEntity.ok(mapOf("message" to "User registered successfully"))
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<TokenResponse> {
        val user = userRepository.findByUsername(req.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!passwordEncoder.matches(req.password, user.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val (access, accessExpiry )= jwtTokenProvider.generateAccessToken(user.username, user.roles)
        val (refresh, refreshExpiry) = jwtTokenProvider.generateRefreshToken(user.username)
        userTokenRepository.save(UserTokens(access, refresh, user.username, accessExpiry, refreshExpiry))

        return ResponseEntity.ok(TokenResponse(access, refresh))
    }


    @PostMapping("/logout")
    fun logout(@RequestBody req: LogoutRequest): ResponseEntity<Unit> {
        val tokenEntry = userTokenRepository.findByRefreshToken(req.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if(tokenEntry.username != req.username) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        userTokenRepository.deleteByRefreshToken(req.refreshToken)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/logoutAll")
    fun logoutAll(@RequestBody req: LogoutRequest): ResponseEntity<Unit> {
        val tokenEntry = userTokenRepository.findByRefreshToken(req.refreshToken)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if(tokenEntry.username != req.username) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        userTokenRepository.deleteByUsername(req.username)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody req: RefreshRequest): ResponseEntity<TokenResponse> {
        if (!jwtTokenProvider.validateToken(req.refreshToken))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val username = jwtTokenProvider.getUsernameFromToken(req.refreshToken)
        val user = userRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val (newAccess, accessExpiry) = jwtTokenProvider.generateAccessToken(user.username, user.roles)
        val (newRefresh, refreshExpiry) = jwtTokenProvider.generateRefreshToken(user.username)
        userTokenRepository.deleteByRefreshToken(req.refreshToken)
        userTokenRepository.save(UserTokens(newAccess, newRefresh, user.username, accessExpiry, refreshExpiry))

        return ResponseEntity.ok(TokenResponse(newAccess, newRefresh))
    }

}
