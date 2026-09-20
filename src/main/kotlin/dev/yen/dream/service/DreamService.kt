package dev.yen.dream.service

import dev.yen.dream.progression.DreamProgressionRealm
import dev.yen.dream.progression.DreamProgressionService
import dev.yen.dream.session.DreamSession
import dev.yen.dream.session.DreamSessionManager
import dev.yen.dream.session.DreamSessionPersistence
import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegionService
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object DreamService {
    private const val DREAM_DURATION_TICKS = 13_000

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
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "The Dream dimension is unavailable.",
                        ),
                    )

                    return false
                }

        /*
         * Resolve the permanent personal Dream spawn before changing
         * progression or starting the session.
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
         * Prepare the session before changing the player's active state.
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

        /*
         * Store waking progression and activate Dream progression.
         *
         * Do this only after the dimension and spawn have been resolved,
         * so an ordinary setup failure cannot remove the player's
         * waking inventory.
         */
        if (
            !DreamProgressionService.swapTo(
                player,
                DreamProgressionRealm.DREAM,
            )
        ) {
            player.sendSystemMessage(
                Component.literal(
                    "Could not prepare your Dream progression.",
                ),
            )

            return false
        }

        DreamSessionManager.start(
            player.uuid,
            session,
        )

        DreamSessionPersistence.save(
            player,
            session,
        )

        /*
         * Enter the player's persistent Dream region.
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
                ?: run {
                    player.sendSystemMessage(
                        Component.literal(
                            "Your waking dimension is unavailable.",
                        ),
                    )

                    return false
                }

        /*
         * Restore waking progression before returning to the waking
         * dimension. If restoration fails, keep the player inside the
         * Dream and preserve the active session.
         */
        if (
            !DreamProgressionService.swapTo(
                player,
                DreamProgressionRealm.WAKING,
            )
        ) {
            player.sendSystemMessage(
                Component.literal(
                    "Could not restore your waking progression.",
                ),
            )

            return false
        }

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
