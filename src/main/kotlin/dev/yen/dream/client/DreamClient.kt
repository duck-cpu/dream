package dev.yen.dream.client

import dev.yen.dream.client.render.DreamBorderRenderer
import net.minecraftforge.common.MinecraftForge
import org.slf4j.LoggerFactory
import thedarkcolour.kotlinforforge.forge.MOD_CONTEXT

object DreamClient {
    private val logger =
        LoggerFactory.getLogger(
            DreamClient::class.java,
        )

    private var registered =
        false

    /**
     * Registers Dream's client-only systems.
     */
    fun register() {
        if (registered) {
            return
        }

        registered = true

        logger.info(
            "Registering Dream client event handlers",
        )

        /*
         * RegisterShadersEvent is fired on the MOD event bus.
         */
        val modEventBus =
            MOD_CONTEXT.getKEventBus()

        modEventBus.addListener(
            DreamShaders::onRegisterShaders,
        )

        /*
         * World rendering events are fired on the normal Forge bus.
         */
        MinecraftForge.EVENT_BUS.register(
            DreamBorderRenderer,
        )
    }
}
