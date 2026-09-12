package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

/** Stable client-side visual time override with safe world-transition handling. */
public final class TimeChangerModule extends Module {
    private final NumberSetting time = addSetting(new NumberSetting(
            "Time", "Visual world time.", 6000.0, 0.0, 24000.0, 500.0
    ));
    private long previousTime;
    private ClientLevel capturedLevel;

    public TimeChangerModule() {
        super("Time Changer", "Changes the client-side visual time of day without a per-frame flicker.", ModuleCategory.WORLD);
    }

    public double getTime() {
        return time.getValueAsDouble();
    }

    @Override
    protected void onEnable() {
        captureCurrentWorld();
        apply();
    }

    @Override
    public void onTick() {
        // Time is applied immediately before level rendering instead of every game tick.
        // This prevents vanilla/server time updates from becoming visible for a frame.
    }

    /** Applies the visual time immediately before a frame is rendered. */
    public void applyForRender() {
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
            mc.level.setDayTime(previousTime);
        }
        capturedLevel = null;
    }

    private void captureCurrentWorld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        capturedLevel = mc.level;
        previousTime = mc.level.getDayTime();
    }

    private void apply() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        long target = ((long) time.getValueAsDouble()) % 24000L;
        if (target < 0) target += 24000L;
        if (mc.level.getDayTime() != target) {
            mc.level.setDayTime(target);
        }
    }
}
