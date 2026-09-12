package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class ArmorHudModule extends Module {
    public ArmorHudModule() {
        super("Armor HUD", "Shows equipped armor, held items and durability.", ModuleCategory.HUD);
    }
}
