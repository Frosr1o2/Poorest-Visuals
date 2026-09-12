package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

/**
 * Real FPS optimization. It only changes real vanilla options and never fabricates frames.
 * The module deliberately does not touch the options during the mod constructor phase.
 */
public final class PerformanceModule extends Module {
    private final NumberSetting renderDistance = addSetting(new NumberSetting(
            "Render Distance",
            "How many chunks are rendered around the player while Performance is enabled.",
            8.0D,
            2.0D,
            32.0D,
            1.0D
    ));

    private final NumberSetting simulationDistance = addSetting(new NumberSetting(
            "Simulation Distance",
            "How many chunks remain actively simulated while Performance is enabled.",
            5.0D,
            5.0D,
            32.0D,
            1.0D
    ));

    private final ModeSetting preset = addSetting(new ModeSetting(
            "Preset",
            "Real vanilla rendering preset used to reduce GPU and CPU work.",
            "Max FPS",
            "Max FPS", "Balanced"
    ));

    private Integer previousRenderDistance;
    private Integer previousSimulationDistance;
    private Double previousEntityDistance;
    private Integer previousMaxFps;
    private GraphicsStatus previousGraphics;
    private Boolean previousAo;
    private CloudStatus previousClouds;
    private ParticleStatus previousParticles;
    private Boolean previousShadows;
    private Boolean previousVsync;
    private Integer previousBiomeBlend;
    private boolean captured;
    private boolean pendingApply;

    public PerformanceModule() {
        super("Performance", "Real FPS optimization using vanilla rendering settings. No fake frames.", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        // Module states may be restored while Minecraft is still constructing Options.
        // Do not touch any OptionInstance here; wait for the first client tick.
        pendingApply = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (!ready(mc)) return;
        if (!captured) capture(mc);
        if (pendingApply || captured) {
            apply(mc);
            pendingApply = false;
        }
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (!captured || mc.options == null) return;

        if (previousRenderDistance != null) mc.options.renderDistance().set(previousRenderDistance);
        if (previousSimulationDistance != null) mc.options.simulationDistance().set(previousSimulationDistance);
        if (previousEntityDistance != null) mc.options.entityDistanceScaling().set(previousEntityDistance);
        if (previousMaxFps != null) mc.options.framerateLimit().set(previousMaxFps);
        if (previousGraphics != null) mc.options.graphicsMode().set(previousGraphics);
        if (previousAo != null) mc.options.ambientOcclusion().set(previousAo);
        if (previousClouds != null) mc.options.cloudStatus().set(previousClouds);
        if (previousParticles != null) mc.options.particles().set(previousParticles);
        if (previousShadows != null) mc.options.entityShadows().set(previousShadows);
        if (previousVsync != null) mc.options.enableVsync().set(previousVsync);
        if (previousBiomeBlend != null) mc.options.biomeBlendRadius().set(previousBiomeBlend);
        mc.options.save();
        captured = false;
        pendingApply = false;
    }

    private boolean ready(Minecraft mc) {
        return mc.options != null && mc.levelRenderer != null;
    }

    private void capture(Minecraft mc) {
        if (!ready(mc) || captured) return;
        previousRenderDistance = mc.options.renderDistance().get();
        previousSimulationDistance = mc.options.simulationDistance().get();
        previousEntityDistance = mc.options.entityDistanceScaling().get();
        previousMaxFps = mc.options.framerateLimit().get();
        previousGraphics = mc.options.graphicsMode().get();
        previousAo = mc.options.ambientOcclusion().get();
        previousClouds = mc.options.cloudStatus().get();
        previousParticles = mc.options.particles().get();
        previousShadows = mc.options.entityShadows().get();
        previousVsync = mc.options.enableVsync().get();
        previousBiomeBlend = mc.options.biomeBlendRadius().get();
        captured = true;
    }

    private void apply(Minecraft mc) {
        if (!ready(mc)) return;
        if (preset.is("Balanced")) setBalanced(mc); else setMaxFps(mc);
    }

    private void setMaxFps(Minecraft mc) {
        set(mc.options.renderDistance(), clampRender((int) Math.round(renderDistance.getValueAsDouble())));
        set(mc.options.simulationDistance(), clampSimulation((int) Math.round(simulationDistance.getValueAsDouble())));
        set(mc.options.entityDistanceScaling(), 0.50D);
        set(mc.options.framerateLimit(), 260);
        set(mc.options.graphicsMode(), GraphicsStatus.FAST);
        set(mc.options.ambientOcclusion(), false);
        set(mc.options.cloudStatus(), CloudStatus.OFF);
        set(mc.options.particles(), ParticleStatus.MINIMAL);
        set(mc.options.entityShadows(), false);
        set(mc.options.enableVsync(), false);
        set(mc.options.biomeBlendRadius(), 0);
    }

    private void setBalanced(Minecraft mc) {
        set(mc.options.renderDistance(), clampRender((int) Math.round(renderDistance.getValueAsDouble())));
        set(mc.options.simulationDistance(), clampSimulation((int) Math.round(simulationDistance.getValueAsDouble())));
        set(mc.options.entityDistanceScaling(), 0.75D);
        set(mc.options.framerateLimit(), 260);
        set(mc.options.graphicsMode(), GraphicsStatus.FAST);
        set(mc.options.ambientOcclusion(), false);
        set(mc.options.cloudStatus(), CloudStatus.FAST);
        set(mc.options.particles(), ParticleStatus.DECREASED);
        set(mc.options.entityShadows(), false);
        set(mc.options.enableVsync(), false);
        set(mc.options.biomeBlendRadius(), 1);
    }

    private <T> void set(net.minecraft.client.OptionInstance<T> option, T value) {
        if (!java.util.Objects.equals(option.get(), value)) option.set(value);
    }

    private int clampRender(int value) { return Math.max(2, value); }
    private int clampSimulation(int value) { return Math.max(5, value); }
}
