package com.poorest.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.modules.visual.ItemPhysicsModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla dropped-item spin with a small client-side physics
 * presentation. The entity's real Minecraft movement is untouched.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {
    @Shadow
    @Final
    private ItemRenderer itemRenderer;

    @Shadow
    @Final
    private RandomSource random;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void poorest$renderPhysics(
            ItemEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        ItemPhysicsModule module = (ItemPhysicsModule) ModuleManager.getModule("Item Physics");
        if (module == null || !module.isEnabled()) {
            return;
        }

        if (entity.getItem().isEmpty()) {
            return;
        }

        double dx = entity.getDeltaMovement().x;
        double dy = entity.getDeltaMovement().y;
        double dz = entity.getDeltaMovement().z;
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);
        double speed = Math.sqrt(dx * dx + dy * dy + dz * dz);

        float age = entity.getAge() + partialTick;
        float baseRotation = (float) ((entity.getId() * 37L) % 360L);

        poseStack.pushPose();

        // Start from a horizontal "resting" orientation. This removes the
        // characteristic upright vanilla spin when the item is sitting still.
        if (module.flatOnGround() && entity.onGround()) {
            poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90.0D)));
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(baseRotation)));

            float slide = (float) Math.min(1.0D, horizontalSpeed * 8.0D);
            if (slide > 0.0F) {
                float sideAngle = (float) Math.toDegrees(Math.atan2(dz, dx));
                poseStack.mulPose(new Quaternionf().rotationZ(
                        (float) Math.toRadians(sideAngle) * slide * 0.22F
                ));
            }
        } else {
            // In the air, use velocity to create a tumble rather than a
            // perfectly uniform turntable rotation.
            float fallTilt = (float) Math.toDegrees(Math.atan2(dy, Math.max(0.08D, horizontalSpeed)));
            float travelYaw = horizontalSpeed > 0.01D
                    ? (float) Math.toDegrees(Math.atan2(dz, dx))
                    : baseRotation;

            float tumble = (float) (module.tumble() * (25.0D + speed * 180.0D) * (age * 0.045F));
            float wobble = (float) (Math.sin(age * 0.19F + entity.getId()) * (8.0F + speed * 35.0D));

            poseStack.mulPose(new Quaternionf().rotationX((float) Math.toRadians(90.0D + fallTilt * 0.35D)));
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(travelYaw)));
            poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(tumble + wobble)));

            float squash = 1.0F + (float) Math.min(0.035D, Math.abs(dy) * module.bounce() * 0.12D);
            poseStack.scale(1.0F / squash, squash, 1.0F / squash);
        }

        // Vanilla seeds this RNG from the entity before rendering the stack.
        // Without doing the same here, stacks with 2+ items get a different random
        // offset every frame and visibly jitter/flicker.
        random.setSeed(entity.getId());

        // Use the same vanilla stack renderer, but with our transform.
        ItemEntityRenderer.renderMultipleFromCount(
                itemRenderer,
                poseStack,
                buffer,
                packedLight,
                entity.getItem(),
                random,
                entity.level()
        );

        poseStack.popPose();
        ci.cancel();
    }
}
