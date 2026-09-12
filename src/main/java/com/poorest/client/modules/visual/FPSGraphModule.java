package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class FPSGraphModule extends Module {

    public FPSGraphModule() {
        super(
                "FPS Graph",
                "Shows recent FPS performance.",
                ModuleCategory.VISUAL
        );
    }
}
