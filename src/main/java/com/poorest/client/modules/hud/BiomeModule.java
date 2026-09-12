package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class BiomeModule extends Module {

    public BiomeModule() {
        super(
                "Biome",
                "Displays the biome you are currently in.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
