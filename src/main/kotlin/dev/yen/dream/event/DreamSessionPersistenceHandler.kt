package dev.yen.dream.event

import dev.yen.dream.session.DreamSessionManager
import dev.yen.dream.session.DreamSessionPersistence
import dev.yen.dream.world.DreamDimensions
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamSessionPersistenceHandler {
    @SubscribeEvent
    fun onPlayerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) {
        val player =
            event.entity as? ServerPlayer
                ?: return

        // Clear any stale runtime state left from a previous server/world.
        DreamSessionManager.end(player.uuid)

        if (player.level().dimension() != DreamDimensions.DREAM) {
            DreamSessionPersistence.clear(player)
            return
        }

        val session =
            DreamSessionPersistence.load(player)
                ?: return

        DreamSessionManager.start(
            player.uuid,
            session,
        )
    }
}
