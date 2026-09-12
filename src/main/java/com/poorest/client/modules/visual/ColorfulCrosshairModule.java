package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

/** Settings container for a more customizable crosshair style. */
public final class ColorfulCrosshairModule extends Module {

    private final NumberSetting length = addSetting(new NumberSetting(
            "Length", "Length of each crosshair arm.", 5.0, 2.0, 14.0, 1.0
    ));

    private final NumberSetting thickness = addSetting(new NumberSetting(
            "Thickness", "Thickness of the crosshair arms.", 1.0, 1.0, 4.0, 1.0
    ));

    private final NumberSetting gap = addSetting(new NumberSetting(
            "Gap", "Distance from the center.", 3.0, 0.0, 10.0, 1.0
    ));

    public ColorfulCrosshairModule() {
        super(
                "Crosshair+",
                "Extra crosshair controls for the Poorest HUD.",
                ModuleCategory.VISUAL
        );
    }

    public double getLength() {
        return length.getValueAsDouble();
    }

    public double getThickness() {
        return thickness.getValueAsDouble();
    }

    public double getGap() {
        return gap.getValueAsDouble();
    }
}
