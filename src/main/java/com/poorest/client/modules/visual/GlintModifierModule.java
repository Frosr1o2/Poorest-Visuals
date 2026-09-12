package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

public final class GlintModifierModule extends Module {
    private final NumberSetting speed = addSetting(new NumberSetting("Speed", "Glint animation speed.", 1.0, 0.0, 3.0, 0.05));
    private final NumberSetting brightness = addSetting(new NumberSetting("Brightness", "Glint brightness.", 1.0, 0.0, 2.0, 0.05));

    public GlintModifierModule() {
        super("Glint Modifier", "Controls enchanted-item glint intensity and animation speed.", ModuleCategory.VISUAL);
    }

    public float speed() { return (float) speed.getValueAsDouble(); }
    public float brightness() { return (float) brightness.getValueAsDouble(); }
}
