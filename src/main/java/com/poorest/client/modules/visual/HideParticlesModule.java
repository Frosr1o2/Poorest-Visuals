package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class HideParticlesModule extends Module {

    public HideParticlesModule() {
        super(
                "Hide Particles",
                "Reduces visual particle clutter.",
                ModuleCategory.VISUAL
        );
    }
}
