package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.BooleanSetting;

public final class ZoomModule extends Module {
    private final NumberSetting zoomFov = addSetting(new NumberSetting(
            "Zoom FOV", "FOV used while zooming.", 25.0, 10.0, 60.0, 1.0
    ));
    private final BooleanSetting smooth = addSetting(new BooleanSetting(
            "Smooth", "Smoothly transitions into and out of zoom.", true
    ));

    private float current = 1.0F;

    public ZoomModule() {
        super("Zoom", "OptiFine-style smooth camera zoom.", ModuleCategory.MOVEMENT);
    }

    public void tickZoom(boolean keyDown) {
        float target = keyDown ? 0.28F : 1.0F;
        float speed = smooth.isEnabled() ? 0.22F : 1.0F;
        current += (target - current) * speed;
        if (Math.abs(target - current) < 0.002F) current = target;
    }

    public boolean isZooming() {
        return current < 0.999F;
    }

    public float getScale() {
        return current;
    }

    public float getZoomFov() {
        return (float) zoomFov.getValueAsDouble();
    }
}
