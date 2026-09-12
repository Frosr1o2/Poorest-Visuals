package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/** Stable client-side clear weather with safe world-transition handling. */
public final class ClearWeatherModule extends Module {
    private float previousRain;
    private float previousThunder;
    private ClientLevel capturedLevel;

    public ClearWeatherModule() {
        super("Clear Weather", "Removes rain and thunder visually without constantly rewriting the same values.", ModuleCategory.WORLD);
    }

    @Override
    protected void onEnable() {
        captureCurrentWorld();
        apply();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (capturedLevel != mc.level) captureCurrentWorld();
        apply();
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (capturedLevel != null && mc.level == capturedLevel) {
            mc.level.setRainLevel(previousRain);
            mc.level.setThunderLevel(previousThunder);
        }
        capturedLevel = null;
    }

    private void captureCurrentWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        capturedLevel = mc.level;
        previousRain = mc.level.getRainLevel(1.0F);
        previousThunder = mc.level.getThunderLevel(1.0F);
    }

    private void apply() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (mc.level.getRainLevel(1.0F) != 0.0F) mc.level.setRainLevel(0.0F);
        if (mc.level.getThunderLevel(1.0F) != 0.0F) mc.level.setThunderLevel(0.0F);
    }
}
