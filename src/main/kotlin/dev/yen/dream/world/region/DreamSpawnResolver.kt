package dev.yen.dream.world.region

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.shapes.CollisionContext

object DreamSpawnResolver {
    /*
     * Search only a small area around the center of the player's region
     * for naturally safe terrain.
     *
     * This is NOT used to decide whether the player keeps the region.
     * Region ownership has already been permanently determined by the
     * square-spiral allocator before this resolver runs.
     *
     * The search merely prevents us from constructing the emergency
     * platform when perfectly usable natural terrain exists nearby.
     */
    private const val NATURAL_SPAWN_SEARCH_RADIUS = 32

    /**
     * Resolve the player's permanent initial Dream spawn.
     *
     * Preferred behavior:
     *
     *     region center
     *         ↓
     *     nearby natural safe position
     *         ↓
     *     persist that position
     *
     * If no natural position exists within the bounded search radius:
     *
     *     region center
     *         ↓
     *     emergency 3 x 3 platform
     *         ↓
     *     persist that position
     *
     * Terrain never causes the player's assigned region to be rerolled
     * or replaced.
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
         * Keep track of chunks we have explicitly generated/loaded
         * during this resolution attempt.
         *
         * Multiple candidate blocks can belong to the same chunk, so
         * there is no reason to repeatedly force-load that chunk.
         */
        val loadedChunks =
            mutableSetOf<Long>()

        /*
         * Search outward from the exact center in square rings.
         *
         * This makes positions near the center strongly preferred while
         * keeping the search bounded and predictable.
         */
        for (
            radius in
                0..NATURAL_SPAWN_SEARCH_RADIUS
        ) {
            /*
             * Radius zero is the exact center column.
             */
            if (radius == 0) {
                val naturalSpawn =
                    findNaturalSpawnAt(
                        dreamLevel = dreamLevel,
                        region = region,
                        x = centerX,
                        z = centerZ,
                        loadedChunks = loadedChunks,
                    )

                if (naturalSpawn != null) {
                    return naturalSpawn
                }

                continue
            }

            /*
             * Search the north and south edges of this ring.
             */
            for (offsetX in -radius..radius) {
                val northSpawn =
                    findNaturalSpawnAt(
                        dreamLevel = dreamLevel,
                        region = region,
                        x = centerX + offsetX,
                        z = centerZ - radius,
                        loadedChunks = loadedChunks,
                    )

                if (northSpawn != null) {
                    return northSpawn
                }

                val southSpawn =
                    findNaturalSpawnAt(
                        dreamLevel = dreamLevel,
                        region = region,
                        x = centerX + offsetX,
                        z = centerZ + radius,
                        loadedChunks = loadedChunks,
                    )

                if (southSpawn != null) {
                    return southSpawn
                }
            }

            /*
             * Search the west and east edges of this ring.
             *
             * The corners were already checked by the north/south
             * loops, so exclude them here.
             */
            for (
                offsetZ in
                    -(radius - 1)..(radius - 1)
            ) {
                val westSpawn =
                    findNaturalSpawnAt(
                        dreamLevel = dreamLevel,
                        region = region,
                        x = centerX - radius,
                        z = centerZ + offsetZ,
                        loadedChunks = loadedChunks,
                    )

                if (westSpawn != null) {
                    return westSpawn
                }

                val eastSpawn =
                    findNaturalSpawnAt(
                        dreamLevel = dreamLevel,
                        region = region,
                        x = centerX + radius,
                        z = centerZ + offsetZ,
                        loadedChunks = loadedChunks,
                    )

                if (eastSpawn != null) {
                    return eastSpawn
                }
            }
        }

