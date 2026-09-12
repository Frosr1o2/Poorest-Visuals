package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class KeystrokesModule extends Module {
    public KeystrokesModule() {
        super("Keystrokes", "Shows WASD, mouse buttons and jump input.", ModuleCategory.HUD);
    }
}
