package dev.yen.dream.progression

/**
 * Identifies which persistent progression state is currently loaded
 * into the player's normal Minecraft fields.
 *
 * WAKING covers the Overworld, Nether, End, and other non-Dream
 * dimensions. DREAM applies only while the player is in dream:dream.
 */
enum class DreamProgressionRealm(
    val serializedName: String,
) {
    WAKING(
        serializedName = "waking",
    ),

    DREAM(
        serializedName = "dream",
    ),
    ;

    companion object {
        /**
         * Unknown or corrupted values fall back to the waking state.
         *
         * Later, login reconciliation will compare this value against
         * the player's dimension and active DreamSession.
         */
        fun fromSerializedName(serializedName: String): DreamProgressionRealm =
            entries.firstOrNull { realm ->
                realm.serializedName == serializedName
            } ?: WAKING
    }
}
