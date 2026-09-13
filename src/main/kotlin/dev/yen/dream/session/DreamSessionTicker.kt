package dev.yen.dream.session

import dev.yen.dream.service.DreamService
import net.minecraft.network.chat.Component
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
            val player =
                server.playerList.getPlayer(playerId)
                    ?: continue

            val remainingTicks =
                (session.endTick - currentTick)
                    .coerceAtLeast(0)

            // Keep persistent session data current.
            DreamSessionPersistence.save(
                player,
                session,
            )

            // DEBUG: print remaining Dream time once per second.
            if (currentTick % 20 == 0) {
                player.sendSystemMessage(
                    Component.literal(
                        "[Dream Debug] ${remainingTicks / 20.0}s remaining " +
                            "($remainingTicks ticks)",
                    ),
                )
            }

            if (currentTick < session.endTick) {
                continue
            }

            DreamService.wakeFromDream(player)
        }
    }
}
