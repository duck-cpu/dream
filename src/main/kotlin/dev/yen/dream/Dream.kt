package dev.yen.dream

import dev.yen.dream.client.DreamClient
import dev.yen.dream.command.DreamCommands
import dev.yen.dream.event.DreamBorderHandler
import dev.yen.dream.event.DreamRegionInteractionHandler
import dev.yen.dream.event.DreamSessionPersistenceHandler
import dev.yen.dream.event.DreamSleepHandler
import dev.yen.dream.progression.DreamProgressionEvents
import dev.yen.dream.registry.DreamBlockEntities
import dev.yen.dream.registry.DreamBlocks
import dev.yen.dream.registry.DreamItems
import dev.yen.dream.session.DreamSessionTicker
import net.minecraft.world.entity.Entity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import org.slf4j.LoggerFactory
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

@Mod(Dream.MOD_ID)
object Dream {
    const val MOD_ID = "dream"

    private val logger =
        LoggerFactory.getLogger(
            Dream::class.java,
        )

    init {
        val modEventBus =
            MOD_CONTEXT.getKEventBus()

        modEventBus.addListener(
            DreamProgressionEvents::onRegisterCapabilities,
        )

        DreamBlocks.register(
            modEventBus,
        )

        DreamItems.register(
            modEventBus,
        )

        DreamBlockEntities.register(
            modEventBus,
        )

        logger.info(
            "Entering the dream...",
        )

        MinecraftForge.EVENT_BUS.addGenericListener(
            Entity::class.java,
            DreamProgressionEvents::onAttachCapabilities,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamProgressionEvents,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamCommands,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamSleepHandler,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamSessionTicker,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamSessionPersistenceHandler,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamBorderHandler,
        )

        MinecraftForge.EVENT_BUS.register(
            DreamRegionInteractionHandler,
        )

        /*
         * Register rendering and other client-only handlers only
         * on the physical Minecraft client.
         */
        DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
        ) {
            Runnable {
                DreamClient.register()
            }
        }
    }
}
