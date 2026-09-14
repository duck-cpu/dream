package dev.yen.dream.registry

import dev.yen.dream.Dream
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
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
     * The Gilded Pot enables Dream entry when placed near a bed.
     *
     * For now this is still a basic Block. We'll give it its proper
     * collision shape, sounds, storage, and other pot behavior after
     * validating the exported Blockbench model in-game.
     */
    val GILDED_POT: RegistryObject<Block> =
        BLOCKS.register("gilded_pot") {
            Block(
                BlockBehaviour.Properties
                    .of()
                    .strength(2.0f)
                    /*
                     * The visual model does not fill the entire cube, so
                     * don't let Minecraft treat it as a full opaque cube
                     * for rendering/face occlusion purposes.
                     */
                    .noOcclusion(),
            )
        }

    /*
     * Inventory/placeable item corresponding to the Gilded Pot block.
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
