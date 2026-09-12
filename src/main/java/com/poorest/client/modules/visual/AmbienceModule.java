package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class AmbienceModule extends Module {
    private final NumberSetting red = addSetting(new NumberSetting("Red", "Fog red channel.", 0.55, 0.0, 1.0, 0.01));
    private final NumberSetting green = addSetting(new NumberSetting("Green", "Fog green channel.", 0.40, 0.0, 1.0, 0.01));
    private final NumberSetting blue = addSetting(new NumberSetting("Blue", "Fog blue channel.", 0.85, 0.0, 1.0, 0.01));

    public AmbienceModule() {
        super("Ambience", "Customizes client-side fog atmosphere colors.", ModuleCategory.WORLD);
    }

    public float red() { return (float) red.getValueAsDouble(); }
    public float green() { return (float) green.getValueAsDouble(); }
    public float blue() { return (float) blue.getValueAsDouble(); }
}
