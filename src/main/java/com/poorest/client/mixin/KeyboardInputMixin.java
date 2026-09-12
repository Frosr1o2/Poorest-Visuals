package com.poorest.client.mixin;

import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.modules.misc.InvMoveModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps normal movement while inventory/container screens are open.
 *
 * Shift/sneak is completely disabled while inventory/container screens are open.
 * Sprint remains available through Minecraft's normal Sprint key.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void poorest$applyInvMove(boolean slowDown, float slowDownFactor, CallbackInfo ci) {
        InvMoveModule module = (InvMoveModule) ModuleManager.getModule("InvMove");
        Minecraft mc = Minecraft.getInstance();

        if (module == null || !module.isEnabled() || mc.screen == null ||
                !(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>)) {
            return;
        }

        long window = mc.getWindow().getWindow();
        net.minecraft.client.player.Input input = (net.minecraft.client.player.Input) (Object) this;

        boolean left = isPressed(window, mc.options.keyLeft);
        boolean right = isPressed(window, mc.options.keyRight);
        boolean forward = isPressed(window, mc.options.keyUp);
        boolean back = isPressed(window, mc.options.keyDown);
        boolean jump = isPressed(window, mc.options.keyJump);
        input.leftImpulse = left ? 1.0F : right ? -1.0F : 0.0F;
        input.forwardImpulse = forward ? 1.0F : back ? -1.0F : 0.0F;
        input.down = back;
        input.left = left;
        input.right = right;
        input.up = forward;
        input.jumping = jump;

        // Shift/sneak is completely disabled while an inventory/container is open.
        // Do not read or forward the Shift key here at all. Sprint is intentionally
        // left to Minecraft's normal Sprint key handling.
        input.shiftKeyDown = false;
    }

    private static boolean isPressed(long window, net.minecraft.client.KeyMapping mapping) {
        int key = mapping.getKey().getValue();
        return key >= 0 && GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }
}
