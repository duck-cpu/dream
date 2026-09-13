package dev.yen.dream.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class DreamSeedMixin {

    private static final ResourceKey<Level> DREAM_LEVEL =
        ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation("dream", "dream")
        );

    private static final long DREAM_SEED_SALT =
        0x445245414D5F5345L;

    @Inject(
        method = "getSeed",
        at = @At("RETURN"),
        cancellable = true
    )
    private void dream$useDifferentSeed(
        CallbackInfoReturnable<Long> cir
    ) {
        ServerLevel level =
            (ServerLevel) (Object) this;

        if (!level.dimension().equals(DREAM_LEVEL)) {
            return;
        }

        long seed =
            cir.getReturnValue() ^ DREAM_SEED_SALT;

        cir.setReturnValue(mix64(seed));
    }

    private static long mix64(long value) {
        value =
            (value ^ (value >>> 30))
                * 0xbf58476d1ce4e5b9L;

        value =
            (value ^ (value >>> 27))
                * 0x94d049bb133111ebL;

        return value ^ (value >>> 31);
    }
}
