package dev.yen.dream.block

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class GildedPotBlock(
    properties: Properties,
) : Block(properties) {

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
}
