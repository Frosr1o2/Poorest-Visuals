package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.NumberSetting;

/**
 * Client-side dropped-item physics renderer.
 * The actual render transform is applied by ItemEntityRendererMixin.
 */
public final class ItemPhysicsModule extends Module {
    private final NumberSetting tumble = addSetting(new NumberSetting(
            "Tumble",
            "How strongly dropped items tumble while falling.",
            1.0D,
            0.0D,
            2.0D,
            0.05D
    ));

    private final NumberSetting bounce = addSetting(new NumberSetting(
            "Bounce",
            "How much the item reacts to vertical movement.",
            0.65D,
            0.0D,
            1.5D,
            0.05D
    ));

    private final BooleanSetting flatOnGround = addSetting(new BooleanSetting(
            "Flat On Ground",
            "Makes dropped items rest naturally instead of standing and spinning.",
            true
    ));

    public ItemPhysicsModule() {
        super(
                "Item Physics",
                "Makes dropped items fall, tumble and rest more naturally.",
                ModuleCategory.VISUAL
        );
    }

    public double tumble() {
        return tumble.getValueAsDouble();
    }

    public double bounce() {
        return bounce.getValueAsDouble();
    }

    public boolean flatOnGround() {
        return flatOnGround.isEnabled();
    }
}
