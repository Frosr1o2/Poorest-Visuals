package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;

/**
 * Fully configurable first-person item animation controller.
 * The actual transform is applied by PoorestClient's RenderHandEvent hook.
 */
public final class CustomAnimationsModule extends Module {

    private final ModeSetting style = addSetting(new ModeSetting(
            "Preset",
            "First-person item animation preset.",
            "1.8 / Vanilla",
            "1.7 / Old 1.7",
            "1.8 / Vanilla",
            "Smooth / Exhibition",
            "Push / Punch",
            "Slide",
            "Stab / Poke",
            "Swank",
            "Tap / Tap 1.7",
            "Sigma",
            "Shield / Shield Block",
            "Vertical / Spin"
    ));

    private final NumberSetting scaleX = addSetting(new NumberSetting(
            "Item Size X", "Horizontal item scale.", 1.0, 0.5, 1.6, 0.05
    ));
    private final NumberSetting scaleY = addSetting(new NumberSetting(
            "Item Size Y", "Vertical item scale.", 1.0, 0.5, 1.6, 0.05
    ));
    private final NumberSetting scaleZ = addSetting(new NumberSetting(
            "Item Size Z", "Depth item scale.", 1.0, 0.5, 1.6, 0.05
    ));

    private final NumberSetting positionX = addSetting(new NumberSetting(
            "Position X", "Moves the item left or right.", 0.0, -0.50, 0.50, 0.01
    ));
    private final NumberSetting positionY = addSetting(new NumberSetting(
            "Position Y", "Moves the item up or down.", 0.0, -0.50, 0.50, 0.01
    ));
    private final NumberSetting positionZ = addSetting(new NumberSetting(
            "Position Z", "Moves the item closer or farther away.", 0.0, -0.50, 0.50, 0.01
    ));

    private final NumberSetting swingSpeed = addSetting(new NumberSetting(
            "Swing Speed", "Animation curve speed/intensity.", 1.0, 0.25, 2.5, 0.05
    ));
    private final NumberSetting swingStrength = addSetting(new NumberSetting(
            "Swing Strength", "Overall strength of the animation.", 1.0, 0.0, 2.0, 0.05
    ));

    private final NumberSetting blockX = addSetting(new NumberSetting(
            "Block Position X", "Shield/blocking horizontal position.", 0.0, -0.5, 0.5, 0.01
    ));
    private final NumberSetting blockY = addSetting(new NumberSetting(
            "Block Position Y", "Shield/blocking vertical position.", 0.0, -0.5, 0.5, 0.01
    ));
    private final NumberSetting blockZ = addSetting(new NumberSetting(
            "Block Position Z", "Shield/blocking depth position.", 0.0, -0.5, 0.5, 0.01
    ));

    private final BooleanSetting itemOnly = addSetting(new BooleanSetting(
            "Items Only", "Only transform held items and not the empty hand/arm.", true
    ));

    public CustomAnimationsModule() {
        super(
                "Custom Animations",
                "Modern first-person item animations with 11 presets and full transform controls.",
                ModuleCategory.PLAYER
        );
    }

    public String style() {
        return style.getValue();
    }

    public float scaleX() {
        return (float) scaleX.getValueAsDouble();
    }

    public float scaleY() {
        return (float) scaleY.getValueAsDouble();
    }

    public float scaleZ() {
        return (float) scaleZ.getValueAsDouble();
    }

    public float positionX() {
        return (float) positionX.getValueAsDouble();
    }

    public float positionY() {
        return (float) positionY.getValueAsDouble();
    }

    public float positionZ() {
        return (float) positionZ.getValueAsDouble();
    }

    public float swingSpeed() {
        return (float) swingSpeed.getValueAsDouble();
    }

    public float swingStrength() {
        return (float) swingStrength.getValueAsDouble();
    }

    public float blockX() {
        return (float) blockX.getValueAsDouble();
    }

    public float blockY() {
        return (float) blockY.getValueAsDouble();
    }

    public float blockZ() {
        return (float) blockZ.getValueAsDouble();
    }

    public boolean itemOnly() {
        return itemOnly.isEnabled();
    }
}
