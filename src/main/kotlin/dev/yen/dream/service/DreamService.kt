package dev.yen.dream.service

import dev.yen.dream.session.DreamSession
import dev.yen.dream.session.DreamSessionManager
import dev.yen.dream.world.DreamDimensions
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DreamService {
    private const val DREAM_DURATION_TICKS = 20 * 10

    fun enterDream(
        player: ServerPlayer,
        originPos: BlockPos,
    ): Boolean {
        val dreamLevel =
            player.server.getLevel(DreamDimensions.DREAM)
                ?: return false

        val session =
            DreamSession(
                originDimension = player.level().dimension(),
                originPos = originPos,
                endTick = player.server.tickCount + DREAM_DURATION_TICKS,
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

        return true
    }

    fun wakeFromDream(player: ServerPlayer): Boolean {
        val session =
            DreamSessionManager.get(player.uuid)
                ?: return false

        val originLevel =
            player.server.getLevel(session.originDimension)
                ?: return false

        val pos = session.originPos

        player.teleportTo(
            originLevel,
            pos.x + 0.5,
            pos.y.toDouble(),
            pos.z + 0.5,
            player.yRot,
            player.xRot,
        )

        DreamSessionManager.end(player.uuid)

        player.sendSystemMessage(
            Component.literal("You wake up."),
        )

        return true
    }
}
