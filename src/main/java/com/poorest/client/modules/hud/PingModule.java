package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

public final class PingModule extends Module {

    public PingModule() {
        super(
                "Ping",
                "Displays your current network latency.",
                ModuleCategory.HUD
        );

        setEnabled(true);
    }
}
