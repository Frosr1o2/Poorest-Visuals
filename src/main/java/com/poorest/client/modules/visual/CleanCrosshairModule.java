package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class CleanCrosshairModule extends Module {

    private final NumberSetting size =
            addSetting(
                    new NumberSetting(
                            "Size",
                            "Crosshair size.",
                            5.0,
                            2.0,
                            12.0,
                            1.0
                    )
            );

    private final NumberSetting gap =
            addSetting(
                    new NumberSetting(
                            "Gap",
                            "Space around the crosshair.",
                            3.0,
                            1.0,
                            8.0,
                            1.0
                    )
            );

    public CleanCrosshairModule() {
        super(
                "Clean Crosshair",
                "A compact modern crosshair.",
                ModuleCategory.VISUAL
        );
    }

    public double getSize() {
        return size.getValueAsDouble();
    }

    public double getGap() {
        return gap.getValueAsDouble();
    }
}
