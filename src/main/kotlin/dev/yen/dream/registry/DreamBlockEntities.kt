package dev.yen.dream.registry

import dev.yen.dream.Dream
import dev.yen.dream.block.entity.GildedPotBlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object DreamBlockEntities {
    private val BLOCK_ENTITY_TYPES =
        DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES,
            Dream.MOD_ID,
        )

    val GILDED_POT: RegistryObject<BlockEntityType<GildedPotBlockEntity>> =
        BLOCK_ENTITY_TYPES.register("gilded_pot") {
            BlockEntityType.Builder
                .of(
                    ::GildedPotBlockEntity,
                    DreamBlocks.GILDED_POT.get(),
                ).build(
                    null,
                )
        }

    fun register(eventBus: IEventBus) {
        BLOCK_ENTITY_TYPES.register(
            eventBus,
        )
    }
}
