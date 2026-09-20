package dev.yen.dream.block

import dev.yen.dream.block.entity.GildedPotBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class GildedPotBlock(
    properties: Properties,
) : BaseEntityBlock(properties) {

    /*
     * Collision/selection geometry matching the final Blockbench model.
     *
     * Minecraft voxel shapes use the same 0..16 coordinate space as
     * Java block models, so these values correspond directly to the
     * exported Gilded Pot geometry.
     */

    /*
     * Main ceramic body.
     *
     * Model:
     *     from [4, 0, 4]
     *     to   [12, 13, 12]
     */
    private val bodyShape: VoxelShape =
        box(
            4.0,
            0.0,
            4.0,
            12.0,
            13.0,
            12.0,
        )

    /*
     * Narrow neck above the body.
     *
     * Model:
     *     from [6, 13, 6]
     *     to   [10, 14, 10]
     */
    private val neckShape: VoxelShape =
        box(
            6.0,
            13.0,
            6.0,
            10.0,
            14.0,
            10.0,
        )

    /*
     * The rim is actually a hollow square in the Blockbench model.
     *
     * Keep the four pieces separate so the selection/collision shape
     * respects the visible opening instead of treating the whole top
     * as one solid plate.
     */

    private val northRimShape: VoxelShape =
        box(
            5.0,
            14.0,
            5.0,
            11.0,
            15.0,
            6.0,
        )

    private val southRimShape: VoxelShape =
        box(
            5.0,
            14.0,
            10.0,
            11.0,
            15.0,
            11.0,
        )

    private val westRimShape: VoxelShape =
        box(
            5.0,
            14.0,
            6.0,
            6.0,
            15.0,
            10.0,
        )

    private val eastRimShape: VoxelShape =
        box(
            10.0,
            14.0,
            6.0,
            11.0,
            15.0,
            10.0,
        )

    /*
     * Complete physical shape of the Gilded Pot.
     */
    private val potShape: VoxelShape =
        Shapes.or(
            bodyShape,
            neckShape,
            northRimShape,
            southRimShape,
            westRimShape,
            eastRimShape,
        )

    /*
     * Selection/outline shape.
     *
     * This controls the wireframe outline shown while looking at the pot.
     */
    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape =
        potShape

    /*
     * Physical collision shape.
     *
     * Explicitly return the same model-matching shape rather than
     * allowing the block to behave like a full 16 x 16 x 16 cube.
     */
    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape =
        potShape

        /**
     * Create the persistent storage object associated with this
     * particular placed Gilded Pot.
     */
    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState,
    ): BlockEntity =
        GildedPotBlockEntity(
            pos,
            state,
        )

    /**
     * Open the Gilded Pot inventory when the player right-clicks it.
     */
    override fun use(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult,
    ): InteractionResult {
        if (!level.isClientSide) {
            val blockEntity =
                level.getBlockEntity(
                    pos,
                )

            if (blockEntity is GildedPotBlockEntity) {
                player.openMenu(
                    blockEntity,
                )
            }
        }

        return InteractionResult.sidedSuccess(
            level.isClientSide,
        )
    }

    /**
     * Drop the inventory contents when the block itself is removed.
     *
     * The pot item continues to come from the existing block loot table.
     */
    override fun onRemove(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        newState: BlockState,
        isMoving: Boolean,
    ) {
        if (!state.`is`(newState.block)) {
            val blockEntity =
                level.getBlockEntity(
                    pos,
                )

            if (blockEntity is Container) {
                Containers.dropContents(
                    level,
                    pos,
                    blockEntity,
                )

                level.updateNeighbourForOutputSignal(
                    pos,
                    this,
                )
            }

            super.onRemove(
                state,
                level,
                pos,
                newState,
                isMoving,
            )
        }
    }

    /**
     * BaseEntityBlock is invisible by default, so explicitly retain
     * the Gilded Pot's existing block model.
     */
    override fun getRenderShape(
        state: BlockState,
    ): RenderShape =
        RenderShape.MODEL

    /**
     * Allow comparators to measure how full the pot is.
     */
    override fun hasAnalogOutputSignal(
        state: BlockState,
    ): Boolean =
        true

    override fun getAnalogOutputSignal(
        state: BlockState,
        level: Level,
        pos: BlockPos,
    ): Int =
        AbstractContainerMenu.getRedstoneSignalFromBlockEntity(
            level.getBlockEntity(pos),
        )
}
