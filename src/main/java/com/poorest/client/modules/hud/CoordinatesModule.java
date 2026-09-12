package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class CoordinatesModule extends Module {

    public CoordinatesModule() {
        super(
                "Coordinates",
                "Displays your current XYZ position.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
