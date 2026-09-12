package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import net.minecraft.client.Minecraft;

/** Keeps rain and thunder visually disabled on the client. */
public final class NoWeatherVisualModule extends Module {

    public NoWeatherVisualModule() {
        super(
                "Weather Visuals",
                "Disables rain and thunder rendering on the client.",
                ModuleCategory.VISUAL
        );
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        mc.level.setRainLevel(0.0F);
        mc.level.setThunderLevel(0.0F);
    }
}
