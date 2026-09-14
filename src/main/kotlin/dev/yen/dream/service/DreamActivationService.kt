package dev.yen.dream.service

import dev.yen.dream.registry.DreamBlocks
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

object DreamActivationService {
    private const val ANCHOR_RADIUS = 3

    /**
     * Dream sleep is enabled when a Gilded Pot exists within
     * three blocks of the bed in any direction.
     */
    fun isDreamEnabled(
        level: Level,
        bedPos: BlockPos,
    ): Boolean {
        val minPos =
            bedPos.offset(
                -ANCHOR_RADIUS,
                -ANCHOR_RADIUS,
                -ANCHOR_RADIUS,
            )

        val maxPos =
            bedPos.offset(
                ANCHOR_RADIUS,
                ANCHOR_RADIUS,
                ANCHOR_RADIUS,
            )

        for (
            pos in
                BlockPos.betweenClosed(
                    minPos,
                    maxPos,
                )
        ) {
            if (
                level
                    .getBlockState(pos)
                    .`is`(
                        DreamBlocks.GILDED_POT.get(),
                    )
            ) {
                return true
            }
        }

        return false
    }
}
