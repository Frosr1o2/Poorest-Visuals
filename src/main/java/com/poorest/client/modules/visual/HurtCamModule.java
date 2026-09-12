package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class HurtCamModule extends Module {
    private final NumberSetting strength = addSetting(new NumberSetting(
            "Strength", "Camera roll strength while taking damage.", 1.0, 0.0, 2.0, 0.05
    ));

    public HurtCamModule() {
        super("Hurt Cam", "Controls the camera damage tilt.", ModuleCategory.VISUAL);
    }

    public float getStrength() {
        return (float) strength.getValueAsDouble();
    }
}
