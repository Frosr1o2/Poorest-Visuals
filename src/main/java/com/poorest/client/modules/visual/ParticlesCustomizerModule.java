package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class ParticlesCustomizerModule extends Module {
    private final NumberSetting scale = addSetting(new NumberSetting("Scale", "Visual particle scale.", 1.0, 0.25, 2.0, 0.05));

    public ParticlesCustomizerModule() {
        super("Particles Customizer", "Controls the visual scale of client particle effects.", ModuleCategory.VISUAL);
    }

    public float scale() { return (float) scale.getValueAsDouble(); }
}
