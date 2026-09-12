package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class ReducedViewBobbingModule extends Module {

    public ReducedViewBobbingModule() {
        super(
                "Reduced View Bobbing",
                "Reduces camera bobbing while moving.",
                ModuleCategory.VISUAL
        );
    }
}
