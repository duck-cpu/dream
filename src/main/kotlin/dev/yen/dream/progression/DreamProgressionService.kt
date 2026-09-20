package dev.yen.dream.progression

import net.minecraft.server.level.ServerPlayer
import org.slf4j.LoggerFactory

/**
 * The only service permitted to exchange a player's waking and
 * Dream progression states.
 */
object DreamProgressionService {
    private val logger =
        LoggerFactory.getLogger(
            DreamProgressionService::class.java,
        )

    /**
     * Replace the currently active progression with the requested realm.
     *
     * The player's live Minecraft fields contain the active state.
     * The capability contains the inactive state.
     */
    fun swapTo(
        player: ServerPlayer,
        targetRealm: DreamProgressionRealm,
    ): Boolean {
        val progressionData =
            findProgressionData(
                player,
            )
                ?: run {
                    logger.error(
                        "Player {} has no Dream progression capability",
                        player.stringUUID,
                    )

                    return false
                }

        /*
         * Treat repeated requests as successful no-ops. This makes the
         * operation safe if lifecycle recovery requests the state that
         * is already active.
         */
        if (
            progressionData.activeRealm ==
            targetRealm
        ) {
            return true
        }

        val targetState =
            progressionData.copyInactiveState()
                ?: createFirstProgressionState(
                    progressionData,
                    targetRealm,
                )
                ?: run {
                    logger.error(
                        "Player {} has no inactive progression state " +
                            "while switching from {} to {}",
                        player.stringUUID,
                        progressionData.activeRealm.serializedName,
                        targetRealm.serializedName,
                    )

                    return false
                }

        /*
         * Closing the menu returns normal carried/container items before
         * the active state is captured.
         */
        player.closeContainer()
        player.stopUsingItem()

        val currentState =
            DreamProgressionStateCodec.capture(
                player,
            )

        return try {
            /*
             * Install the target state first. Only update the persisted
             * realm marker after application succeeds.
             */
            DreamProgressionStateCodec.apply(
                player,
                targetState,
            )

            progressionData.completeSwap(
                newActiveRealm = targetRealm,
                newlyInactiveState = currentState,
            )

            logger.info(
                "Swapped player {} progression from {} to {}",
                player.stringUUID,
                oppositeOf(
                    targetRealm,
                ).serializedName,
                targetRealm.serializedName,
            )

            true
        } catch (exception: Exception) {
            logger.error(
                "Failed to swap player {} progression to {}",
                player.stringUUID,
                targetRealm.serializedName,
                exception,
            )

            /*
             * Best-effort rollback. The capability marker has not been
             * changed yet, so restoring currentState returns the player
             * to the previously authoritative state.
             */
            try {
                DreamProgressionStateCodec.apply(
                    player,
                    currentState,
                )
            } catch (rollbackException: Exception) {
                logger.error(
                    "Failed to roll back player {} progression",
                    player.stringUUID,
                    rollbackException,
                )
            }

            false
        }
    }

    private fun createFirstProgressionState(
        progressionData: DreamProgressionData,
        targetRealm: DreamProgressionRealm,
    ): DreamProgressionState? {
        /*
         * A missing inactive state is valid only before the player's
         * first Dream entry.
         */
        if (
            progressionData.activeRealm !=
            DreamProgressionRealm.WAKING
        ) {
            return null
        }

        if (
            targetRealm !=
            DreamProgressionRealm.DREAM
        ) {
            return null
        }

        return DreamProgressionState.fresh()
    }

    private fun findProgressionData(
        player: ServerPlayer,
    ): DreamProgressionData? {
        var progressionData: DreamProgressionData? =
            null

        DreamProgressionCapability
            .get(
                player,
            ).ifPresent { foundData ->
                progressionData =
                    foundData
            }

        return progressionData
    }

    private fun oppositeOf(
        realm: DreamProgressionRealm,
    ): DreamProgressionRealm =
        when (realm) {
            DreamProgressionRealm.WAKING ->
                DreamProgressionRealm.DREAM

            DreamProgressionRealm.DREAM ->
                DreamProgressionRealm.WAKING
        }
}
