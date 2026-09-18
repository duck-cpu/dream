package dev.yen.dream.registry

import dev.yen.dream.Dream
import dev.yen.dream.block.GildedPotBlock
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object DreamBlocks {
    private val BLOCKS =
        DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            Dream.MOD_ID,
        )

    private val ITEMS =
        DeferredRegister.create(
            ForgeRegistries.ITEMS,
            Dream.MOD_ID,
        )

    /*
     * The Gilded Pot is the physical Dream Anchor.
     *
     * A nearby Gilded Pot enables Dream entry for eligible beds.
     * Storage behavior will be added after the physical block behavior
     * is verified.
     */
    val GILDED_POT: RegistryObject<GildedPotBlock> =
        BLOCKS.register("gilded_pot") {
            GildedPotBlock(
                BlockBehaviour.Properties
                    .of()
                    /*
                     * Similar durability to the current prototype.
                     */
                    .strength(2.0f)
                    /*
                     * Use vanilla decorated-pot sounds for placement,
                     * stepping, hits, and normal block interaction.
                     */
                    .sound(
                        SoundType.DECORATED_POT,
                    )
                    /*
                     * The rendered model does not occupy the entire
                     * Minecraft block cube.
                     */
                    .noOcclusion(),
            )
        }

    /*
     * Inventory/placeable representation of the Gilded Pot.
     */
    val GILDED_POT_ITEM =
        ITEMS.register("gilded_pot") {
            BlockItem(
                GILDED_POT.get(),
                Item.Properties(),
            )
        }

    fun register(eventBus: IEventBus) {
        BLOCKS.register(eventBus)
        ITEMS.register(eventBus)
    }
}
