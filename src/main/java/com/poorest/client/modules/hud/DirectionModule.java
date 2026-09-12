package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class DirectionModule extends Module {

    public DirectionModule() {
        super(
                "Direction",
                "Displays your current facing direction.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
