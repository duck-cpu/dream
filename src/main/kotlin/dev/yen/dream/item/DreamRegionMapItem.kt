package dev.yen.dream.item

import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegionMapFormatter
import dev.yen.dream.world.region.DreamWorldSavedData
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class DreamRegionMapItem(
    properties: Properties,
) : Item(properties) {
    /**
     * Print the local Dream-region allocation map when the
     * player right-clicks while holding this item.
     */
    override fun use(
        level: Level,
        player: Player,
        usedHand: InteractionHand,
    ): InteractionResultHolder<ItemStack> {
        val itemStack =
            player.getItemInHand(
                usedHand,
            )

        /*
         * The client reports a successful interaction, but the server
         * performs all SavedData access and sends the actual messages.
         */
        if (level.isClientSide) {
            return InteractionResultHolder.success(
                itemStack,
            )
        }

        val serverPlayer =
            player as? ServerPlayer
                ?: return InteractionResultHolder.pass(
                    itemStack,
                )

        val dreamLevel =
            serverPlayer.server.getLevel(
                DreamDimensions.DREAM,
            )
                ?: run {
                    serverPlayer.sendSystemMessage(
                        Component.literal(
                            "Dream dimension is unavailable.",
                        ),
                    )

                    return InteractionResultHolder.fail(
                        itemStack,
                    )
                }

        val worldData =
            DreamWorldSavedData.get(
                dreamLevel,
            )

        /*
         * Reading the map must not silently allocate a new region.
         * Players must already have a permanent Dream region.
         */
        val playerRegion =
            worldData.getRegion(
                serverPlayer.uuid,
            )
                ?: run {
                    serverPlayer.sendSystemMessage(
                        Component.literal(
                            "You do not have a Dream region yet.",
                        ),
                    )

                    return InteractionResultHolder.success(
                        itemStack,
                    )
                }

        val mapRows =
            DreamRegionMapFormatter.createMap(
                worldData,
                playerRegion,
            )

        serverPlayer.sendSystemMessage(
            Component.literal(
                "Dream regions centered on " +
                    "(${playerRegion.gridX}, ${playerRegion.gridZ}):",
            ),
        )

        serverPlayer.sendSystemMessage(
            Component.literal(
                "North (-Z)",
            ),
        )

        for (row in mapRows) {
            serverPlayer.sendSystemMessage(
                Component.literal(
                    row,
                ),
            )
        }

        serverPlayer.sendSystemMessage(
            Component.literal(
                "X = your region, O = allocated, . = unallocated",
            ),
        )

        return InteractionResultHolder.consume(
            itemStack,
        )
    }
}
