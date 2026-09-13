package dev.yen.dream.world.region

import dev.yen.dream.world.DreamDimensions
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import java.util.UUID

class DreamWorldSavedData private constructor(
    private val regions: MutableMap<UUID, DreamRegion>,
) : SavedData() {
    constructor() : this(
        mutableMapOf(),
    )

    fun getRegion(playerId: UUID): DreamRegion? = regions[playerId]

    fun hasRegion(playerId: UUID): Boolean = regions.containsKey(playerId)

    fun isGridCellOccupied(
        gridX: Int,
        gridZ: Int,
    ): Boolean =
        regions.values.any { region ->
            region.gridX == gridX &&
                region.gridZ == gridZ
        }

    fun assignRegion(
        playerId: UUID,
        region: DreamRegion,
    ): Boolean {
        if (regions.containsKey(playerId)) {
            return false
        }

        if (
            isGridCellOccupied(
                region.gridX,
                region.gridZ,
            )
        ) {
            return false
        }

        regions[playerId] = region

        setDirty()

        return true
    }

    fun setSpawn(
        playerId: UUID,
        spawnPos: BlockPos,
    ): Boolean {
        val region =
            regions[playerId]
                ?: return false

        if (!region.containsBlock(spawnPos)) {
            return false
        }

        regions[playerId] =
            region.copy(
                spawnPos = spawnPos,
            )

        setDirty()

        return true
    }

    fun allRegions(): Map<UUID, DreamRegion> = regions.toMap()

    override fun save(tag: CompoundTag): CompoundTag {
        tag.putInt(
            DATA_VERSION_KEY,
            CURRENT_DATA_VERSION,
        )

        val regionList = ListTag()

        for ((playerId, region) in regions) {
            val regionTag = CompoundTag()

            regionTag.putUUID(
                PLAYER_UUID_KEY,
                playerId,
            )

            regionTag.putInt(
                GRID_X_KEY,
                region.gridX,
            )

            regionTag.putInt(
                GRID_Z_KEY,
                region.gridZ,
            )

            region.spawnPos?.let { spawnPos ->
                regionTag.putLong(
                    SPAWN_POS_KEY,
                    spawnPos.asLong(),
                )
            }

            regionList.add(regionTag)
        }

        tag.put(
            REGIONS_KEY,
            regionList,
        )

        return tag
    }

    companion object {
        private const val DATA_NAME =
            "dream_world"

        private const val CURRENT_DATA_VERSION =
            1

        private const val DATA_VERSION_KEY =
            "version"

        private const val REGIONS_KEY =
            "regions"

        private const val PLAYER_UUID_KEY =
            "player_uuid"

        private const val GRID_X_KEY =
            "grid_x"

        private const val GRID_Z_KEY =
            "grid_z"

        private const val SPAWN_POS_KEY =
            "spawn_pos"

        fun get(level: ServerLevel): DreamWorldSavedData {
            require(
                level.dimension() == DreamDimensions.DREAM,
            ) {
                "DreamWorldSavedData must be loaded from dream:dream"
            }

            return level.dataStorage.computeIfAbsent(
                ::load,
                { DreamWorldSavedData() },
                DATA_NAME,
            )
        }

        private fun load(tag: CompoundTag): DreamWorldSavedData {
            val regions =
                mutableMapOf<UUID, DreamRegion>()

            val regionList =
                tag.getList(
                    REGIONS_KEY,
                    Tag.TAG_COMPOUND.toInt(),
                )

            for (index in 0 until regionList.size) {
                val regionTag =
                    regionList.getCompound(index)

                if (!regionTag.hasUUID(PLAYER_UUID_KEY)) {
                    continue
                }

                if (
                    !regionTag.contains(
                        GRID_X_KEY,
                        Tag.TAG_INT.toInt(),
                    )
                ) {
                    continue
                }

                if (
                    !regionTag.contains(
                        GRID_Z_KEY,
                        Tag.TAG_INT.toInt(),
                    )
                ) {
                    continue
                }

                val playerId =
                    regionTag.getUUID(
                        PLAYER_UUID_KEY,
                    )

                val gridX =
                    regionTag.getInt(
                        GRID_X_KEY,
                    )

                val gridZ =
                    regionTag.getInt(
                        GRID_Z_KEY,
                    )

                val spawnPos =
                    if (
                        regionTag.contains(
                            SPAWN_POS_KEY,
                            Tag.TAG_LONG.toInt(),
                        )
                    ) {
                        BlockPos.of(
                            regionTag.getLong(
                                SPAWN_POS_KEY,
                            ),
                        )
                    } else {
                        null
                    }

                regions[playerId] =
                    DreamRegion(
                        gridX = gridX,
                        gridZ = gridZ,
                        spawnPos = spawnPos,
                    )
            }

            return DreamWorldSavedData(regions)
        }
    }
}
