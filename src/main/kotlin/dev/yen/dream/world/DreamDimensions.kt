package dev.yen.dream.world

import dev.yen.dream.Dream
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

object DreamDimensions {
    val DREAM: ResourceKey<Level> =
        ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation(Dream.MOD_ID, "dream"),
        )
}
