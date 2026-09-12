package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class ClockModule extends Module {

    public ClockModule() {
        super(
                "Clock",
                "Displays the local system time.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
