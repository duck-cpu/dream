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

    val DREAM_ANCHOR: RegistryObject<Block> =
        BLOCKS.register("dream_anchor") {
            Block(
                BlockBehaviour.Properties
                    .of()
                    .strength(2.0f),
            )
        }

    val DREAM_ANCHOR_ITEM =
        ITEMS.register("dream_anchor") {
            BlockItem(
                DREAM_ANCHOR.get(),
                Item.Properties(),
            )
        }

    fun register(eventBus: IEventBus) {
        BLOCKS.register(eventBus)
        ITEMS.register(eventBus)
    }
}
