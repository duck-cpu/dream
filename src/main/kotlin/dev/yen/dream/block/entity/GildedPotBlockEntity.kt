package dev.yen.dream.block.entity

import dev.yen.dream.registry.DreamBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
import net.minecraft.world.level.block.state.BlockState

class GildedPotBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : RandomizableContainerBlockEntity(
        DreamBlockEntities.GILDED_POT.get(),
        pos,
        state,
    ) {
    private var items =
        NonNullList.withSize(
            CONTAINER_SIZE,
            ItemStack.EMPTY,
        )

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(
            tag,
        )

        if (!trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(
                tag,
                items,
            )
        }
    }

    override fun load(tag: CompoundTag) {
        super.load(
            tag,
        )

        items =
            NonNullList.withSize(
                containerSize,
                ItemStack.EMPTY,
            )

        if (!tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(
                tag,
                items,
            )
        }
    }

    override fun getContainerSize(): Int = CONTAINER_SIZE

    override fun getItems(): NonNullList<ItemStack> = items

    override fun setItems(items: NonNullList<ItemStack>) {
        this.items = items
    }

    override fun getDefaultName(): Component =
        Component.translatable(
            "container.dream.gilded_pot",
        )

    override fun createMenu(
        containerId: Int,
        inventory: Inventory,
    ): AbstractContainerMenu =
        ChestMenu(
            MenuType.GENERIC_9x1,
            containerId,
            inventory,
            this,
            1,
        )

    companion object {
        private const val CONTAINER_SIZE = 9
    }
}
