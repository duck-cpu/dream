package dev.yen.dream.world.region

import net.minecraft.core.BlockPos

data class DreamRegion(
    val gridX: Int,
    val gridZ: Int,
    val spawnPos: BlockPos? = null,
) {
    val minChunkX: Int
        get() = gridX * REGION_SIZE_CHUNKS

    val maxChunkX: Int
        get() = minChunkX + REGION_SIZE_CHUNKS - 1

    val minChunkZ: Int
        get() = gridZ * REGION_SIZE_CHUNKS

    val maxChunkZ: Int
        get() = minChunkZ + REGION_SIZE_CHUNKS - 1

    val minBlockX: Int
        get() = minChunkX * CHUNK_SIZE_BLOCKS

    val maxBlockX: Int
        get() = minBlockX + REGION_SIZE_BLOCKS - 1

    val minBlockZ: Int
        get() = minChunkZ * CHUNK_SIZE_BLOCKS

    val maxBlockZ: Int
        get() = minBlockZ + REGION_SIZE_BLOCKS - 1

    val centerBlockX: Int
        get() = minBlockX + REGION_SIZE_BLOCKS / 2

    val centerBlockZ: Int
        get() = minBlockZ + REGION_SIZE_BLOCKS / 2

    /*
     * Block coordinates are stored as inclusive integer ranges:
     *
     * Region (0, 0):
     *     blocks X 0..399
     *
     * But movement happens in continuous world coordinates, so the
     * corresponding geometric region is:
     *
     *     0.0 <= X < 400.0
     *
     * maxXExclusive/maxZExclusive represent those outer boundary planes.
     * These are also the exact planes the client renderer will eventually use.
     */

    val minX: Double
        get() = minBlockX.toDouble()

    val maxXExclusive: Double
        get() = (maxBlockX + 1).toDouble()

    val minZ: Double
        get() = minBlockZ.toDouble()

    val maxZExclusive: Double
        get() = (maxBlockZ + 1).toDouble()

    /*
     * Checks a precise horizontal world position rather than an integer
     * BlockPos. This is useful for players, entities, projectiles, etc.
     */

    fun containsHorizontal(
        x: Double,
        z: Double,
    ): Boolean =
        x >= minX &&
            x < maxXExclusive &&
            z >= minZ &&
            z < maxZExclusive

    fun containsBlock(
        x: Int,
        z: Int,
    ): Boolean =
        x in minBlockX..maxBlockX &&
            z in minBlockZ..maxBlockZ

    fun containsBlock(pos: BlockPos): Boolean =
        containsBlock(
            pos.x,
            pos.z,
        )

    companion object {
        const val REGION_SIZE_CHUNKS = 25
        const val CHUNK_SIZE_BLOCKS = 16
        const val REGION_SIZE_BLOCKS =
            REGION_SIZE_CHUNKS * CHUNK_SIZE_BLOCKS
    }
}
