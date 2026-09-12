package dev.yen.dream

import net.minecraftforge.fml.common.Mod
import org.slf4j.LoggerFactory

@Mod(Dream.MOD_ID)
object Dream {
    const val MOD_ID = "dream"

    private val logger = LoggerFactory.getLogger(Dream::class.java)

    init {
        logger.info("Entering the dream...")
    }
}
