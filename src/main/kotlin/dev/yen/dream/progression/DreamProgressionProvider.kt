package dev.yen.dream.progression

import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional

/**
 * Owns one player's progression capability data and connects it
 * to Forge's capability serialization lifecycle.
 */
class DreamProgressionProvider : ICapabilitySerializable<CompoundTag> {
    private val progressionData =
        DreamProgressionData()

    /*
     * LazyOptional instances cannot become valid again after
     * invalidate() is called.
     *
     * Forge may invalidate and later revive a player's capability
     * dispatcher during dimension travel. The progression data itself
     * remains valid, so create a new wrapper when Forge queries this
     * provider after revival.
     */
    private var optionalProgressionData =
        createOptional()

    private var optionalIsValid =
        true

    override fun <T : Any?> getCapability(
        capability: Capability<T>,
        side: Direction?,
    ): LazyOptional<T> {
        if (
            capability !==
            DreamProgressionCapability.INSTANCE
        ) {
            return LazyOptional.empty()
        }

        return getOrCreateOptional().cast()
    }

    override fun serializeNBT(): CompoundTag = progressionData.save()

    override fun deserializeNBT(tag: CompoundTag) {
        progressionData.load(
            tag,
        )
    }

    /**
     * Called when Forge invalidates the owning player's capabilities.
     */
    fun invalidate() {
        if (!optionalIsValid) {
            return
        }

        optionalProgressionData.invalidate()
        optionalIsValid =
            false
    }

    /**
     * Recreate only the LazyOptional wrapper after Forge revives the
     * player's capability dispatcher. The existing progression data is
     * deliberately preserved.
     */
    private fun getOrCreateOptional(): LazyOptional<DreamProgressionData> {
        if (!optionalIsValid) {
            optionalProgressionData =
                createOptional()

            optionalIsValid =
                true
        }

        return optionalProgressionData
    }

    private fun createOptional(): LazyOptional<DreamProgressionData> =
        LazyOptional.of {
            progressionData
        }
}
