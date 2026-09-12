package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class WatermarkModule extends Module {

    public WatermarkModule() {
        super(
                "Watermark",
                "Displays the Poorest Visuals watermark.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
