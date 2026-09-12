package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;

/** Removes selected negative visual effects locally while enabled. */
public final class NoDebuffsModule extends Module {

    private final BooleanSetting blindness = addSetting(new BooleanSetting(
            "Blindness", "Removes the blindness effect.", true
    ));

    private final BooleanSetting darkness = addSetting(new BooleanSetting(
            "Darkness", "Removes the darkness effect.", true
    ));

    private final BooleanSetting nausea = addSetting(new BooleanSetting(
            "Nausea", "Removes the nausea effect.", true
    ));

    public NoDebuffsModule() {
        super(
                "No Debuffs",
                "Removes annoying visual debuffs from your local client view.",
                ModuleCategory.PLAYER
        );
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (blindness.isEnabled()) {
            mc.player.removeEffect(MobEffects.BLINDNESS);
        }
        if (darkness.isEnabled()) {
            mc.player.removeEffect(MobEffects.DARKNESS);
        }
        if (nausea.isEnabled()) {
            mc.player.removeEffect(MobEffects.CONFUSION);
        }
    }
}
