package dev.yen.dream.event

import dev.yen.dream.world.region.DreamRegionAccessService
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamRegionInteractionHandler {
    /**
     * Prevent a player from beginning to mine a block outside
     * their currently allowed Dream region.
     *
     * BreakEvent below is the authoritative protection, but handling
     * LeftClickBlock as well prevents the client from behaving as though
     * the foreign block can be mined in the first place.
     */
    @SubscribeEvent
    fun onLeftClickBlock(event: PlayerInteractEvent.LeftClickBlock) {
        val player =
            event.entity as? ServerPlayer
                ?: return

        if (
            !DreamRegionAccessService.canAccessBlock(
                player,
                event.pos,
            )
        ) {
            event.isCanceled = true
        }
    }

    /**
     * Prevent actual block destruction outside the player's
     * currently allowed Dream region.
     *
     * This is the important server-side check. Even if some client
     * behavior or mod bypasses LeftClickBlock, the block itself still
     * cannot be broken.
     */
    @SubscribeEvent
    fun onBreakBlock(event: BlockEvent.BreakEvent) {
        val player =
            event.player as? ServerPlayer
                ?: return

        if (
            !DreamRegionAccessService.canAccessBlock(
                player,
                event.pos,
            )
        ) {
            event.isCanceled = true
        }
    }

    /**
     * Prevent blocks from actually being placed inside another
     * player's Dream region.
     *
     * Checking the placed block's final position is important because
     * the player may click a legal block on their own side of the border
     * while attempting to place the new block across the boundary.
     */
    @SubscribeEvent
    fun onPlaceBlock(event: BlockEvent.EntityPlaceEvent) {
        val player =
            event.entity as? ServerPlayer
                ?: return

        if (
            !DreamRegionAccessService.canAccessBlock(
                player,
                event.pos,
            )
        ) {
            event.isCanceled = true
        }
    }

    /**
     * Prevent direct interaction with blocks outside the player's
     * currently allowed Dream region.
     *
     * This covers things such as:
     *
     * - chests
     * - doors
     * - buttons
     * - levers
     * - crafting blocks
     * - machines
     *
     * More specialized cross-border mechanics such as fluids,
     * explosions, pistons, pipes, and modded networks will be handled
     * separately later.
     */
    @SubscribeEvent
    fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) {
        val player =
            event.entity as? ServerPlayer
                ?: return

        if (
            !DreamRegionAccessService.canAccessBlock(
                player,
                event.pos,
            )
        ) {
            event.isCanceled = true
        }
    }
}
