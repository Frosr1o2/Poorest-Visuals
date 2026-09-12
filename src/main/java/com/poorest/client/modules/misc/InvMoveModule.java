package com.poorest.client.modules.misc;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

/**
 * Allows normal movement while inventory/container screens are open.
 * The actual movement input is applied by KeyboardInputMixin at the end of
 * KeyboardInput.tick(), which is the reliable point for NeoForge 1.21.1.
 */
public final class InvMoveModule extends Module {
    public InvMoveModule() {
        super("InvMove", "Move while inventory and container screens are open.", ModuleCategory.MISC);
    }
}
