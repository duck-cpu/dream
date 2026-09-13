package dev.yen.dream.service

import dev.yen.dream.session.DreamSession
import dev.yen.dream.session.DreamSessionManager
import dev.yen.dream.session.DreamSessionPersistence
import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegionService
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DreamService {
    private const val DREAM_DURATION_TICKS = 200 * 100

    fun enterDream(
        player: ServerPlayer,
        originPos: BlockPos,
    ): Boolean {
        /*
         * A player may only have one active Dream session.
         */
        if (DreamSessionManager.has(player.uuid)) {
            player.sendSystemMessage(
                Component.literal(
                    "You are already dreaming.",
                ),
            )

            return false
        }

        val dreamLevel =
            player.server.getLevel(
                DreamDimensions.DREAM,
            )
                ?: return false

        /*
         * Get the player's permanent personal Dream spawn.
         *
         * If the player has never entered their personal region before,
         * this will allocate their region, find a safe spawn inside it,
         * and persist that spawn for future Dream sessions.
         *
         * Resolve this BEFORE starting the Dream session so a failed
         * spawn lookup cannot leave behind a broken active session.
         */
        val spawnPos =
            DreamRegionService.getOrCreateSpawn(
                player,
                dreamLevel,
            )
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Could not find a safe Dream spawn.",
                        ),
                    )

                    return false
                }

        /*
         * Store where the player came from so the Dream can return
         * them to the correct dimension and position when they wake.
         */
        val session =
            DreamSession(
                originDimension =
                    player.level().dimension(),
                originPos = originPos,
                endTick =
                    player.server.tickCount +
                        DREAM_DURATION_TICKS,
            )

        DreamSessionManager.start(
            player.uuid,
            session,
        )

        DreamSessionPersistence.save(
            player,
            session,
        )

        /*
         * Enter the player's own persistent Dream region.
         *
         * X/Z are offset by 0.5 so the player appears in the center
         * of the destination block instead of directly on its edge.
         */
        player.teleportTo(
            dreamLevel,
            spawnPos.x + 0.5,
            spawnPos.y.toDouble(),
            spawnPos.z + 0.5,
            player.yRot,
            player.xRot,
        )

        player.sendSystemMessage(
            Component.literal(
                "You drift into a dream...",
            ),
        )

        return true
    }

    fun wakeFromDream(player: ServerPlayer): Boolean {
        val session =
            DreamSessionManager.get(
                player.uuid,
            )
                ?: return false

        val originLevel =
            player.server.getLevel(
                session.originDimension,
            )
                ?: return false

        val pos =
            session.originPos

        /*
         * Return the player to the position from which
         * their Dream session originally began.
         */
        player.teleportTo(
            originLevel,
            pos.x + 0.5,
            pos.y.toDouble(),
            pos.z + 0.5,
            player.yRot,
            player.xRot,
        )

        DreamSessionManager.end(
            player.uuid,
        )

        DreamSessionPersistence.clear(
            player,
        )

        player.sendSystemMessage(
            Component.literal(
                "You wake up.",
            ),
        )

        return true
    }
}
