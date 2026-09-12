package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import net.minecraft.client.Minecraft;

/** Completely disables vanilla view bobbing while enabled. */
public final class NoViewBobbingModule extends Module {

    private boolean previous;
    private boolean captured;

    public NoViewBobbingModule() {
        super(
                "No View Bobbing",
                "Removes the camera bobbing animation while moving.",
                ModuleCategory.MOVEMENT
        );
    }

    @Override
    protected void onEnable() {
        captureAndDisable();
    }

    @Override
    public void onTick() {
        captureAndDisable();
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (captured) {
            mc.options.bobView().set(previous);
        }
        captured = false;
    }

    private void captureAndDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (!captured) {
            previous = mc.options.bobView().get();
            captured = true;
        }
        mc.options.bobView().set(false);
    }
}
