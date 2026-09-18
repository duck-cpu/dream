package dev.yen.dream.world.region

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap

object DreamSpawnResolver {
    /**
     * Resolves a permanent spawn at the exact horizontal center
     * of the player's assigned Dream region.
     *
     * Region assignment is intentionally terrain-agnostic.
     *
     * We do NOT search elsewhere for nicer terrain and we do NOT
     * reject or reroll regions because their center happens to be
     * ocean, lava, forest, mountain, etc.
     *
     * If the natural center position is unsafe, we create a small
     * artificial arrival platform there instead.
     */
    fun findSafeSpawn(
        dreamLevel: ServerLevel,
        region: DreamRegion,
    ): BlockPos {
        val centerX =
            region.centerBlockX

        val centerZ =
            region.centerBlockZ

        /*
         * Only the chunk containing the region center needs to exist.
         *
         * The old resolver could synchronously inspect/generate much
         * of the entire 25 x 25 chunk region while looking for land.
         * That is unnecessary because terrain quality must never
         * influence region ownership.
         */
        dreamLevel.getChunk(
            centerX shr 4,
            centerZ shr 4,
        )

        /*
         * WORLD_SURFACE gives us the first open position above the
         * highest terrain or fluid at this exact X/Z coordinate.
         *
         * Examples:
         *
         * grass surface -> one block above grass
         * ocean surface -> one block above water
         * lava surface  -> one block above lava
         */
        val surfaceY =
            dreamLevel.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                centerX,
                centerZ,
            )

        /*
         * Leave enough vertical room for:
         *
         * floor
         * feet
         * head
         *
         * The clamps are mostly defensive because normal Overworld
         * generation should already keep us well inside these limits.
         */
        val spawnY =
            surfaceY.coerceIn(
                dreamLevel.minBuildHeight + 1,
                dreamLevel.maxBuildHeight - 2,
            )

        val spawnPos =
            BlockPos(
                centerX,
                spawnY,
                centerZ,
            )

        /*
         * If the exact center already has normal safe terrain,
         * preserve it completely and simply use that position.
         */
        if (
            isSafeStandingPosition(
                dreamLevel,
                spawnPos,
            )
        ) {
            return spawnPos
        }

        /*
         * The exact center is not naturally safe.
         *
         * This can happen when the assigned region center contains:
         *
         * - ocean
         * - river
         * - lava
         * - leaves
         * - awkward generated terrain
         *
         * The region is still valid. We make the spawn valid instead
         * of looking somewhere else.
         */
        createArrivalPlatform(
            dreamLevel,
            spawnPos,
        )

        return spawnPos
    }

    /**
     * Checks whether the player can safely stand at the supplied
     * position without modifying the terrain.
     */
    private fun isSafeStandingPosition(
        dreamLevel: ServerLevel,
        spawnPos: BlockPos,
    ): Boolean {
        val headPos =
            spawnPos.above()

        val groundPos =
            spawnPos.below()

        val feetState =
            dreamLevel.getBlockState(
                spawnPos,
            )

        val headState =
            dreamLevel.getBlockState(
                headPos,
            )

        val groundState =
            dreamLevel.getBlockState(
                groundPos,
            )

        /*
         * Feet and head require empty collision space and cannot
         * contain water, lava, or another fluid.
         */
        val feetClear =
            feetState
                .getCollisionShape(
                    dreamLevel,
                    spawnPos,
                ).isEmpty &&
                dreamLevel
                    .getFluidState(
                        spawnPos,
                    ).isEmpty

        val headClear =
            headState
                .getCollisionShape(
                    dreamLevel,
                    headPos,
                ).isEmpty &&
                dreamLevel
                    .getFluidState(
                        headPos,
                    ).isEmpty

        /*
         * Ground must actually support the player and must not
         * itself be fluid.
         */
        val safeGround =
            groundState.isFaceSturdy(
                dreamLevel,
                groundPos,
                Direction.UP,
            ) &&
                dreamLevel
                    .getFluidState(
                        groundPos,
                    ).isEmpty

        return feetClear &&
            headClear &&
            safeGround
    }

    /**
     * Creates a minimal 3 x 3 safety platform centered on the
     * permanent Dream spawn.
     *
     * This is a fallback only. Normal terrain remains untouched when
     * the center already provides a valid standing position.
     *
     * A small platform guarantees that even an ocean or lava region
     * remains a usable Dream region without changing its assignment.
     */
    private fun createArrivalPlatform(
        dreamLevel: ServerLevel,
        spawnPos: BlockPos,
    ) {
        val floorY =
            spawnPos.y - 1

        /*
         * Build a 3 x 3 stone floor beneath the player.
         *
         * This is intentionally small: enough to guarantee a safe
         * arrival without substantially replacing the generated terrain.
         */
        for (offsetX in -1..1) {
            for (offsetZ in -1..1) {
                val floorPos =
                    BlockPos(
                        spawnPos.x + offsetX,
                        floorY,
                        spawnPos.z + offsetZ,
                    )

                dreamLevel.setBlockAndUpdate(
                    floorPos,
                    Blocks.STONE.defaultBlockState(),
                )
            }
        }

        /*
         * Guarantee two blocks of clear space at the actual spawn
         * coordinate even if unusual terrain generation occupies it.
         */
        dreamLevel.setBlockAndUpdate(
            spawnPos,
            Blocks.AIR.defaultBlockState(),
        )

        dreamLevel.setBlockAndUpdate(
            spawnPos.above(),
            Blocks.AIR.defaultBlockState(),
        )
    }
}
