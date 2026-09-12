package dev.yen.dream.event

import dev.yen.dream.session.DreamSession
import dev.yen.dream.session.DreamSessionManager
import dev.yen.dream.world.DreamDimensions
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamSleepHandler {
    @SubscribeEvent
    fun onPlayerSleep(event: PlayerSleepInBedEvent) {
        val player = event.entity as? ServerPlayer ?: return

        val dreamLevel = player.server.getLevel(DreamDimensions.DREAM) ?: return

        val bedPos = event.pos

        val dreamDurationTicks = 20 * 10

        val session =
            DreamSession(
                originDimension = player.level().dimension(),
                originPos = bedPos,
                endTick = player.server.tickCount + dreamDurationTicks,
            )

        DreamSessionManager.start(player.uuid, session)

        player.teleportTo(
            dreamLevel,
            0.5,
            80.0,
            0.5,
            player.yRot,
            player.xRot,
        )

        player.sendSystemMessage(
            Component.literal("You drift into a dream..."),
        )
    }
}
