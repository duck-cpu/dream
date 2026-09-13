package dev.yen.dream.session

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level

object DreamSessionPersistence {
    private const val SESSION_TAG = "dream_session"
    private const val ORIGIN_DIMENSION = "origin_dimension"
    private const val ORIGIN_POS = "origin_pos"
    private const val REMAINING_TICKS = "remaining_ticks"

    fun save(
        player: ServerPlayer,
        session: DreamSession,
    ) {
        val sessionTag = CompoundTag()

        val remainingTicks =
            (session.endTick - player.server.tickCount)
                .coerceAtLeast(0)

        sessionTag.putString(
            ORIGIN_DIMENSION,
            session.originDimension.location().toString(),
        )

        sessionTag.putLong(
            ORIGIN_POS,
            session.originPos.asLong(),
        )

        sessionTag.putInt(
            REMAINING_TICKS,
            remainingTicks,
        )

        player.persistentData.put(
            SESSION_TAG,
            sessionTag,
        )
    }

    fun load(player: ServerPlayer): DreamSession? {
        val persistentData = player.persistentData

        if (!persistentData.contains(SESSION_TAG)) {
            return null
        }

        val sessionTag =
            persistentData.getCompound(SESSION_TAG)

        val dimensionLocation =
            ResourceLocation.tryParse(
                sessionTag.getString(ORIGIN_DIMENSION),
            ) ?: return null

        val originDimension: ResourceKey<Level> =
            ResourceKey.create(
                Registries.DIMENSION,
                dimensionLocation,
            )

        val originPos =
            BlockPos.of(
                sessionTag.getLong(ORIGIN_POS),
            )

        val remainingTicks =
            sessionTag
                .getInt(REMAINING_TICKS)
                .coerceAtLeast(0)

        return DreamSession(
            originDimension = originDimension,
            originPos = originPos,
            endTick = player.server.tickCount + remainingTicks,
        )
    }

    fun clear(player: ServerPlayer) {
        player.persistentData.remove(SESSION_TAG)
    }
}
