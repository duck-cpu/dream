package dev.yen.dream.session

import java.util.UUID

object DreamSessionManager {

    private val sessions = mutableMapOf<UUID, DreamSession>()

    fun start(playerId: UUID, session: DreamSession) {
        sessions[playerId] = session
    }

    fun get(playerId: UUID): DreamSession? {
        return sessions[playerId]
    }

    fun end(playerId: UUID): DreamSession? {
        return sessions.remove(playerId)
    }

    fun all(): Map<UUID, DreamSession> {
        return sessions.toMap()
    }
}
