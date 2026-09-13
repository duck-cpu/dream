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
