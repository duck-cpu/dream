package dev.yen.dream.progression

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object DreamProgressionEvents {
    /**
     * Register the type represented by the capability token.
     *
     * This event is received through the MOD event bus.
     */
    fun onRegisterCapabilities(event: RegisterCapabilitiesEvent) {
        event.register(
            DreamProgressionData::class.java,
        )
    }

    /**
     * Attach an independent provider to every player entity.
     *
     * This generic event is registered explicitly from Dream.kt using
     * addGenericListener.
     */
    fun onAttachCapabilities(event: AttachCapabilitiesEvent<Entity>) {
        if (event.`object` !is Player) {
            return
        }

        val provider =
            DreamProgressionProvider()

        event.addCapability(
            DreamProgressionCapability.ID,
            provider,
        )

        event.addListener {
            provider.invalidate()
        }
    }

    /**
     * Copy progression metadata to the new player entity after death.
     */
    @SubscribeEvent
    fun onPlayerClone(event: PlayerEvent.Clone) {
        if (!event.isWasDeath) {
            return
        }

        event.original.reviveCaps()

        try {
            DreamProgressionCapability
                .get(
                    event.original,
                ).ifPresent { originalData ->
                    DreamProgressionCapability
                        .get(
                            event.entity,
                        ).ifPresent { clonedData ->
                            clonedData.copyFrom(
                                originalData,
                            )
                        }
                }
        } finally {
            event.original.invalidateCaps()
        }
    }
}
