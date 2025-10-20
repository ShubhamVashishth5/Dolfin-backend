package com.shubhamvashishth.DolFin.socket

import com.corundumstudio.socketio.SocketIOClient
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.orEmpty

@Component
class InMemorySocketSessionRegistry: SocketSessionRegistry {
    private val userClients = ConcurrentHashMap<String, MutableSet<SocketIOClient>>()
    private val sessionToUser = ConcurrentHashMap<String, String>()

    override fun register(userId: String, client: SocketIOClient) {
        userClients.compute(userId) { _, set ->
            val s = set ?: mutableSetOf()
            s.add(client)
            s
        }
        sessionToUser[client.sessionId.toString()] = userId
    }

    override fun unregister(client: SocketIOClient) {
        val sessionId = client.sessionId.toString()
        val userId = sessionToUser.remove(sessionId) ?: return
        userClients.computeIfPresent(userId) { _, set ->
            set.remove(client)
            if (set.isEmpty()) null else set
        }
    }

    override fun getClients(userId: String): Set<SocketIOClient> = userClients[userId].orEmpty()
    override fun getUserId(sessionId: String): String? = sessionToUser[sessionId]
    override fun getAllUserIds(): Set<String> = userClients.keys
}