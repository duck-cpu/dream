package dev.yen.dream.progression

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag

/**
 * A complete serialized snapshot of one inactive player progression.
 *
 * This class only stores data. It does not read from or modify a player.
 * DreamProgressionService will perform those operations later.
 */
class DreamProgressionState private constructor(
    inventoryTag: ListTag,
    enderChestTag: ListTag,
    val selectedSlot: Int,
    val experienceProgress: Float,
    val experienceLevel: Int,
    val totalExperience: Int,
    val health: Float,
    val absorption: Float,
    foodDataTag: CompoundTag,
    effectsTag: ListTag,
) {
    /*
     * NBT tags are mutable, so keep private copies inside the snapshot.
     */
    private val inventoryTag =
        inventoryTag.copy()

    private val enderChestTag =
        enderChestTag.copy()

    private val foodDataTag =
        foodDataTag.copy()

    private val effectsTag =
        effectsTag.copy()

    fun copyInventoryTag(): ListTag = inventoryTag.copy()

    fun copyEnderChestTag(): ListTag = enderChestTag.copy()

    fun copyFoodDataTag(): CompoundTag = foodDataTag.copy()

    fun copyEffectsTag(): ListTag = effectsTag.copy()

    /**
     * Serialize this snapshot for persistent storage.
     */
    fun save(): CompoundTag =
        CompoundTag().apply {
            putInt(
                KEY_SCHEMA_VERSION,
                CURRENT_SCHEMA_VERSION,
            )

            put(
                KEY_INVENTORY,
                inventoryTag.copy(),
            )

            put(
                KEY_ENDER_CHEST,
                enderChestTag.copy(),
            )

            putInt(
                KEY_SELECTED_SLOT,
                selectedSlot,
            )

            putFloat(
                KEY_EXPERIENCE_PROGRESS,
                experienceProgress,
            )

            putInt(
                KEY_EXPERIENCE_LEVEL,
                experienceLevel,
            )

            putInt(
                KEY_TOTAL_EXPERIENCE,
                totalExperience,
            )

            putFloat(
                KEY_HEALTH,
                health,
            )

            putFloat(
                KEY_ABSORPTION,
                absorption,
            )

            put(
                KEY_FOOD_DATA,
                foodDataTag.copy(),
            )

            put(
                KEY_EFFECTS,
                effectsTag.copy(),
            )
        }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        private const val DEFAULT_HEALTH = 20.0F
        private const val DEFAULT_FOOD_LEVEL = 20
        private const val DEFAULT_SATURATION = 5.0F
        private const val DEFAULT_EXHAUSTION = 0.0F
        private const val DEFAULT_FOOD_TICK_TIMER = 0

        private const val KEY_SCHEMA_VERSION =
            "SchemaVersion"

        private const val KEY_INVENTORY =
            "Inventory"

        private const val KEY_ENDER_CHEST =
            "EnderChest"

        private const val KEY_SELECTED_SLOT =
            "SelectedSlot"

        private const val KEY_EXPERIENCE_PROGRESS =
            "ExperienceProgress"

        private const val KEY_EXPERIENCE_LEVEL =
            "ExperienceLevel"

        private const val KEY_TOTAL_EXPERIENCE =
            "TotalExperience"

        private const val KEY_HEALTH =
            "Health"

        private const val KEY_ABSORPTION =
            "Absorption"

        private const val KEY_FOOD_DATA =
            "FoodData"

        private const val KEY_EFFECTS =
            "Effects"

        /**
         * Construct a snapshot from freshly captured player data.
         */
        fun create(
            inventoryTag: ListTag,
            enderChestTag: ListTag,
            selectedSlot: Int,
            experienceProgress: Float,
            experienceLevel: Int,
            totalExperience: Int,
            health: Float,
            absorption: Float,
            foodDataTag: CompoundTag,
            effectsTag: ListTag,
        ): DreamProgressionState =
            DreamProgressionState(
                inventoryTag = inventoryTag,
                enderChestTag = enderChestTag,
                selectedSlot = selectedSlot,
                experienceProgress = experienceProgress,
                experienceLevel = experienceLevel,
                totalExperience = totalExperience,
                health = health,
                absorption = absorption,
                foodDataTag = foodDataTag,
                effectsTag = effectsTag,
            )

        /**
         * Create a player's initial Dream progression.
         *
         * It contains no items, Ender Chest contents, experience,
         * absorption, or status effects.
         */
        fun fresh(maximumHealth: Float = DEFAULT_HEALTH): DreamProgressionState =
            DreamProgressionState(
                inventoryTag = ListTag(),
                enderChestTag = ListTag(),
                selectedSlot = 0,
                experienceProgress = 0.0F,
                experienceLevel = 0,
                totalExperience = 0,
                health =
                    maximumHealth.coerceAtLeast(
                        1.0F,
                    ),
                absorption = 0.0F,
                foodDataTag = createFreshFoodData(),
                effectsTag = ListTag(),
            )

        /**
         * Reconstruct a progression snapshot from persistent NBT.
         *
         * Missing fields receive safe defaults so an older snapshot can
         * still load after future schema additions.
         */
        fun load(tag: CompoundTag): DreamProgressionState =
            DreamProgressionState(
                inventoryTag =
                    readCompoundList(
                        tag,
                        KEY_INVENTORY,
                    ),
                enderChestTag =
                    readCompoundList(
                        tag,
                        KEY_ENDER_CHEST,
                    ),
                selectedSlot =
                    if (
                        tag.contains(
                            KEY_SELECTED_SLOT,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag
                            .getInt(
                                KEY_SELECTED_SLOT,
                            ).coerceIn(
                                0,
                                8,
                            )
                    } else {
                        0
                    },
                experienceProgress =
                    if (
                        tag.contains(
                            KEY_EXPERIENCE_PROGRESS,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag.getFloat(
                            KEY_EXPERIENCE_PROGRESS,
                        )
                    } else {
                        0.0F
                    },
                experienceLevel =
                    if (
                        tag.contains(
                            KEY_EXPERIENCE_LEVEL,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag
                            .getInt(
                                KEY_EXPERIENCE_LEVEL,
                            ).coerceAtLeast(
                                0,
                            )
                    } else {
                        0
                    },
                totalExperience =
                    if (
                        tag.contains(
                            KEY_TOTAL_EXPERIENCE,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag
                            .getInt(
                                KEY_TOTAL_EXPERIENCE,
                            ).coerceAtLeast(
                                0,
                            )
                    } else {
                        0
                    },
                health =
                    if (
                        tag.contains(
                            KEY_HEALTH,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag
                            .getFloat(
                                KEY_HEALTH,
                            ).coerceAtLeast(
                                0.0F,
                            )
                    } else {
                        DEFAULT_HEALTH
                    },
                absorption =
                    if (
                        tag.contains(
                            KEY_ABSORPTION,
                            Tag.TAG_ANY_NUMERIC.toInt(),
                        )
                    ) {
                        tag
                            .getFloat(
                                KEY_ABSORPTION,
                            ).coerceAtLeast(
                                0.0F,
                            )
                    } else {
                        0.0F
                    },
                foodDataTag =
                    if (
                        tag.contains(
                            KEY_FOOD_DATA,
                            Tag.TAG_COMPOUND.toInt(),
                        )
                    ) {
                        tag.getCompound(
                            KEY_FOOD_DATA,
                        )
                    } else {
                        createFreshFoodData()
                    },
                effectsTag =
                    readCompoundList(
                        tag,
                        KEY_EFFECTS,
                    ),
            )

        private fun readCompoundList(
            parentTag: CompoundTag,
            key: String,
        ): ListTag {
            if (
                !parentTag.contains(
                    key,
                    Tag.TAG_LIST.toInt(),
                )
            ) {
                return ListTag()
            }

            return parentTag
                .getList(
                    key,
                    Tag.TAG_COMPOUND.toInt(),
                ).copy()
        }

        private fun createFreshFoodData(): CompoundTag =
            CompoundTag().apply {
                putInt(
                    "foodLevel",
                    DEFAULT_FOOD_LEVEL,
                )

                putInt(
                    "foodTickTimer",
                    DEFAULT_FOOD_TICK_TIMER,
                )

                putFloat(
                    "foodSaturationLevel",
                    DEFAULT_SATURATION,
                )

                putFloat(
                    "foodExhaustionLevel",
                    DEFAULT_EXHAUSTION,
                )
            }
    }
}
