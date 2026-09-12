package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class CrosshairModule extends Module {

    public CrosshairModule() {
        super(
                "Crosshair",
                "Draws a clean custom crosshair.",
                ModuleCategory.VISUAL
        );

        setEnabled(true);
    }
}
