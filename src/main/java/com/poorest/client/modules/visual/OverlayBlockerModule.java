package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

/**
 * Client-side GUI layer blocker used by the visual category.
 * The actual cancellation is performed by PoorestClient's render hook.
 */
public final class OverlayBlockerModule extends Module {

    private final String layerPath;

    public OverlayBlockerModule(String name, String description, String layerPath) {
        super(name, description, ModuleCategory.VISUAL);
        this.layerPath = layerPath;
    }

    public String getLayerPath() {
        return layerPath;
    }
}
