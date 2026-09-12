package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;

/** Target HUD configuration. */
public final class TargetHudModule extends Module {
    private final NumberSetting holdTime = addSetting(new NumberSetting(
            "Hold Time", "How long the last valid target remains visible after leaving the crosshair.",
            2.5, 2.0, 3.0, 0.1
    ));

    private final NumberSetting animationSpeed = addSetting(new NumberSetting(
            "Animation Speed", "Target HUD entrance and exit animation speed.",
            0.22, 0.10, 0.40, 0.01
    ));

    public TargetHudModule() {
        super("Target HUD", "Shows information about the entity currently under your crosshair with a short animated hold.", ModuleCategory.HUD);
    }

    public long holdTimeMillis() {
        return Math.round(holdTime.getValueAsDouble() * 1000.0D);
    }

    public float animationSpeed() {
        return (float) animationSpeed.getValueAsDouble();
    }
}
