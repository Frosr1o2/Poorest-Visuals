package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import net.minecraft.client.Minecraft;

/** Client-side entity shadow visibility control. */
public final class EntityShadowsModule extends Module {
    private final BooleanSetting showShadows = addSetting(new BooleanSetting(
            "Shadows", "Show entity shadows while enabled.", true
    ));

    private boolean previous;
    private boolean captured;

    public EntityShadowsModule() {
        super("Entity Shadows", "Control vanilla entity shadow rendering.", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        captureAndApply();
    }

    @Override
    public void onTick() {
        captureAndApply();
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (captured) mc.options.entityShadows().set(previous);
        captured = false;
    }

    private void captureAndApply() {
        Minecraft mc = Minecraft.getInstance();
        if (!captured) {
            previous = mc.options.entityShadows().get();
            captured = true;
        }
        mc.options.entityShadows().set(showShadows.isEnabled());
    }
}
