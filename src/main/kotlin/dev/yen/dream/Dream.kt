package dev.yen.dream

import dev.yen.dream.command.DreamCommands
import dev.yen.dream.event.DreamSessionPersistenceHandler
import dev.yen.dream.event.DreamSleepHandler
import dev.yen.dream.registry.DreamBlocks
import dev.yen.dream.session.DreamSessionTicker
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import org.slf4j.LoggerFactory
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

@Mod(Dream.MOD_ID)
object Dream {
    const val MOD_ID = "dream"

    private val logger = LoggerFactory.getLogger(Dream::class.java)

    init {

        val modEventBus =
            MOD_CONTEXT.getKEventBus()

        DreamBlocks.register(modEventBus)

        logger.info("Entering the dream...")

        MinecraftForge.EVENT_BUS.register(DreamCommands)
        MinecraftForge.EVENT_BUS.register(DreamSleepHandler)
        MinecraftForge.EVENT_BUS.register(DreamSessionTicker)
        MinecraftForge.EVENT_BUS.register(
            DreamSessionPersistenceHandler,
        )
    }
}
