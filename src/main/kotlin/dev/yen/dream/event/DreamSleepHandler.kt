package dev.yen.dream.event

import dev.yen.dream.service.DreamService
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamSleepHandler {
    @SubscribeEvent
    fun onPlayerSleep(event: PlayerSleepInBedEvent) {
        val player = event.entity as? ServerPlayer ?: return

        DreamService.enterDream(
            player,
            event.pos,
        )
    }
}
