package dev.yen.dream.event

import dev.yen.dream.service.DreamService
import dev.yen.dream.world.DreamDimensions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import java.util.UUID

object DreamSleepHandler {
    // Temporary activation gate.
    // Sprint 3 will replace this with Dream Anchor logic.
    private const val DREAM_ACTIVATION_ENABLED = false

    private val pendingDreams =
        mutableMapOf<UUID, BlockPos>()

    @SubscribeEvent
    fun onPlayerSleep(event: PlayerSleepInBedEvent) {
        val player =
            event.entity as? ServerPlayer
                ?: return

        // If Dream activation is disabled, completely ignore sleeping
        // and allow vanilla Minecraft to handle it normally.
        if (!DREAM_ACTIVATION_ENABLED) {
            return
        }

        // Beds inside the Dream keep their normal explosion behavior.
        if (player.level().dimension() == DreamDimensions.DREAM) {
            return
        }

        pendingDreams[player.uuid] = event.pos
    }

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        if (event.phase != TickEvent.Phase.END) {
            return
        }

        val player =
            event.player as? ServerPlayer
                ?: return

        val bedPos =
            pendingDreams.remove(player.uuid)
                ?: return

        // Vanilla rejected the sleep attempt.
        // This covers daytime, nearby monsters, etc.
        if (!player.isSleeping) {
            return
        }

        // Vanilla accepted sleep, but Dreaming replaces actually
        // remaining asleep.
        player.stopSleepInBed(
            true,
            true,
        )

        DreamService.enterDream(
            player,
            bedPos,
        )
    }
}
