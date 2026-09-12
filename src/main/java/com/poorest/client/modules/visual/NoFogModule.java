package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class NoFogModule extends Module {
    public NoFogModule() {
        super("No Fog", "Removes most distance fog from the client view.", ModuleCategory.WORLD);
    }
}
