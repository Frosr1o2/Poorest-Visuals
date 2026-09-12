package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class FovModule extends Module {
    private final NumberSetting fov = addSetting(new NumberSetting(
            "FOV", "Client field of view.", 90.0, 30.0, 150.0, 1.0
    ));

    public FovModule() {
        super("FOV", "Expanded field-of-view controls.", ModuleCategory.VISUAL);
    }

    public float getFov() {
        return (float) fov.getValueAsDouble();
    }
}
