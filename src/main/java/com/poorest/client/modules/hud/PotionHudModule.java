package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class PotionHudModule extends Module {
    public PotionHudModule() {
        super("Potion HUD", "Shows active effects and their remaining duration.", ModuleCategory.HUD);
    }
}
