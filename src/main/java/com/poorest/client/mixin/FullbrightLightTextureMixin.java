package com.poorest.client.mixin;

import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.modules.visual.FullbrightModule;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightTexture.class)
public abstract class FullbrightLightTextureMixin {

    @Redirect(
            method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LightTexture;getBrightness(Lnet/minecraft/world/level/dimension/DimensionType;I)F"
            )
    )
    private float poorest$fullbrightBrightness(DimensionType dimensionType, int lightLevel) {
        FullbrightModule module = (FullbrightModule) ModuleManager.getModule("Fullbright");
        if (module != null && module.isEnabled()) {
            return 1.0F;
        }

        return LightTexture.getBrightness(dimensionType, lightLevel);
    }
}
