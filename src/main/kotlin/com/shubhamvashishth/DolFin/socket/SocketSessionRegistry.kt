package com.shubhamvashishth.DolFin.socket

import com.corundumstudio.socketio.SocketIOClient
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
interface SocketSessionRegistry {
    fun register(userId: String, client: SocketIOClient)
    fun unregister(client: SocketIOClient)
    fun getClients(userId: String): Set<SocketIOClient>
    fun getUserId(sessionId: String): String?
    fun getAllUserIds(): Set<String>
}
