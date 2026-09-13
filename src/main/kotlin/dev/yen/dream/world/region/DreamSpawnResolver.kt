package dev.yen.dream.world.region

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap

object DreamSpawnResolver {
    /**
     * Finds a safe spawn position near the center of a player's region.
     *
     * We start at the exact region center and search outward in square
     * rings until a usable surface position is found.
     *
     * This only runs when a player receives their Dream spawn for the
     * first time. The resulting BlockPos is then persisted permanently.
     */
    fun findSafeSpawn(
        dreamLevel: ServerLevel,
        region: DreamRegion,
    ): BlockPos? {
        val centerX =
            region.centerBlockX

        val centerZ =
            region.centerBlockZ

        /*
         * Search outward from the center.
         *
         * 25 chunks = 400 blocks, so a radius of 200 is enough to
         * inspect the entire region from its center. containsBlock()
         * prevents us from accidentally searching a neighboring region.
         */
        val maximumRadius =
            DreamRegion.REGION_SIZE_BLOCKS / 2

        for (radius in 0..maximumRadius) {
            /*
             * Radius zero is just the center coordinate.
             */
            if (radius == 0) {
                findSafePosition(
                    dreamLevel,
                    region,
                    centerX,
                    centerZ,
                )?.let { spawnPos ->
                    return spawnPos
                }

                continue
            }

            /*
             * NORTH AND SOUTH EDGES
             *
             * Scan both horizontal edges of the current square ring.
             */
            for (x in centerX - radius..centerX + radius) {
                findSafePosition(
                    dreamLevel,
                    region,
                    x,
                    centerZ - radius,
                )?.let { spawnPos ->
                    return spawnPos
                }

                findSafePosition(
                    dreamLevel,
                    region,
                    x,
                    centerZ + radius,
                )?.let { spawnPos ->
                    return spawnPos
                }
            }

            /*
             * WEST AND EAST EDGES
             *
             * The corners were already checked above, so skip them here.
             */
            for (z in centerZ - radius + 1 until centerZ + radius) {
                findSafePosition(
                    dreamLevel,
                    region,
                    centerX - radius,
                    z,
                )?.let { spawnPos ->
                    return spawnPos
                }

                findSafePosition(
                    dreamLevel,
                    region,
                    centerX + radius,
                    z,
                )?.let { spawnPos ->
                    return spawnPos
                }
            }
        }

        /*
         * This should be extremely unusual with normal Overworld terrain,
         * but returning null lets the caller fail safely rather than
         * teleporting the player somewhere invalid.
         */
        return null
    }

    /**
     * Tests one X/Z column for a safe standing position.
     */
    private fun findSafePosition(
        dreamLevel: ServerLevel,
        region: DreamRegion,
        x: Int,
        z: Int,
    ): BlockPos? {
        /*
         * Never inspect terrain outside this player's region.
         */
        if (!region.containsBlock(x, z)) {
            return null
        }

        /*
         * A player's personal region may never have been visited before.
         *
         * Explicitly load/generate the chunk containing this candidate
         * position before querying its heightmap and block states.
         */
        dreamLevel.getChunk(
            x shr 4,
            z shr 4,
        )

        /*
         * MOTION_BLOCKING_NO_LEAVES gives us the first standing position
         * above normal terrain while avoiding tree leaves as the surface.
         */
        val y =
            dreamLevel.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z,
            )

        if (
            y <= dreamLevel.minBuildHeight ||
            y >= dreamLevel.maxBuildHeight - 1
        ) {
            return null
        }

        val feetPos =
            BlockPos(
                x,
                y,
                z,
            )

        val headPos =
            feetPos.above()

        val groundPos =
            feetPos.below()

        val feetState =
            dreamLevel.getBlockState(
                feetPos,
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
         * The player's feet and head must have no collision geometry
         * and must not contain a fluid.
         *
         * Using collision shapes instead of isAir also allows harmless
         * vegetation such as grass.
         */
        val feetClear =
            feetState
                .getCollisionShape(
                    dreamLevel,
                    feetPos,
                ).isEmpty &&
                dreamLevel
                    .getFluidState(
                        feetPos,
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
         * There must be a solid upward-facing surface underneath the
         * player, and that surface must not itself be submerged.
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

        return if (
            feetClear &&
            headClear &&
            safeGround
        ) {
            feetPos
        } else {
            null
        }
    }
}
