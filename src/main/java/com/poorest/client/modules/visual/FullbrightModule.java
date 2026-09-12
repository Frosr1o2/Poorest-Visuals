package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;

/**
 * True client-side Fullbright.
 *
 * The lightmap calculation is overridden by a client mixin while this module
 * is enabled. No potion effect, gamma value, or player state is modified, so
 * there is nothing to flicker and disabling the module immediately restores
 * vanilla lighting.
 */
public final class FullbrightModule extends Module {
    public FullbrightModule() {
        super(
                "Fullbright",
                "Makes dark areas fully visible without a potion effect.",
                ModuleCategory.VISUAL
        );
    }
}
