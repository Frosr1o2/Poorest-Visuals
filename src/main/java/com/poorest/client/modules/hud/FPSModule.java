package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.NumberSetting;

public final class FPSModule extends Module {

    private final BooleanSetting showLabel =
            addSetting(
                    new BooleanSetting(
                            "Show Label",
                            "Displays the FPS label.",
                            true
                    )
            );

    private final NumberSetting scale =
            addSetting(
                    new NumberSetting(
                            "Scale",
                            "Changes the size of the FPS display.",
                            1.0,
                            0.5,
                            2.0,
                            0.1
                    )
            );

    public FPSModule() {
        super(
                "FPS",
                "Displays your current FPS.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }

    public boolean isShowLabel() {
        return showLabel.isEnabled();
    }

    public double getScale() {
        return scale.getValueAsDouble();
    }

    public static void renderHud() {
        PoorestHud.render();
    }
}
