package dev.yen.dream.client.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import dev.yen.dream.world.DreamDimensions
import dev.yen.dream.world.region.DreamRegion
import dev.yen.dream.client.DreamShaders
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.joml.Matrix4f
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object DreamBorderRenderer {
    /*
     * Begin showing the Dream veil when the player gets reasonably
     * close to one of their region boundaries.
     *
     * We only render nearby pieces of the border rather than drawing
     * the entire 400-block wall every frame.
     */
    private const val RENDER_DISTANCE = 40.0

    /*
     * Only render a vertical section around the camera.
     *
     * The logical border still extends through the full build height;
     * this is purely a rendering optimization.
     */
    private const val VERTICAL_RADIUS = 28.0

    /*
     * Each portal sprite covers a relatively large section of wall.
     *
     * The earlier 4-block tiles made the repeating portal pattern look
     * like wallpaper. Larger tiles make the animation read more like a
     * continuous supernatural veil.
     */
    private const val TILE_SIZE = 16.0

    /*
     * Pull the visible wall slightly inside the player's region.
     *
     * The authoritative server-side border remains exactly on the region
     * boundary. This offset only prevents visual z-fighting.
     */
    private const val WALL_INSET = 0.002

    /*
     * Maximum opacity when standing directly against the border.
     *
     * The border fades toward complete transparency as the player
     * approaches RENDER_DISTANCE.
     */
    private const val MAX_ALPHA = 0.58f

    /*
     * Use the vanilla Nether portal sprite as our temporary animated
     * source texture.
     *
     * Because this sprite comes from Minecraft's block texture atlas,
     * Minecraft automatically advances its animation frames.
     *
     * Later we can replace this with Dream's own animated sprite or
     * recolor it properly with a custom shader.
     */
    private val PORTAL_SPRITE =
        ResourceLocation(
            "minecraft",
            "block/nether_portal",
        )

    @SubscribeEvent
    fun onRenderLevel(
        event: RenderLevelStageEvent,
    ) {
        /*
         * Draw after normal translucent world geometry.
         */
        if (
            event.stage !=
            RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS
        ) {
            return
        }

        val minecraft =
            Minecraft.getInstance()

        val player =
            minecraft.player
                ?: return

        val level =
            minecraft.level
                ?: return

        /*
         * Dream region borders only exist visually inside dream:dream.
         */
        if (
            level.dimension() !=
            DreamDimensions.DREAM
        ) {
            return
        }
        /*
        * Shader registration occurs during client resource loading.
        *
        * If rendering happens before it is available, simply skip this frame.
        */
        if (DreamShaders.borderShader == null) {
            return
        }

        /*
         * Dream regions form a deterministic 400 x 400 block grid.
         *
         * The server remains authoritative for actual region ownership
         * and movement restrictions. The client only derives the grid
         * cell it currently occupies so it knows where to draw the veil.
         *
         * floorDiv() is important because it handles negative regions:
         *
         *     X    0..399  -> gridX  0
         *     X  400..799  -> gridX  1
         *     X -400..-1   -> gridX -1
         */
        val regionSize =
            DreamRegion.REGION_SIZE_BLOCKS

        val gridX =
            Math.floorDiv(
                Mth.floor(player.x),
                regionSize,
            )

        val gridZ =
            Math.floorDiv(
                Mth.floor(player.z),
                regionSize,
            )

        /*
         * Convert the player's grid coordinate into the four precise
         * geometric border planes.
         */
        val minX =
            gridX * regionSize.toDouble()

        val maxX =
            minX + regionSize

        val minZ =
            gridZ * regionSize.toDouble()

        val maxZ =
            minZ + regionSize

        /*
         * Retrieve the animated Nether portal sprite from the block atlas.
         */
        val sprite =
            minecraft
                .getTextureAtlas(
                    TextureAtlas.LOCATION_BLOCKS,
                )
                .apply(
                    PORTAL_SPRITE,
                )

        val cameraPos =
            event.camera.position

        val poseStack =
            event.poseStack

        poseStack.pushPose()

        /*
         * Our geometry is specified using world coordinates.
         *
         * Convert it into camera-relative render coordinates.
         */
        poseStack.translate(
            -cameraPos.x,
            -cameraPos.y,
            -cameraPos.z,
        )

        setupRenderState()

        val buffer =
            Tesselator
                .getInstance()
                .builder

        buffer.begin(
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION_TEX_COLOR,
        )

        val matrix =
            poseStack
                .last()
                .pose()

        /*
         * WEST
         */
        renderXWall(
            matrix = matrix,
            sprite = sprite,
            wallX = minX + WALL_INSET,
            regionMinZ = minZ,
            regionMaxZ = maxZ,
            cameraX = cameraPos.x,
            cameraY = cameraPos.y,
            cameraZ = cameraPos.z,
        )

        /*
         * EAST
         */
        renderXWall(
            matrix = matrix,
            sprite = sprite,
            wallX = maxX - WALL_INSET,
            regionMinZ = minZ,
            regionMaxZ = maxZ,
            cameraX = cameraPos.x,
            cameraY = cameraPos.y,
            cameraZ = cameraPos.z,
        )

        /*
         * NORTH
         */
        renderZWall(
            matrix = matrix,
            sprite = sprite,
            wallZ = minZ + WALL_INSET,
            regionMinX = minX,
            regionMaxX = maxX,
            cameraX = cameraPos.x,
            cameraY = cameraPos.y,
            cameraZ = cameraPos.z,
        )

        /*
         * SOUTH
         */
        renderZWall(
            matrix = matrix,
            sprite = sprite,
            wallZ = maxZ - WALL_INSET,
            regionMinX = minX,
            regionMaxX = maxX,
            cameraX = cameraPos.x,
            cameraY = cameraPos.y,
            cameraZ = cameraPos.z,
        )

        BufferUploader.drawWithShader(
            buffer.end(),
        )

        restoreRenderState()

        poseStack.popPose()
    }

    /**
     * Render an east/west wall where X remains constant.
     */
    private fun renderXWall(
        matrix: Matrix4f,
        sprite: TextureAtlasSprite,
        wallX: Double,
        regionMinZ: Double,
        regionMaxZ: Double,
        cameraX: Double,
        cameraY: Double,
        cameraZ: Double,
    ) {
        val distance =
            abs(
                cameraX - wallX,
            )

        /*
         * Completely skip walls outside the visual range.
         */
        if (distance >= RENDER_DISTANCE) {
            return
        }

        val alpha =
            calculateAlpha(
                distance,
            )

        /*
         * Only draw the horizontal section of wall near the camera.
         */
        val visibleMinZ =
            max(
                regionMinZ,
                cameraZ - RENDER_DISTANCE,
            )

        val visibleMaxZ =
            min(
                regionMaxZ,
                cameraZ + RENDER_DISTANCE,
            )

        val visibleMinY =
            cameraY - VERTICAL_RADIUS

        val visibleMaxY =
            cameraY + VERTICAL_RADIUS

        /*
         * Anchor tile boundaries to world coordinates rather than camera
         * coordinates so the texture pattern does not slide around as
         * the player moves.
         */
        var z =
            floor(
                visibleMinZ / TILE_SIZE,
            ) * TILE_SIZE

        while (z < visibleMaxZ) {
            val tileMinZ =
                max(
                    z,
                    visibleMinZ,
                )

            val tileMaxZ =
                min(
                    z + TILE_SIZE,
                    visibleMaxZ,
                )

            var y =
                floor(
                    visibleMinY / TILE_SIZE,
                ) * TILE_SIZE

            while (y < visibleMaxY) {
                val tileMinY =
                    max(
                        y,
                        visibleMinY,
                    )

                val tileMaxY =
                    min(
                        y + TILE_SIZE,
                        visibleMaxY,
                    )

                addXTile(
                    matrix = matrix,
                    sprite = sprite,
                    x = wallX,
                    y1 = tileMinY,
                    y2 = tileMaxY,
                    z1 = tileMinZ,
                    z2 = tileMaxZ,
                    alpha = alpha,
                )

                y += TILE_SIZE
            }

            z += TILE_SIZE
        }
    }

    /**
     * Render a north/south wall where Z remains constant.
     */
    private fun renderZWall(
        matrix: Matrix4f,
        sprite: TextureAtlasSprite,
        wallZ: Double,
        regionMinX: Double,
        regionMaxX: Double,
        cameraX: Double,
        cameraY: Double,
        cameraZ: Double,
    ) {
        val distance =
            abs(
                cameraZ - wallZ,
            )

        if (distance >= RENDER_DISTANCE) {
            return
        }

        /*
         * Use exactly the same distance-fade behavior as X walls.
         *
         * The earlier debug version forced this to 1.0, which made
         * north/south borders completely opaque.
         */
        val alpha =
            calculateAlpha(
                distance,
            )

        val visibleMinX =
            max(
                regionMinX,
                cameraX - RENDER_DISTANCE,
            )

        val visibleMaxX =
            min(
                regionMaxX,
                cameraX + RENDER_DISTANCE,
            )

        val visibleMinY =
            cameraY - VERTICAL_RADIUS

        val visibleMaxY =
            cameraY + VERTICAL_RADIUS

        var x =
            floor(
                visibleMinX / TILE_SIZE,
            ) * TILE_SIZE

        while (x < visibleMaxX) {
            val tileMinX =
                max(
                    x,
                    visibleMinX,
                )

            val tileMaxX =
                min(
                    x + TILE_SIZE,
                    visibleMaxX,
                )

            var y =
                floor(
                    visibleMinY / TILE_SIZE,
                ) * TILE_SIZE

            while (y < visibleMaxY) {
                val tileMinY =
                    max(
                        y,
                        visibleMinY,
                    )

                val tileMaxY =
                    min(
                        y + TILE_SIZE,
                        visibleMaxY,
                    )

                addZTile(
                    matrix = matrix,
                    sprite = sprite,
                    x1 = tileMinX,
                    x2 = tileMaxX,
                    y1 = tileMinY,
                    y2 = tileMaxY,
                    z = wallZ,
                    alpha = alpha,
                )

                y += TILE_SIZE
            }

            x += TILE_SIZE
        }
    }

    /**
     * Convert distance from the boundary into veil opacity.
     */
    private fun calculateAlpha(
        distance: Double,
    ): Float {
        val proximity =
            1.0 -
                (
                    distance /
                        RENDER_DISTANCE
                    )

        /*
         * Squaring proximity makes the wall very subtle at long range
         * while letting it become materially visible up close.
         */
        val curved =
            proximity * proximity

        return (
            curved.coerceIn(
                0.0,
                1.0,
            ) * MAX_ALPHA
            ).toFloat()
    }

    /**
     * Submit one portal tile on an X-aligned wall.
     */
    private fun addXTile(
        matrix: Matrix4f,
        sprite: TextureAtlasSprite,
        x: Double,
        y1: Double,
        y2: Double,
        z1: Double,
        z2: Double,
        alpha: Float,
    ) {
        val buffer =
            Tesselator
                .getInstance()
                .builder

        /*
         * Keep vertex color completely neutral.
         *
         * We are no longer trying to recolor the vanilla portal through
         * vertex multiplication because that produces muddy results.
         */
        buffer
            .vertex(
                matrix,
                x.toFloat(),
                y1.toFloat(),
                z1.toFloat(),
            )
            .uv(
                sprite.u0,
                sprite.v1,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x.toFloat(),
                y1.toFloat(),
                z2.toFloat(),
            )
            .uv(
                sprite.u1,
                sprite.v1,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x.toFloat(),
                y2.toFloat(),
                z2.toFloat(),
            )
            .uv(
                sprite.u1,
                sprite.v0,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x.toFloat(),
                y2.toFloat(),
                z1.toFloat(),
            )
            .uv(
                sprite.u0,
                sprite.v0,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()
    }

    /**
     * Submit one portal tile on a Z-aligned wall.
     */
    private fun addZTile(
        matrix: Matrix4f,
        sprite: TextureAtlasSprite,
        x1: Double,
        x2: Double,
        y1: Double,
        y2: Double,
        z: Double,
        alpha: Float,
    ) {
        val buffer =
            Tesselator
                .getInstance()
                .builder

        buffer
            .vertex(
                matrix,
                x1.toFloat(),
                y1.toFloat(),
                z.toFloat(),
            )
            .uv(
                sprite.u0,
                sprite.v1,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x2.toFloat(),
                y1.toFloat(),
                z.toFloat(),
            )
            .uv(
                sprite.u1,
                sprite.v1,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x2.toFloat(),
                y2.toFloat(),
                z.toFloat(),
            )
            .uv(
                sprite.u1,
                sprite.v0,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()

        buffer
            .vertex(
                matrix,
                x1.toFloat(),
                y2.toFloat(),
                z.toFloat(),
            )
            .uv(
                sprite.u0,
                sprite.v0,
            )
            .color(
                1.0f,
                1.0f,
                1.0f,
                alpha,
            )
            .endVertex()
    }

    /**
    * Configure rendering for the translucent Dream veil.
    */
    private fun setupRenderState() {
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        /*
        * The veil must be visible from both sides.
        */
        RenderSystem.disableCull()

        /*
        * Terrain can obscure the veil, but the translucent veil itself
        * should not write into the depth buffer.
        */
        RenderSystem.enableDepthTest()
        RenderSystem.depthMask(false)

        /*
        * Use Dream's custom luminance-remapping shader instead of
        * Minecraft's normal PositionTexColor shader.
        */
        RenderSystem.setShader {
            DreamShaders.borderShader!!
        }

        /*
        * The shader still samples the vanilla animated portal sprite
        * from Minecraft's block atlas.
        */

        RenderSystem.setShaderTexture(
            0,
            TextureAtlas.LOCATION_BLOCKS,
        )
    }

    /**
    * Restore global rendering state after drawing.
    */
    private fun restoreRenderState() {
        RenderSystem.depthMask(true)
        RenderSystem.enableCull()
        RenderSystem.disableBlend()
    }
}

