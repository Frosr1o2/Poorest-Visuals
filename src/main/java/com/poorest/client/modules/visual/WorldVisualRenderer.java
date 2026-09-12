package com.poorest.client.modules.visual;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.poorest.client.ui.PoorestTheme;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class WorldVisualRenderer {
    private WorldVisualRenderer() {}

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.player.isRemoved()) return;

        var circles = module("Jump Circles", JumpCirclesModule.class);
        var trail = module("Trail Effects", TrailEffectsModule.class);
        var hitboxes = module("Hitboxes", HitboxesModule.class);
        var chinaHat = module("China Hat", ChinaHatModule.class);
        if ((circles == null || !circles.isEnabled()) && (trail == null || !trail.isEnabled()) && (hitboxes == null || !hitboxes.isEnabled()) && (chinaHat == null || !chinaHat.isEnabled())) return;

        PoseStack pose = event.getPoseStack();
        if (pose == null) return;

        Vec3 camera = event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (circles != null && circles.isEnabled()) renderCircles(pose, circles);
        if (trail != null && trail.isEnabled()) renderTrail(pose, trail);
        if (chinaHat != null && chinaHat.isEnabled()) renderChinaHat(pose, mc, event, chinaHat);

        // Hitboxes are a normal world-space debug visualization.
        // Keep depth testing enabled so blocks occlude them instead of
        // drawing the boxes through walls.
        if (hitboxes != null && hitboxes.isEnabled()) {
            RenderSystem.enableDepthTest();
            renderHitboxes(pose, mc, event, hitboxes);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private static void renderCircles(PoseStack pose, JumpCirclesModule module) {
        if (module.circles().isEmpty()) return;
        long now = System.currentTimeMillis();
        int[] rgb = effectColor(module.color());
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (var circle : module.circles()) {
            float progress = Math.max(0.0F, Math.min(1.0F, (now - circle.createdAt()) / module.duration()));
            float radius = module.radius() * progress;
            int alpha = (int) ((1.0F - progress) * 230.0F);
            for (int i = 0; i <= 48; i++) {
                double angle = Math.PI * 2.0D * i / 48.0D;
                float x = (float) (circle.x() + Math.cos(angle) * radius);
                float z = (float) (circle.z() + Math.sin(angle) * radius);
                buffer.addVertex(pose.last().pose(), x, (float) circle.y(), z).setColor(rgb[0], rgb[1], rgb[2], alpha);
            }
        }
        draw(buffer);
    }

    private static void renderTrail(PoseStack pose, TrailEffectsModule module) {
        if (module.points().size() < 2) return;
        long now = System.currentTimeMillis();
        int[] rgb = effectColor(module.color());
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        boolean first = true;
        for (var point : module.points()) {
            float progress = Math.max(0.0F, Math.min(1.0F, (now - point.createdAt()) / module.duration()));
            int alpha = (int) ((1.0F - progress) * 220.0F);
            if (first) first = false;
            buffer.addVertex(pose.last().pose(), (float) point.x(), (float) point.y(), (float) point.z())
                    .setColor(rgb[0], rgb[1], rgb[2], alpha);
        }
        draw(buffer);
    }



    private static int[] effectColor(String value) {
        if ("Accent".equalsIgnoreCase(value)) {
            int c = PoorestTheme.accent();
            return new int[]{(c >> 16) & 255, (c >> 8) & 255, c & 255};
        }
        return switch (value) {
            case "Blue" -> new int[]{94, 140, 255};
            case "Cyan" -> new int[]{74, 198, 255};
            case "Pink" -> new int[]{255, 98, 181};
            case "Green" -> new int[]{86, 217, 138};
            case "Orange" -> new int[]{255, 179, 94};
            case "Red" -> new int[]{255, 98, 98};
            case "White" -> new int[]{255, 255, 255};
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 5000L) / 5000.0F;
                int rgb = java.awt.Color.HSBtoRGB(hue, 0.75F, 1.0F);
                yield new int[]{(rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255};
            }
            default -> new int[]{155, 108, 255};
        };
    }

    private static void renderChinaHat(PoseStack pose, Minecraft mc, RenderLevelStageEvent event, ChinaHatModule module) {
        if (mc.player == null || mc.player.isInvisible() || !mc.player.isAlive()) return;

        // Keep the hat anchored to the player's authoritative entity position.
        // Using a second interpolation path here can produce a visible vertical
        // micro-jitter because the world renderer already has its own frame
        // interpolation. The player position is stable between ticks, while the
        // hat still follows normal movement/jumping immediately.
        double x = mc.player.getX();
        double y = mc.player.getY() + mc.player.getBbHeight() + 0.035D;
        double z = mc.player.getZ();
        int[] rgb = chinaHatColor(module.color());
        float a = module.alpha();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        int segments = 48;
        float radius = module.radius();
        float height = module.height();
        float apexY = (float) (y + height);
        float centerY = (float) y;
        float centerX = (float) x;
        float centerZ = (float) z;

        for (int i = 0; i < segments; i++) {
            double a0 = Math.PI * 2.0D * i / segments;
            double a1 = Math.PI * 2.0D * (i + 1) / segments;
            float x0 = (float) (x + Math.cos(a0) * radius);
            float z0 = (float) (z + Math.sin(a0) * radius);
            float x1 = (float) (x + Math.cos(a1) * radius);
            float z1 = (float) (z + Math.sin(a1) * radius);

            buffer.addVertex(pose.last().pose(), x0, centerY, z0).setColor(rgb[0], rgb[1], rgb[2], (int) (a * 255.0F));
            buffer.addVertex(pose.last().pose(), x1, centerY, z1).setColor(rgb[0], rgb[1], rgb[2], (int) (a * 255.0F));
            buffer.addVertex(pose.last().pose(), centerX, apexY, centerZ).setColor(rgb[0], rgb[1], rgb[2], (int) (a * 255.0F));
        }
        draw(buffer);

        if (module.outline()) {
            BufferBuilder outline = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int i = 0; i <= segments; i++) {
                double angle = Math.PI * 2.0D * i / segments;
                outline.addVertex(pose.last().pose(), (float) (x + Math.cos(angle) * radius), centerY, (float) (z + Math.sin(angle) * radius))
                        .setColor(rgb[0], rgb[1], rgb[2], 230);
            }
            outline.addVertex(pose.last().pose(), centerX, apexY, centerZ).setColor(rgb[0], rgb[1], rgb[2], 180);
            draw(outline);
        }
    }

    private static int[] chinaHatColor(String value) {
        return switch (value) {
            case "Cyan" -> new int[]{80, 220, 255};
            case "Pink" -> new int[]{255, 105, 190};
            case "White" -> new int[]{255, 255, 255};
            case "Green" -> new int[]{100, 235, 150};
            case "Rainbow" -> {
                float hue = (System.currentTimeMillis() % 5000L) / 5000.0F;
                int rgb = java.awt.Color.HSBtoRGB(hue, 0.75F, 1.0F);
                yield new int[]{(rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255};
            }
            default -> new int[]{170, 95, 255};
        };
    }

    private static void renderHitboxes(PoseStack pose, Minecraft mc, RenderLevelStageEvent event, HitboxesModule module) {
        // During death, respawn and world transitions the client can still
        // receive a render-stage callback while the local player/entity list
        // is in the process of being rebuilt. Do not touch the renderer in
        // that state.
        if (mc.player == null || mc.player.isRemoved() || !mc.player.isAlive()) return;

        Vec3 camera = event.getCamera().getPosition();
        float maxDistance = module.range();
        double maxSq = maxDistance * maxDistance;
        int[] color = color(module.color());
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        boolean hasVertices = false;

        AABB searchBox = new AABB(
                camera.x - maxDistance, camera.y - maxDistance, camera.z - maxDistance,
                camera.x + maxDistance, camera.y + maxDistance, camera.z + maxDistance
        );

        for (Entity entity : mc.level.getEntities(mc.player, searchBox, e -> e.isAlive() && !e.isSpectator() && e != mc.player)) {
            // Never let the debug overlay reveal invisible entities.
            // This keeps Hitboxes consistent with normal client rendering.
            if (entity.isInvisible()) continue;
            if (entity.distanceToSqr(camera.x, camera.y, camera.z) > maxSq) continue;

            AABB box = entity.getBoundingBox();
            hasVertices = true;
            float minX = (float) box.minX;
            float minY = (float) box.minY;
            float minZ = (float) box.minZ;
            float maxX = (float) box.maxX;
            float maxY = (float) box.maxY;
            float maxZ = (float) box.maxZ;

            edge(buffer, pose, minX, minY, minZ, maxX, minY, minZ, color);
            edge(buffer, pose, minX, minY, minZ, minX, minY, maxZ, color);
            edge(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, color);
            edge(buffer, pose, minX, minY, maxZ, maxX, minY, maxZ, color);
            edge(buffer, pose, minX, maxY, minZ, maxX, maxY, minZ, color);
            edge(buffer, pose, minX, maxY, minZ, minX, maxY, maxZ, color);
            edge(buffer, pose, maxX, maxY, minZ, maxX, maxY, maxZ, color);
            edge(buffer, pose, minX, maxY, maxZ, maxX, maxY, maxZ, color);
            edge(buffer, pose, minX, minY, minZ, minX, maxY, minZ, color);
            edge(buffer, pose, maxX, minY, minZ, maxX, maxY, minZ, color);
            edge(buffer, pose, maxX, minY, maxZ, maxX, maxY, maxZ, color);
            edge(buffer, pose, minX, minY, maxZ, minX, maxY, maxZ, color);
        }

        // BufferBuilder.buildOrThrow() throws when nothing was submitted.
        // This can happen during death/respawn/world transitions.
        if (hasVertices) {
            draw(buffer);
        }
    }

    private static void edge(BufferBuilder buffer, PoseStack pose, float x1, float y1, float z1, float x2, float y2, float z2, int[] color) {
        buffer.addVertex(pose.last().pose(), x1, y1, z1).setColor(color[0], color[1], color[2], 220);
        buffer.addVertex(pose.last().pose(), x2, y2, z2).setColor(color[0], color[1], color[2], 220);
    }

    private static int[] color(String value) {
        return switch (value) {
            case "Red" -> new int[]{255, 90, 100};
            case "Green" -> new int[]{90, 235, 140};
            case "Cyan" -> new int[]{80, 220, 255};
            case "Yellow" -> new int[]{255, 220, 80};
            case "White" -> new int[]{255, 255, 255};
            default -> new int[]{155, 108, 255};
        };
    }

    private static void draw(BufferBuilder buffer) {
        var mesh = buffer.buildOrThrow();
        BufferUploader.drawWithShader(mesh);
        mesh.close();
    }

    private static <T> T module(String name, Class<T> type) {
        var module = com.poorest.client.core.module.ModuleManager.getModule(name);
        return type.isInstance(module) ? type.cast(module) : null;
    }
}
