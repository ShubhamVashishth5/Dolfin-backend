package com.shubhamvashishth.DolFin.model

import java.net.http.WebSocket

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class TokenResponse(val accessToken: String, val refreshToken: String)
data class LogoutRequest(val username: String, val refreshToken: String)