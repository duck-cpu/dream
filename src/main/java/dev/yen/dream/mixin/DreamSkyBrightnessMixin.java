package dev.yen.dream.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class DreamSkyBrightnessMixin {

    @Shadow
    private int skyDarken;

    @Inject(
        method = "updateSkyBrightness",
        at = @At("HEAD"),
        cancellable = true
    )
    private void dream$updateSkyBrightness(
        CallbackInfo ci
    ) {
        Level level =
            (Level) (Object) this;

        if (level.isClientSide()) {
            return;
        }

        ResourceKey<Level> dimension =
            level.dimension();

        if (!dimension.location().getNamespace().equals("dream")
            || !dimension.location().getPath().equals("dream")) {
            return;
        }

        /*
         * getDayTime() is already offset by DreamTimeMixin,
         * so calculate the Dream sun angle from that value.
         */
        float timeOfDay =
            level.dimensionType()
                .timeOfDay(level.getDayTime());

        double rainBrightness =
            1.0D
                - (double) (
                    level.getRainLevel(1.0F) * 5.0F
                ) / 16.0D;

        double thunderBrightness =
            1.0D
                - (double) (
                    level.getThunderLevel(1.0F) * 5.0F
                ) / 16.0D;

        double sunBrightness =
            0.5D
                + 2.0D
                * Mth.clamp(
                    (double) Mth.cos(
                        timeOfDay
                            * ((float) Math.PI * 2.0F)
                    ),
                    -0.25D,
                    0.25D
                );

        this.skyDarken =
            (int) (
                (
                    1.0D
                        - sunBrightness
                        * rainBrightness
                        * thunderBrightness
                )
                    * 11.0D
            );

        ci.cancel();
    }
}
