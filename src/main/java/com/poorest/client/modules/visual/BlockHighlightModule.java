package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;

public final class BlockHighlightModule extends Module {
    private final ModeSetting color = addSetting(new ModeSetting(
            "Color", "Block outline color.", "Purple",
            "Purple", "Red", "Green", "Cyan", "Yellow", "White"
    ));
    private final NumberSetting thickness = addSetting(new NumberSetting(
            "Thickness", "Block outline thickness.", 1.0, 1.0, 1.0, 1.0
    ));

    public BlockHighlightModule() {
        super("Block Highlight", "Improves the block selection outline with a configurable color.", ModuleCategory.VISUAL);
    }

    public String color() { return color.getValue(); }
    public float thickness() { return (float) thickness.getValueAsDouble(); }
}
