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

        val server = event.server
        val currentTick = server.tickCount

        for ((playerId, session) in DreamSessionManager.all()) {
            if (currentTick < session.endTick) {
                continue
            }

            val player =
                server.playerList.getPlayer(playerId)
                    ?: continue

            DreamService.wakeFromDream(player)
        }
    }
}
