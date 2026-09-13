package dev.yen.dream.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class DreamTimeMixin {

    private static final ResourceKey<Level> DREAM_LEVEL =
        ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("dream", "dream")
        );

    @Inject(
        method = "getDayTime",
        at = @At("RETURN"),
        cancellable = true
    )
    private void dream$oppositeTime(
        CallbackInfoReturnable<Long> cir
    ) {
        Level level =
            (Level) (Object) this;

        // Only change what the server reports.
        // The server then sends that Dream time to the client.
        if (level.isClientSide()) {
            return;
        }

        if (!level.dimension().equals(DREAM_LEVEL)) {
            return;
        }

        cir.setReturnValue(
            cir.getReturnValue() + 12000L
        );
    }
}
