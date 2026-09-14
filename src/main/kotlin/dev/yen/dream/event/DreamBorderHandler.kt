package dev.yen.dream.event

import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegionAccessService
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamBorderHandler {
    /*
     * Tiny inset from the mathematical border plane.
     *
     * This avoids floating-point jitter where the player's bounding box
     * repeatedly touches and crosses the exact same coordinate.
     */
    private const val BORDER_EPSILON = 0.001

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        /*
         * Only perform border enforcement once per server tick.
         */
        if (event.phase != TickEvent.Phase.END) {
            return
        }

        val player =
            event.player as? ServerPlayer
                ?: return

        /*
         * Dream borders have no meaning outside dream:dream.
         */
        if (
            player.level().dimension() !=
            DreamDimensions.DREAM
        ) {
            return
        }

        val region =
            DreamRegionAccessService.getAllowedRegion(
                player,
            )
                ?: return

        /*
         * Minecraft positions players by their center point.
         *
         * We need to account for half the player's width so their actual
         * bounding box cannot extend through the Dream border even though
         * their center technically remains inside the region.
         */
        val halfWidth =
            player.bbWidth.toDouble() / 2.0

        val minimumX =
            region.minX +
                halfWidth +
                BORDER_EPSILON

        val maximumX =
            region.maxXExclusive -
                halfWidth -
                BORDER_EPSILON

        val minimumZ =
            region.minZ +
                halfWidth +
                BORDER_EPSILON

        val maximumZ =
            region.maxZExclusive -
                halfWidth -
                BORDER_EPSILON

        /*
         * Clamp the player's center to the legal area.
         *
         * If they're already inside the region, these values are identical
         * to their current position and nothing else happens.
         */
        val clampedX =
            Mth.clamp(
                player.x,
                minimumX,
                maximumX,
            )

        val clampedZ =
            Mth.clamp(
                player.z,
                minimumZ,
                maximumZ,
            )

        val crossedX =
            clampedX != player.x

        val crossedZ =
            clampedZ != player.z

        if (!crossedX && !crossedZ) {
            return
        }

        /*
         * Move the player back only as far as necessary.
         *
         * We deliberately do NOT send them back to their personal spawn.
         * The Dream border should behave like an invisible wall rather
         * than punishing the player with a large teleport.
         */
        player.teleportTo(
            clampedX,
            player.y,
            clampedZ,
        )

        /*
         * Cancel only the velocity component trying to push through the
         * border. Motion parallel to the wall and vertical motion remain.
         *
         * This lets players slide naturally along the boundary.
         */
        val velocity =
            player.deltaMovement

        player.setDeltaMovement(
            Vec3(
                if (crossedX) 0.0 else velocity.x,
                velocity.y,
                if (crossedZ) 0.0 else velocity.z,
            ),
        )
    }
}
