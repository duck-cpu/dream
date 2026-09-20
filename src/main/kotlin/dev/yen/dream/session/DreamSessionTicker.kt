package dev.yen.dream.session

import dev.yen.dream.service.DreamService
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamSessionTicker {
    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) {
            return
        }

        val server =
            event.server

        val currentTick =
            server.tickCount

        for ((playerId, session) in DreamSessionManager.all()) {
            val player =
                server.playerList.getPlayer(
                    playerId,
                )
                    ?: continue

            /*
             * Keep the persisted session available for recovery if the
             * player disconnects or the server stops while dreaming.
             */
            DreamSessionPersistence.save(
                player,
                session,
            )

            if (currentTick < session.endTick) {
                continue
            }

            DreamService.wakeFromDream(
                player,
            )
        }
    }
}
