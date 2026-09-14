package dev.yen.dream.world.region

import dev.yen.dream.world.DreamDimensions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer

object DreamRegionAccessService {
    /**
     * Returns the Dream region the player is currently allowed to occupy.
     *
     * Right now a player may access only their own permanent region.
     *
     * Keeping this behind an access service is intentional. Later this
     * can support gates, neighboring-region permission, shared spaces,
     * dungeon access, etc. without rewriting border enforcement.
     */
    fun getAllowedRegion(player: ServerPlayer): DreamRegion? {
        val level =
            player.serverLevel()

        if (level.dimension() != DreamDimensions.DREAM) {
            return null
        }

        val worldData =
            DreamWorldSavedData.get(
                level,
            )

        return worldData.getRegion(
            player.uuid,
        )
    }

    /**
     * Determines whether the player may directly interact with a block.
     *
     * Outside the Dream dimension this service imposes no restrictions.
     * Inside the Dream, the BlockPos must belong to the player's currently
     * allowed region.
     */
    fun canAccessBlock(
        player: ServerPlayer,
        pos: BlockPos,
    ): Boolean {
    /*
     * Region ownership restrictions apply only inside dream:dream.
     * Returning true here ensures this system never interferes with
     * normal Overworld/Nether/End gameplay.
     */
        if (
            player.serverLevel().dimension() !=
            DreamDimensions.DREAM
        ) {
            return true
        }

        val region =
            getAllowedRegion(player)
                ?: return false

        return region.containsBlock(
            pos,
        )
    }

    fun canOccupy(
        player: ServerPlayer,
        x: Double,
        z: Double,
    ): Boolean {
        val region =
            getAllowedRegion(player)
                ?: return false

        return region.containsHorizontal(
            x,
            z,
        )
    }
}
