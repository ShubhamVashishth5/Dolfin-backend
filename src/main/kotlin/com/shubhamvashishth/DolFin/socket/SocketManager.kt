package com.shubhamvashishth.DolFin.socket

import com.corundumstudio.socketio.AuthTokenResult
import com.corundumstudio.socketio.AuthorizationListener
import com.corundumstudio.socketio.AuthorizationResult
import jakarta.annotation.PostConstruct

//Write a socketmanager using netty sockitio in kotlin, using socketsessionregistry to manage sessions
// The socket manager should have methods to send messages to a specific user, broadcast messages to all users, and handle incoming messages
// Use dependency injection to inject the SocketSessionRegistry
// Use coroutines to handle incoming messages asynchronously
// Use a logger to log incoming and outgoing messages

import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.DataListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.shubhamvashishth.DolFin.security.JwtTokenProvider
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.cancel

@Service
class SocketManager(
    private val registry: SocketSessionRegistry,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${socket.port:9092}") private val port: Int,
    @Value("\${socket.host:0.0.0.0}") private val host: String
) {
    private val logger = LoggerFactory.getLogger(SocketManager::class.java)
    private lateinit var server: SocketIOServer
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @PostConstruct
    fun start() {
        val config = Configuration()
        config.hostname = host
        config.port = port
       config.authorizationListener = AuthorizationListener { handshakeData ->
            val token = handshakeData.urlParams["token"]?.firstOrNull()
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                logger.warn("Socket handshake rejected due to missing/invalid token (handshake={})", handshakeData)
                AuthorizationResult(false)
            } else {
                AuthorizationResult(true)
            }
        }

        logger.info("Creating SocketIOServer with config port={} host={}", config.port, config.hostname)


        server = SocketIOServer(config)

        server.addConnectListener { client ->
            val token = client.handshakeData.urlParams["token"]?.firstOrNull()
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                logger.warn("Socket connection rejected due to missing/invalid token")
                client.disconnect()
                return@addConnectListener
            }

            // adapt method name if your JwtTokenProvider exposes a different API
            val userId = jwtTokenProvider.getUsernameFromToken(token)
            registry.register(userId, client)
            logger.info("Socket connected for user={} session={}", userId, client.sessionId)
        }

        server.addDisconnectListener { client ->
            registry.unregister(client)
            logger.info("Socket disconnected session={}", client.sessionId)
        }

        server.addEventListener("message", String::class.java, DataListener<String> { client, data, _ ->
            scope.launch {
                logger.info("Incoming socket message from session={} : {}", client.sessionId, data)
                // handle incoming message; example: echo back
                client.sendEvent("message:ack", mapOf("received" to true, "payload" to data))
            }
        })

        server.start()
        registerEvents()
        logger.info("Socket server started on port {}", port)
    }

    data class SocketMessage(val fromUserId: String, val toUserId: String, val content: String)

    fun registerEvents(){

        server.addEventListener<SocketMessage>(SocketEvents.MESSAGE, SocketMessage::class.java, DataListener { client, data, _ ->
            scope.launch {
                logger.info("Received message from user={} to user={}: {}", data.fromUserId, data.toUserId, data.content)
                sendToUser(data.toUserId, SocketEvents.MESSAGE, data)
            }
        })

    }

    fun sendToUser(userId: String, event: String, data: Any) {
        registry.getClients(userId).forEach { client ->
            try {
                client.sendEvent(event, data)
                logger.debug("Sent event={} to user={} session={}", event, userId, client.sessionId)
            } catch (ex: Exception) {
                logger.warn("Failed to send event to session={}: {}", client.sessionId, ex.message)
            }
        }
    }

    fun broadcast(event: String, data: Any) {
        try {
            server.broadcastOperations.sendEvent(event, data)
            logger.debug("Broadcasted event={}", event)
        } catch (ex: Exception) {
            logger.warn("Broadcast failed: {}", ex.message)
        }
    }

    @PreDestroy
    fun stop() {
        server.stop()
        scope.coroutineContext.cancel()
        logger.info("Socket server stopped")
    }
}
