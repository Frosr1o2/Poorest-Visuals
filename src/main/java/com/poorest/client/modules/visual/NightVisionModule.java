package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class NightVisionModule extends Module {

    private boolean added;

    public NightVisionModule() {
        super(
                "Night Vision",
                "Keeps night vision active.",
                ModuleCategory.VISUAL
        );
    }

    @Override
    public void onTick() {
        Minecraft mc =
                Minecraft.getInstance();

        if (mc.player == null) {
            return;
        }

        if (!mc.player.hasEffect(
                MobEffects.NIGHT_VISION
        )) {
            mc.player.addEffect(
                    new MobEffectInstance(
                            MobEffects.NIGHT_VISION,
                            220,
                            0,
                            true,
                            false,
                            false
                    )
            );

            added = true;
        }
    }

    @Override
    protected void onDisable() {
        if (!added) {
            return;
        }

        Minecraft mc =
                Minecraft.getInstance();

        if (mc.player != null) {
            mc.player.removeEffect(
                    MobEffects.NIGHT_VISION
            );
        }

        added = false;
    }
}
