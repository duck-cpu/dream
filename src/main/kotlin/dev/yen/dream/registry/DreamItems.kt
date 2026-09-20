package dev.yen.dream.registry

import dev.yen.dream.Dream
import dev.yen.dream.item.DreamRegionMapItem
import net.minecraft.world.item.Item
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object DreamItems {
    private val ITEMS =
        DeferredRegister.create(
            ForgeRegistries.ITEMS,
            Dream.MOD_ID,
        )

    /*
     * Development tool for displaying nearby Dream-region
     * allocation without modifying SavedData.
     */
    val DREAM_REGION_MAP: RegistryObject<DreamRegionMapItem> =
        ITEMS.register("dream_region_map") {
            DreamRegionMapItem(
                Item
                    .Properties()
                    .stacksTo(1),
            )
        }

    fun register(eventBus: IEventBus) {
        ITEMS.register(
            eventBus,
        )
    }
}
