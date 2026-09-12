package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;

/** Cosmetic cone/hat drawn above the local player's head. */
public final class ChinaHatModule extends Module {
    private final NumberSetting radius = addSetting(new NumberSetting(
            "Radius", "Hat radius in blocks.", 0.55, 0.20, 1.20, 0.05
    ));
    private final NumberSetting height = addSetting(new NumberSetting(
            "Height", "Hat cone height.", 0.22, 0.08, 0.60, 0.02
    ));
    private final NumberSetting alpha = addSetting(new NumberSetting(
            "Opacity", "Hat opacity.", 0.65, 0.10, 1.0, 0.05
    ));
    private final ModeSetting color = addSetting(new ModeSetting(
            "Color", "Hat color.", "Purple", "Purple", "Cyan", "Pink", "White", "Green", "Rainbow"
    ));
    private final BooleanSetting outline = addSetting(new BooleanSetting(
            "Outline", "Draw a thin outline around the hat.", true
    ));

    public ChinaHatModule() {
        super("China Hat", "Cosmetic cone hat above your character.", ModuleCategory.VISUAL);
    }

    public float radius() { return (float) radius.getValueAsDouble(); }
    public float height() { return (float) height.getValueAsDouble(); }
    public float alpha() { return (float) alpha.getValueAsDouble(); }
    public String color() { return color.getValue(); }
    public boolean outline() { return outline.isEnabled(); }
}
