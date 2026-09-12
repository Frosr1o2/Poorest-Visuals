package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;

public final class ArraylistModule extends Module {
    private final BooleanSetting gradient = addSetting(new BooleanSetting(
            "Gradient", "Uses a moving accent gradient for module names.", true
    ));

    public ArraylistModule() {
        super("Arraylist", "Shows enabled client modules in a compact animated list.", ModuleCategory.HUD);
    }

    public boolean gradient() {
        return gradient.isEnabled();
    }
}
