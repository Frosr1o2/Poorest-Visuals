package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class DarkModeModule extends Module {

    public DarkModeModule() {
        super(
                "Dark Mode",
                "Uses darker client-side interface styling.",
                ModuleCategory.VISUAL
        );
    }
}
