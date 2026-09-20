package dev.yen.dream.progression

import dev.yen.dream.Dream
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityToken
import net.minecraftforge.common.util.LazyOptional

object DreamProgressionCapability {
    /**
     * Forge capability token used to request a player's
     * DreamProgressionData.
     */
    val INSTANCE: Capability<DreamProgressionData> =
        CapabilityManager.get(
            object :
                CapabilityToken<DreamProgressionData>() {},
        )

    /**
     * Unique identifier used when attaching the provider to a player.
     */
    val ID =
        ResourceLocation(
            Dream.MOD_ID,
            "dream_progression",
        )

    fun get(player: Player): LazyOptional<DreamProgressionData> =
        player.getCapability(
            INSTANCE,
        )
}
