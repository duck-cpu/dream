package dev.yen.dream.progression

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

/**
 * Player-owned persistent progression metadata.
 *
 * The player's normal Minecraft fields contain the active progression.
 * This object stores only:
 *
 * 1. Which realm is currently active.
 * 2. The inactive realm's progression snapshot.
 */
class DreamProgressionData {
    var activeRealm: DreamProgressionRealm =
        DreamProgressionRealm.WAKING
        private set

    private var inactiveState: DreamProgressionState? =
        null

    fun hasInactiveState(): Boolean = inactiveState != null

    /**
     * Return a defensive copy so callers cannot mutate the stored state.
     */
    fun copyInactiveState(): DreamProgressionState? =
        inactiveState?.let { state ->
            copyState(
                state,
            )
        }

    /**
     * Complete one progression transition.
     *
     * The state is copied before either field is changed. If copying ever
     * fails, the existing stored data remains untouched.
     */
    fun completeSwap(
        newActiveRealm: DreamProgressionRealm,
        newlyInactiveState: DreamProgressionState,
    ) {
        val copiedInactiveState =
            copyState(
                newlyInactiveState,
            )

        inactiveState =
            copiedInactiveState

        activeRealm =
            newActiveRealm
    }

    /**
     * Copy progression metadata during PlayerEvent.Clone.
     */
    fun copyFrom(other: DreamProgressionData) {
        val copiedInactiveState =
            other.copyInactiveState()

        activeRealm =
            other.activeRealm

        inactiveState =
            copiedInactiveState
    }

    /**
     * Reset to the state of a player who has never entered the Dream.
     *
     * This is intended for explicit recovery logic, not normal waking.
     */
    fun reset() {
        activeRealm =
            DreamProgressionRealm.WAKING

        inactiveState =
            null
    }

    fun save(): CompoundTag =
        CompoundTag().apply {
            putInt(
                KEY_SCHEMA_VERSION,
                CURRENT_SCHEMA_VERSION,
            )

            putString(
                KEY_ACTIVE_REALM,
                activeRealm.serializedName,
            )

            inactiveState?.let { state ->
                put(
                    KEY_INACTIVE_STATE,
                    state.save(),
                )
            }
        }

    /**
     * Replace the current contents using serialized capability data.
     *
     * Values are decoded into temporary variables first so a failure
     * cannot leave this object half-loaded.
     */
    fun load(tag: CompoundTag) {
        val loadedRealm =
            if (
                tag.contains(
                    KEY_ACTIVE_REALM,
                    Tag.TAG_STRING.toInt(),
                )
            ) {
                DreamProgressionRealm.fromSerializedName(
                    tag.getString(
                        KEY_ACTIVE_REALM,
                    ),
                )
            } else {
                DreamProgressionRealm.WAKING
            }

        val loadedInactiveState =
            if (
                tag.contains(
                    KEY_INACTIVE_STATE,
                    Tag.TAG_COMPOUND.toInt(),
                )
            ) {
                DreamProgressionState.load(
                    tag.getCompound(
                        KEY_INACTIVE_STATE,
                    ),
                )
            } else {
                null
            }

        activeRealm =
            loadedRealm

        inactiveState =
            loadedInactiveState
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        private const val KEY_SCHEMA_VERSION =
            "SchemaVersion"

        private const val KEY_ACTIVE_REALM =
            "ActiveRealm"

        private const val KEY_INACTIVE_STATE =
            "InactiveState"

        private fun copyState(state: DreamProgressionState): DreamProgressionState =
            DreamProgressionState.load(
                state.save(),
            )
    }
}
