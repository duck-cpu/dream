package dev.yen.dream.session

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

data class DreamSession(
    val originDimension: ResourceKey<Level>,
    val originPos: BlockPos,
    val endTick: Int
)
