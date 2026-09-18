package dev.yen.dream.world.region

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object DreamRegionService {
    /**
     * Returns the player's existing Dream region, or permanently
     * allocates the next square-spiral region if they do not have one.
     *
     * This keeps region creation in one place instead of making
     * commands or DreamService implement their own allocation logic.
     */
    fun getOrCreateRegion(
        player: ServerPlayer,
        dreamLevel: ServerLevel,
    ): DreamRegion {
        val worldData =
            DreamWorldSavedData.get(
                dreamLevel,
            )

        val existingRegion =
            worldData.getRegion(
                player.uuid,
            )

        if (existingRegion != null) {
            return existingRegion
        }

        val newRegion =
            DreamRegionAllocator.nextAvailableRegion(
                worldData,
            )

        val assigned =
            worldData.assignRegion(
                player.uuid,
                newRegion,
            )

        check(assigned) {
            "Failed to assign Dream region for ${player.uuid}"
        }

        return newRegion
    }

    /**
     * Returns the player's permanent Dream spawn.
     *
     * If the player has never received a spawn before, a safe position
     * is found near the center of their region and written to SavedData.
     */
    fun getOrCreateSpawn(
        player: ServerPlayer,
        dreamLevel: ServerLevel,
    ): BlockPos? {
        val worldData =
            DreamWorldSavedData.get(
                dreamLevel,
            )

        val region =
            getOrCreateRegion(
                player,
                dreamLevel,
            )

        /*
         * Once assigned, the player's spawn is permanent.
         *
         * We do not recalculate it every time they enter the Dream.
         */
        region.spawnPos?.let { spawnPos ->
            return spawnPos
        }
        val spawnPos =
            DreamSpawnResolver.findSafeSpawn(
                dreamLevel,
                region,
            )

        val saved =
            worldData.setSpawn(
                player.uuid,
                spawnPos,
            )

        if (!saved) {
            return null
        }

        return spawnPos
    }
}
