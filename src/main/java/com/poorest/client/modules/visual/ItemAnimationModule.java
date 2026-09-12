package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;

/**
 * Lightweight first-person animation preset module.
 * Custom Animations has the complete controller; this module is useful as a
 * simple preset-only alternative when the full controller is disabled.
 */
public final class ItemAnimationModule extends Module {
    private final ModeSetting preset = addSetting(new ModeSetting(
            "Preset", "Simple item animation preset.", "1.8 / Vanilla",
            "1.7 / Old 1.7", "1.8 / Vanilla", "Smooth / Exhibition", "Push / Punch",
            "Slide", "Stab / Poke", "Swank", "Tap / Tap 1.7", "Sigma",
            "Shield / Shield Block", "Vertical / Spin"
    ));
    private final NumberSetting swing = addSetting(new NumberSetting(
            "Swing Speed", "Animation speed.", 1.0, 0.25, 2.5, 0.05
    ));
    private final NumberSetting scale = addSetting(new NumberSetting(
            "Scale", "Uniform first-person item scale.", 1.0, 0.5, 1.6, 0.05
    ));
    private final NumberSetting x = addSetting(new NumberSetting(
            "Position X", "Horizontal item offset.", 0.0, -0.5, 0.5, 0.01
    ));
    private final NumberSetting y = addSetting(new NumberSetting(
            "Position Y", "Vertical item offset.", 0.0, -0.5, 0.5, 0.01
    ));
    private final NumberSetting z = addSetting(new NumberSetting(
            "Position Z", "Depth item offset.", 0.0, -0.5, 0.5, 0.01
    ));

    public ItemAnimationModule() {
        super("Item Animations", "Simple preset-based first-person item animations.", ModuleCategory.VISUAL);
    }

    public String preset() { return preset.getValue(); }
    public float swing() { return (float) swing.getValueAsDouble(); }
    public float scale() { return (float) scale.getValueAsDouble(); }
    public float x() { return (float) x.getValueAsDouble(); }
    public float y() { return (float) y.getValueAsDouble(); }
    public float z() { return (float) z.getValueAsDouble(); }
}
