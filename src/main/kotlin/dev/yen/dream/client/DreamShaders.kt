package dev.yen.dream.client

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import dev.yen.dream.Dream
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.RegisterShadersEvent

object DreamShaders {
    /*
     * The currently loaded Dream border shader.
     *
     * Minecraft can recreate ShaderInstance objects during resource
     * reloads, so this reference is replaced whenever the shader
     * registration callback supplies a newly loaded instance.
     */
    var borderShader: ShaderInstance? = null
        private set

    /**
     * Register Dream's custom border shader.
     *
     * RegisterShadersEvent is fired on the client mod event bus.
     */
    fun onRegisterShaders(event: RegisterShadersEvent) {
        val shader =
            ShaderInstance(
                event.resourceProvider,
                ResourceLocation(
                    Dream.MOD_ID,
                    "dream_border",
                ),
                DefaultVertexFormat.POSITION_TEX_COLOR,
            )

        /*
         * Store the actual loaded ShaderInstance returned by Forge.
         *
         * This is important for resource reloads because Minecraft may
         * replace the shader object while the game is running.
         */
        event.registerShader(
            shader,
        ) { loadedShader ->
            borderShader =
                loadedShader
        }
    }
}
