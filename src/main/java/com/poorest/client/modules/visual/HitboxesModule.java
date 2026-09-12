package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;

/** Vanilla-style entity hitbox debug rendering, with a configurable color. */
public final class HitboxesModule extends Module {
    private final ModeSetting color = addSetting(new ModeSetting(
            "Color", "Hitbox outline color.", "Purple",
            "Purple", "Red", "Green", "Cyan", "Yellow", "White"
    ));
    private final NumberSetting thickness = addSetting(new NumberSetting(
            "Thickness", "Hitbox line thickness.", 1.0, 1.0, 1.0, 1.0
    ));
    private final NumberSetting range = addSetting(new NumberSetting(
            "Range", "Maximum distance for debug hitboxes.", 48.0, 8.0, 96.0, 4.0
    ));

    public HitboxesModule() {
        super("Hitboxes", "Vanilla-style F3+B entity hitboxes with a custom color.", ModuleCategory.VISUAL);
    }

    public String color() { return color.getValue(); }
    public float thickness() { return (float) thickness.getValueAsDouble(); }
    public float range() { return (float) range.getValueAsDouble(); }
}
