package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

/** Hit-flash visual configuration used by the client renderer. */
public final class HitColorModule extends Module {

    private final NumberSetting intensity = addSetting(new NumberSetting(
            "Intensity", "Intensity of the local hit visual.", 0.55, 0.0, 1.0, 0.05
    ));

    private final NumberSetting duration = addSetting(new NumberSetting(
            "Duration", "Duration of the hit visual in ticks.", 5.0, 1.0, 15.0, 1.0
    ));

    public HitColorModule() {
        super(
                "Hit Color",
                "Clean hit feedback without changing gameplay.",
                ModuleCategory.COMBAT
        );
    }

    public double getIntensity() {
        return intensity.getValueAsDouble();
    }

    public double getDuration() {
        return duration.getValueAsDouble();
    }
}