        /*
         * No naturally safe location exists reasonably close to the
         * region center.
         *
         * This is the only situation in which we alter terrain.
         */
        return createEmergencySpawn(
            dreamLevel = dreamLevel,
            centerX = centerX,
            centerZ = centerZ,
            loadedChunks = loadedChunks,
        )
    }

    /**
     * Attempt to resolve one natural spawn position at the supplied
     * X/Z column.
     *
     * Returns null if the position is unsuitable.
     */
    private fun findNaturalSpawnAt(
        dreamLevel: ServerLevel,
        region: DreamRegion,
        x: Int,
        z: Int,
        loadedChunks: MutableSet<Long>,
    ): BlockPos? {
        /*
         * Spawn resolution must never escape the player's assigned
         * Dream region.
         */
        if (!region.containsBlock(x, z)) {
            return null
        }

        /*
         * Heightmap data cannot be trusted for an unloaded Dream chunk.
         *
         * Explicitly generate/load the candidate chunk before asking
         * Minecraft for its surface height.
         *
         * This prevents the -63 / minimum-build-height spawn bug.
         */
        ensureChunkLoaded(
            dreamLevel = dreamLevel,
            x = x,
            z = z,
            loadedChunks = loadedChunks,
        )

        /*
         * MOTION_BLOCKING_NO_LEAVES is preferable to WORLD_SURFACE for
         * natural player spawning.
         *
         * It ignores leaves and avoids treating tree canopies as the
         * desired terrain surface.
         *
         * Fluids still prevent the resulting position from passing our
         * safety check below.
         */
        val spawnY =
            dreamLevel.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                x,
                z,
            )

        /*
         * Defensive sanity check.
         *
         * A legitimate generated surface should not be at or outside
         * the dimension's build limits.
         */
        if (
            spawnY <= dreamLevel.minBuildHeight ||
            spawnY >= dreamLevel.maxBuildHeight - 1
        ) {
            return null
        }

        val spawnPos =
            BlockPos(
                x,
                spawnY,
                z,
            )

        return if (
            isSafeStandingPosition(
                dreamLevel,
                spawnPos,
            )
        ) {
            spawnPos
        } else {
            null
        }
    }

    /**
     * Determine whether a player can safely stand at spawnPos without
     * changing the generated terrain.
     */
    private fun isSafeStandingPosition(
        dreamLevel: ServerLevel,
        spawnPos: BlockPos,
    ): Boolean {
        val groundPos =
            spawnPos.below()

        val headPos =
            spawnPos.above()

        val groundState =
            dreamLevel.getBlockState(
                groundPos,
            )

        val feetState =
            dreamLevel.getBlockState(
                spawnPos,
            )

        val headState =
            dreamLevel.getBlockState(
                headPos,
            )

        /*
         * The player's feet position must contain no collision and no
         * fluid.
         *
         * Grass, flowers, etc. are acceptable because they have no
         * meaningful collision volume.
         */
        val feetClear =
            feetState
                .getCollisionShape(
                    dreamLevel,
                    spawnPos,
                    CollisionContext.empty(),
                ).isEmpty &&
                dreamLevel
                    .getFluidState(
                        spawnPos,
                    ).isEmpty

        /*
         * The player's head position must also be unobstructed.
         */
        val headClear =
            headState
                .getCollisionShape(
                    dreamLevel,
                    headPos,
                    CollisionContext.empty(),
                ).isEmpty &&
                dreamLevel
                    .getFluidState(
                        headPos,
                    ).isEmpty

        /*
         * The block beneath the player must provide a sturdy upper
         * surface and cannot itself contain fluid.
         *
         * This excludes water, lava, and other obviously invalid
         * standing surfaces.
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
     * Create a guaranteed spawn at the exact center of the player's
     * region when no nearby natural position is usable.
     *
     * This is intentionally a last-resort fallback.
     */
    private fun createEmergencySpawn(
        dreamLevel: ServerLevel,
        centerX: Int,
        centerZ: Int,
        loadedChunks: MutableSet<Long>,
    ): BlockPos {
        /*
         * Explicitly ensure that the center chunk exists before using
         * its heightmap.
         *
         * Do this even though the natural search normally touched the
         * center already. The emergency function should remain correct
         * independently.
         */
        ensureChunkLoaded(
            dreamLevel = dreamLevel,
            x = centerX,
            z = centerZ,
            loadedChunks = loadedChunks,
        )

        /*
         * WORLD_SURFACE is appropriate for the fallback because we
         * want the visible surface height, including an ocean surface.
         *
         * For an ocean Dream this places the platform at approximately
         * sea level rather than on the ocean floor.
         */
        val surfaceY =
            dreamLevel.getHeight(
                Heightmap.Types.WORLD_SURFACE,
                centerX,
                centerZ,
            )

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

        val floorY =
            spawnY - 1

        /*
         * Create the smallest practical guaranteed landing area.
         *
         * This platform is temporary design-wise. A future Dream-region
         * generation system may replace this fallback entirely.
         */
        for (offsetX in -1..1) {
            for (offsetZ in -1..1) {
                val floorPos =
                    BlockPos(
                        centerX + offsetX,
                        floorY,
                        centerZ + offsetZ,
                    )

                dreamLevel.setBlockAndUpdate(
                    floorPos,
                    Blocks.STONE.defaultBlockState(),
                )
            }
        }

        /*
         * Guarantee two blocks of clear player space directly above
         * the center of the platform.
         */
        dreamLevel.setBlockAndUpdate(
            spawnPos,
            Blocks.AIR.defaultBlockState(),
        )

        dreamLevel.setBlockAndUpdate(
            spawnPos.above(),
            Blocks.AIR.defaultBlockState(),
        )

        return spawnPos
    }

    /**
     * Explicitly generate/load the chunk containing a candidate X/Z
     * coordinate.
     *
     * ChunkPos.asLong() gives us a compact key so the same chunk is not
     * force-loaded repeatedly during one spawn-resolution pass.
     */
    private fun ensureChunkLoaded(
        dreamLevel: ServerLevel,
        x: Int,
        z: Int,
        loadedChunks: MutableSet<Long>,
    ) {
        val chunkX =
            x shr 4

        val chunkZ =
            z shr 4

        val chunkKey =
            ChunkPos.asLong(
                chunkX,
                chunkZ,
            )

        if (
            loadedChunks.add(
                chunkKey,
            )
        ) {
            dreamLevel.getChunk(
                chunkX,
                chunkZ,
            )
        }
    }
}

