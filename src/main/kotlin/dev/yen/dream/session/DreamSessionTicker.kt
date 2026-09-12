package dev.yen.dream.session

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
            if (currentTick < session.endTick) {
                continue
            }

            val player =
                server.playerList.getPlayer(playerId)
                    ?: continue

            val originLevel =
                server.getLevel(session.originDimension)
                    ?: continue

            val pos = session.originPos

            player.teleportTo(
                originLevel,
                pos.x + 0.5,
                pos.y.toDouble(),
                pos.z + 0.5,
                player.yRot,
                player.xRot,
            )

            DreamSessionManager.end(playerId)

            player.sendSystemMessage(
                Component.literal("You wake up."),
            )
        }
    }
}
