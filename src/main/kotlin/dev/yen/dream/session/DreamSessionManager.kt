package dev.yen.dream.session

import java.util.UUID

object DreamSessionManager {
    private val sessions = mutableMapOf<UUID, DreamSession>()

    fun start(
        playerId: UUID,
        session: DreamSession,
    ) {
        sessions[playerId] = session
    }

    fun get(playerId: UUID): DreamSession? = sessions[playerId]

    fun end(playerId: UUID): DreamSession? = sessions.remove(playerId)

    fun all(): Map<UUID, DreamSession> = sessions.toMap()
}
