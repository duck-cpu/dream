package dev.yen.dream.progression

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance

/**
 * Converts between a live ServerPlayer and a persistent progression
 * snapshot.
 *
 * The caller must close or resolve any open container before calling
 * capture. This prevents cursor and crafting-grid items from being
 * omitted from the snapshot.
 */
object DreamProgressionStateCodec {
    fun capture(player: ServerPlayer): DreamProgressionState {
        val inventoryTag =
            player.inventory.save(
                ListTag(),
            )

        val enderChestTag =
            player.enderChestInventory.createTag()

        val foodDataTag =
            CompoundTag().also { tag ->
                player.foodData.addAdditionalSaveData(
                    tag,
                )
            }

        val effectsTag =
            ListTag()

        for (effect in player.activeEffects) {
            effectsTag.add(
                effect.save(
                    CompoundTag(),
                ),
            )
        }

        return DreamProgressionState.create(
            inventoryTag = inventoryTag,
            enderChestTag = enderChestTag,
            selectedSlot = player.inventory.selected,
            experienceProgress = player.experienceProgress,
            experienceLevel = player.experienceLevel,
            totalExperience = player.totalExperience,
            health = player.health,
            absorption = player.absorptionAmount,
            foodDataTag = foodDataTag,
            effectsTag = effectsTag,
        )
    }

    /**
     * Replace the player's active vanilla state with a snapshot.
     *
     * This must only run on the logical server.
     */
    fun apply(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        applyInventory(
            player,
            state,
        )

        applyEnderChest(
            player,
            state,
        )

        applyExperience(
            player,
            state,
        )

        applyFoodData(
            player,
            state,
        )

        applyEffects(
            player,
            state,
        )

        /*
         * Apply health after effects because effects may modify the
         * player's maximum-health attribute.
         */
        player.health =
            state.health.coerceAtLeast(
                1.0F,
            )

        player.absorptionAmount =
            state.absorption.coerceAtLeast(
                0.0F,
            )

        synchronizeClient(
            player,
        )
    }

    private fun applyInventory(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        player.inventory.clearContent()

        player.inventory.load(
            state.copyInventoryTag(),
        )

        player.inventory.selected =
            state.selectedSlot.coerceIn(
                0,
                8,
            )

        player.inventory.setChanged()
    }

    private fun applyEnderChest(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        player.enderChestInventory.clearContent()

        player.enderChestInventory.fromTag(
            state.copyEnderChestTag(),
        )

        player.enderChestInventory.setChanged()
    }

    private fun applyExperience(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        player.experienceProgress =
            state.experienceProgress.coerceIn(
                0.0F,
                1.0F,
            )

        player.experienceLevel =
            state.experienceLevel.coerceAtLeast(
                0,
            )

        player.totalExperience =
            state.totalExperience.coerceAtLeast(
                0,
            )
    }

    private fun applyFoodData(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        player.foodData.readAdditionalSaveData(
            state.copyFoodDataTag(),
        )
    }

    private fun applyEffects(
        player: ServerPlayer,
        state: DreamProgressionState,
    ) {
        player.removeAllEffects()

        val effectsTag =
            state.copyEffectsTag()

        for (index in 0 until effectsTag.size) {
            val effect =
                MobEffectInstance.load(
                    effectsTag.getCompound(
                        index,
                    ),
                )

            if (effect != null) {
                player.addEffect(
                    effect,
                )
            }
        }
    }

    /**
     * Force the client to immediately display the restored progression.
     */
    private fun synchronizeClient(player: ServerPlayer) {
        player.inventoryMenu.broadcastChanges()

        if (
            player.containerMenu !==
            player.inventoryMenu
        ) {
            player.containerMenu.broadcastChanges()
        }

        player.connection.send(
            ClientboundSetCarriedItemPacket(
                player.inventory.selected,
            ),
        )

        player.connection.send(
            ClientboundSetExperiencePacket(
                player.experienceProgress,
                player.totalExperience,
                player.experienceLevel,
            ),
        )

        player.connection.send(
            ClientboundSetHealthPacket(
                player.health,
                player.foodData.foodLevel,
                player.foodData.saturationLevel,
            ),
        )
    }
}
