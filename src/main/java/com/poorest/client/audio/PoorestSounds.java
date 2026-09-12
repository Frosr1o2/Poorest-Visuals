package com.poorest.client.audio;

import com.poorest.client.PoorestClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Client UI/gameplay feedback sounds. Put matching .ogg files in assets/poorest_client/sounds/. */
public final class PoorestSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PoorestClient.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BELL = register("bell");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUBBLE = register("bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> BONK = register("bonk");
    public static final DeferredHolder<SoundEvent, SoundEvent> GET = register("get");
    public static final DeferredHolder<SoundEvent, SoundEvent> MAGIC_POK = register("magic_pok");
    public static final DeferredHolder<SoundEvent, SoundEvent> OFF = register("off");
    public static final DeferredHolder<SoundEvent, SoundEvent> ON = register("on");
    public static final DeferredHolder<SoundEvent, SoundEvent> POK = register("pok");
    public static final DeferredHolder<SoundEvent, SoundEvent> CUSTOM = register("custom");

    private PoorestSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(PoorestClient.MOD_ID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void play(String name, float volume, float pitch) {
        SoundEvent event = switch (name.toLowerCase(java.util.Locale.ROOT)) {
            case "bell" -> BELL.get();
            case "bubble" -> BUBBLE.get();
            case "bonk" -> BONK.get();
            case "get" -> GET.get();
            case "magic_pok" -> MAGIC_POK.get();
            case "off" -> OFF.get();
            case "on" -> ON.get();
            case "pok" -> POK.get();
            case "custom" -> CUSTOM.get();
            default -> BELL.get();
        };
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
        }
    }
}
