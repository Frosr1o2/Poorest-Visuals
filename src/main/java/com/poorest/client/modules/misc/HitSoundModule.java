package com.poorest.client.modules.misc;

import com.poorest.client.audio.PoorestSounds;
import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.BooleanSetting;

/** Client-side hit feedback. It can be restricted to real critical hits. */
public final class HitSoundModule extends Module {
    private final ModeSetting trigger = addSetting(new ModeSetting(
            "Trigger",
            "Play on every entity hit or only on critical hits.",
            "Critical",
            "Critical", "Any Hit"
    ));
    private final ModeSetting sound = addSetting(new ModeSetting(
            "Sound", "Sound used for hit feedback.", "bell",
            "bell", "bubble", "bonk", "get", "magic_pok", "off", "on", "pok", "custom"
    ));
    private final NumberSetting volume = addSetting(new NumberSetting(
            "Volume", "Hit sound volume.", 0.85, 0.05, 1.5, 0.05
    ));
    private final NumberSetting pitch = addSetting(new NumberSetting(
            "Pitch", "Hit sound pitch.", 1.0, 0.5, 1.5, 0.05
    ));
    private final BooleanSetting muteVanillaCritical = addSetting(new BooleanSetting(
            "Mute Vanilla Crit", "Disable Minecraft's original critical-hit sound.", false
    ));

    public HitSoundModule() {
        super("Hit Sound", "Client-side hit feedback with an optional critical-only trigger.", ModuleCategory.MISC);
    }

    public boolean muteVanillaCritical() {
        return muteVanillaCritical.isEnabled();
    }

    public boolean isCriticalOnly() {
        return trigger.is("Critical");
    }

    public void playForHit() {
        if (!isEnabled() || isCriticalOnly()) return;
        play();
    }

    public void playForCritical() {
        if (!isEnabled() || !isCriticalOnly()) return;
        play();
    }

    private void play() {
        if (sound.is("off")) return;
        PoorestSounds.play(sound.getValue(), (float) volume.getValueAsDouble(), (float) pitch.getValueAsDouble());
    }
}
