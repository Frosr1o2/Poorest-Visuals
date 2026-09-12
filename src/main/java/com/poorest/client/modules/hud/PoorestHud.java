package com.poorest.client.modules.hud;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.ui.PoorestTheme;
import com.poorest.client.ui.font.PoorestFont;
import com.poorest.client.ui.render.GLRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PoorestHud {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static boolean rendering;
    private static final Map<String, Float> ELEMENT_ANIMATION = new LinkedHashMap<>();

    private static LivingEntity lastTarget;
    private static long lastTargetSeenAt;
    private static float targetHudAnimation;
    private static GuiGraphics guiGraphics;

    private PoorestHud() {
    }

    public static void render() {
        if (guiGraphics != null) render(guiGraphics);
    }

    public static void render(GuiGraphics graphics) {
        if (rendering) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        guiGraphics = graphics;
        rendering = true;
        try {
            float width = mc.getWindow().getWidth();
            float height = mc.getWindow().getHeight();
            PoorestHudLayout.tick();
            GLRenderer.begin(width, height);

            if (enabled("Hit Color") && mc.player.hurtTime > 0) {
                var hit = ModuleManager.getModule("Hit Color");
                if (hit instanceof com.poorest.client.modules.visual.HitColorModule hitColor) {
                    float progress = Math.max(0.0F, Math.min(1.0F, mc.player.hurtTime / Math.max(1.0F, (float) hitColor.getDuration())));
                    int alpha = (int) (255.0F * hitColor.getIntensity() * progress * 0.55F);
                    int overlay = (Math.max(0, Math.min(255, alpha)) << 24) | 0xFF4A66;
                    GLRenderer.roundedRect(0, 0, width, height, 0, overlay, width, height);
                }
            }

            drawElement("Watermark", "PV", "POOREST", "VISUALS");
            drawElement("FPS", "F", "FPS", Integer.toString(mc.getFps()));
            drawElement("Coordinates", "X", "XYZ", coords(mc));
            drawElement("Ping", "P", "PING", ping(mc) + " ms");
            drawElement("Clock", "T", "TIME", LocalTime.now().format(TIME));
            drawElement("Direction", "D", "DIR", direction(normalize(mc.player.getYRot())));
            drawElement("Biome", "B", "BIOME", biome(mc));

            if (enabled("Keystrokes")) drawKeystrokes(mc, width, height);
            if (enabled("CPS")) drawCps(width, height);
            if (enabled("Target HUD")) drawTargetHud(mc, width, height);
            if (enabled("Armor HUD")) drawArmorHud(mc, width, height);
            if (enabled("Potion HUD")) drawPotionHud(mc, width, height);
            if (enabled("Arraylist")) drawArraylist(width, height);

            if (mc.screen == null && (enabled("Crosshair") || enabled("Clean Crosshair") || enabled("Crosshair+"))) {
                drawCrosshair(width, height);
            }

            PoorestHudLayout.renderEditorOverlay(width, height);
        } finally {
            GLRenderer.end();
            try {
                drawArmorItems(mc);
            } finally {
                guiGraphics = null;
                rendering = false;
            }
        }
    }

    private static String coords(Minecraft mc) {
        return (int) Math.floor(mc.player.getX()) + " " +
                (int) Math.floor(mc.player.getY()) + " " +
                (int) Math.floor(mc.player.getZ());
    }

    private static int ping(Minecraft mc) {
        if (mc.getConnection() == null) return 0;
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? 0 : Math.max(0, info.getLatency());
    }

    private static String biome(Minecraft mc) {
        String biome = mc.player.level().getBiome(mc.player.blockPosition())
                .unwrapKey().map(key -> key.location().getPath()).orElse("unknown").replace('_', ' ');
        return biome.length() > 18 ? biome.substring(0, 15) + "..." : biome;
    }

    private static void drawElement(String id, String icon, String label, String value) {
        float progress = hudAnimation(id, enabled(id));
        if (progress < 0.01F) return;

        float scale = PoorestHudLayout.scale(id) * (0.97F + progress * 0.03F);
        float slide = (1.0F - progress) * 10.0F;
        panel(PoorestHudLayout.x(id), PoorestHudLayout.y(id) + slide,
                PoorestHudLayout.width(id) * scale, 32, icon, label, value, progress);
    }

    private static void drawKeystrokes(Minecraft mc, float screenWidth, float screenHeight) {
        float animation = hudAnimation("Keystrokes", enabled("Keystrokes"));
        if (animation < 0.01F) return;

        long window = mc.getWindow().getWindow();
        float x = PoorestHudLayout.x("Keystrokes");
        float y = PoorestHudLayout.y("Keystrokes") + (1.0F - animation) * 12.0F;
        float s = PoorestHudLayout.scale("Keystrokes") * (0.94F + animation * 0.06F);
        float key = 42 * s;
        float gap = 5 * s;
        int up = GLFW.glfwGetKey(window, mc.options.keyUp.getKey().getValue()) == GLFW.GLFW_PRESS ? PoorestTheme.cardEnabled() : PoorestTheme.panel();
        int down = GLFW.glfwGetKey(window, mc.options.keyDown.getKey().getValue()) == GLFW.GLFW_PRESS ? PoorestTheme.cardEnabled() : PoorestTheme.panel();
        int left = GLFW.glfwGetKey(window, mc.options.keyLeft.getKey().getValue()) == GLFW.GLFW_PRESS ? PoorestTheme.cardEnabled() : PoorestTheme.panel();
        int right = GLFW.glfwGetKey(window, mc.options.keyRight.getKey().getValue()) == GLFW.GLFW_PRESS ? PoorestTheme.cardEnabled() : PoorestTheme.panel();
        int jump = GLFW.glfwGetKey(window, mc.options.keyJump.getKey().getValue()) == GLFW.GLFW_PRESS ? PoorestTheme.cardEnabled() : PoorestTheme.panel();
        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        keyButton(x + key + gap, y, key, "W", withAlpha(up, animation), screenWidth, screenHeight);
        keyButton(x, y + key + gap, key, "A", withAlpha(left, animation), screenWidth, screenHeight);
        keyButton(x + key + gap, y + key + gap, key, "S", withAlpha(down, animation), screenWidth, screenHeight);
        keyButton(x + 2 * (key + gap), y + key + gap, key, "D", withAlpha(right, animation), screenWidth, screenHeight);
        keyButton(x, y + 2 * (key + gap), key * 1.45F, "LMB", withAlpha(lmb ? PoorestTheme.cardEnabled() : PoorestTheme.panel(), animation), screenWidth, screenHeight);
        keyButton(x + key * 1.45F + gap, y + 2 * (key + gap), key * 1.45F, "RMB", withAlpha(rmb ? PoorestTheme.cardEnabled() : PoorestTheme.panel(), animation), screenWidth, screenHeight);
        keyButton(x, y + 3 * (key + gap), key * 2.95F, "SPACE", withAlpha(jump, animation), screenWidth, screenHeight);
    }

    private static void keyButton(float x, float y, float w, String text, int color, float sw, float sh) {
        GLRenderer.roundedRect(x, y, w, 32, 8, color, sw, sh);
        GLRenderer.outline(x, y, w, 32, 8, 1, withAlpha(PoorestTheme.border(), colorAlpha(color)), sw, sh);
        float tw = hudWidth(text, 11);
        hudString(text, x + (w - tw) * 0.5F, y + 9, 11, withAlpha(PoorestTheme.text(), colorAlpha(color)), sw, sh);
    }

    private static void drawCps(float sw, float sh) {
        String value = "LMB " + InputTracker.getLeftCps() + "   RMB " + InputTracker.getRightCps();
        drawElement("CPS", "C", "CPS", value);
    }

    private static void drawTargetHud(Minecraft mc, float sw, float sh) {
        TargetHudModule module = module("Target HUD", TargetHudModule.class);
        if (module == null || !module.isEnabled()) return;

        LivingEntity current = mc.crosshairPickEntity instanceof LivingEntity living &&
                living.isAlive() && !living.isInvisible() ? living : null;

        long now = System.currentTimeMillis();
        if (current != null) {
            if (current != lastTarget) {
                targetHudAnimation = 0.0F;
            }
            lastTarget = current;
            lastTargetSeenAt = now;
        }

        if (lastTarget == null) return;

        long age = now - lastTargetSeenAt;
        boolean withinHold = age <= module.holdTimeMillis();
        boolean usable = lastTarget.isAlive() && !lastTarget.isRemoved() && !lastTarget.isInvisible();
        if (!withinHold && !usable) {
            lastTarget = null;
            targetHudAnimation = 0.0F;
            return;
        }

        float target = withinHold ? 1.0F : 0.0F;
        targetHudAnimation += (target - targetHudAnimation) * module.animationSpeed();
        if (targetHudAnimation < 0.01F) {
            if (!withinHold) lastTarget = null;
            return;
        }

        float eased = targetHudAnimation * targetHudAnimation * (3.0F - 2.0F * targetHudAnimation);
        float x = PoorestHudLayout.x("Target HUD");
        float baseY = PoorestHudLayout.y("Target HUD");
        float w = PoorestHudLayout.width("Target HUD") * PoorestHudLayout.scale("Target HUD");
        float y = baseY + (1.0F - eased) * 12.0F;

        panel(x, y, w, 94, "T", "TARGET", lastTarget.getDisplayName().getString(), eased);

        float hp = Math.max(0, lastTarget.getHealth());
        float max = Math.max(1, lastTarget.getMaxHealth());
        float ratio = Math.min(1, hp / max);
        GLRenderer.roundedRect(x + 10, y + 42, w - 20, 8, 4, withAlpha(PoorestTheme.surface(), eased), sw, sh);
        GLRenderer.roundedRect(x + 10, y + 42, (w - 20) * ratio, 8, 4, withAlpha(PoorestTheme.accentBright(), eased), sw, sh);
        hudString("HP " + String.format("%.1f / %.1f", hp, max), x + 10, y + 56, 10, withAlpha(PoorestTheme.secondary(), eased), sw, sh);

        int armor = 0;
        for (ItemStack stack : lastTarget.getArmorSlots()) {
            if (!stack.isEmpty()) armor++;
        }
        hudString("Armor " + armor + "/4", x + 10, y + 72, 10, withAlpha(PoorestTheme.muted(), eased), sw, sh);
    }

    private static void drawArmorHud(Minecraft mc, float sw, float sh) {
        float animation = hudAnimation("Armor HUD", enabled("Armor HUD"));
        if (animation < 0.01F) return;

        // Armor HUD keeps a stable physical size while animating. Previously the
        // animation factor was also applied to width, which made the whole panel
        // visibly "squeeze" every time it appeared/disappeared.
        float scale = PoorestHudLayout.scale("Armor HUD");
        float x = PoorestHudLayout.x("Armor HUD");
        float y = PoorestHudLayout.y("Armor HUD") + (1.0F - animation) * 10.0F;
        float w = Math.max(280.0F, PoorestHudLayout.width("Armor HUD")) * scale;
        float h = 128.0F * scale;

        GLRenderer.roundedRect(x, y, w, h, 11.0F * scale, withAlpha(PoorestTheme.panel(), animation), sw, sh);
        GLRenderer.outline(x, y, w, h, 11.0F * scale, 1.0F, withAlpha(PoorestTheme.border(), animation), sw, sh);
        hudString("ARMOR", x + 10.0F * scale, y + 7.0F * scale, 11.0F * scale,
                withAlpha(PoorestTheme.secondary(), animation), sw, sh);

        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        float cellW = (w - 20.0F * scale) / 4.0F;
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(slots[i]);
            float cellX = x + 10.0F * scale + i * cellW;
            float boxY = y + 21.0F * scale;
            float boxH = 51.0F * scale;

            GLRenderer.roundedRect(cellX + scale, boxY, cellW - 2.0F * scale, boxH,
                    7.0F * scale, withAlpha(PoorestTheme.surface(), animation), sw, sh);

            if (!stack.isEmpty()) {
                if (stack.isDamageableItem()) {
                    float ratio = durabilityRatio(stack);
                    int bar = ratio > 0.25F ? PoorestTheme.accentBright() : 0xFFE35D6A;
                    float barX = cellX + 6.0F * scale;
                    float barY = y + 75.0F * scale;
                    float barW = cellW - 12.0F * scale;
                    GLRenderer.roundedRect(barX, barY, barW, 3.0F * scale, 1.5F * scale,
                            withAlpha(PoorestTheme.surface(), animation), sw, sh);
                    GLRenderer.roundedRect(barX, barY, barW * ratio, 3.0F * scale, 1.5F * scale,
                            withAlpha(bar, animation), sw, sh);
                    String pct = Math.round(ratio * 100.0F) + "%";
                    float tw = hudWidth(pct, 8.0F * scale);
                    hudString(pct, cellX + (cellW - tw) * 0.5F, y + 82.0F * scale,
                            8.0F * scale, withAlpha(PoorestTheme.secondary(), animation), sw, sh);
                } else {
                    String infinite = "∞";
                    float tw = hudWidth(infinite, 9.0F * scale);
                    hudString(infinite, cellX + (cellW - tw) * 0.5F, y + 75.0F * scale,
                            9.0F * scale, withAlpha(PoorestTheme.muted(), animation), sw, sh);
                }
            } else {
                String empty = "—";
                float tw = hudWidth(empty, 9.0F * scale);
                hudString(empty, cellX + (cellW - tw) * 0.5F, y + 41.0F * scale,
                        9.0F * scale, withAlpha(PoorestTheme.muted(), animation), sw, sh);
            }
        }

        drawHeldItemInfo(mc, mc.player.getMainHandItem(), "MAIN", x + 10.0F * scale,
                y + 96.0F * scale, w * 0.5F - 15.0F * scale, animation, scale, sw, sh);
        drawHeldItemInfo(mc, mc.player.getOffhandItem(), "OFF", x + w * 0.5F + 5.0F * scale,
                y + 96.0F * scale, w * 0.5F - 15.0F * scale, animation, scale, sw, sh);
    }

    private static void drawHeldItemInfo(Minecraft mc, ItemStack stack, String label, float x, float y,
                                         float width, float animation, float scale, float sw, float sh) {
        String value = stack.isEmpty() ? label + " —" : label + (stack.getCount() > 1 ? " x" + stack.getCount() : "");
        hudString(value, x + 20.0F * scale, y + 1.0F * scale, 8.5F * scale,
                withAlpha(stack.isEmpty() ? PoorestTheme.muted() : PoorestTheme.text(), animation), sw, sh);

        if (!stack.isEmpty() && stack.isDamageableItem()) {
            float ratio = durabilityRatio(stack);
            int bar = ratio > 0.25F ? PoorestTheme.accentBright() : 0xFFE35D6A;
            float barY = y + 13.0F * scale;
            GLRenderer.roundedRect(x + 20.0F * scale, barY, Math.max(20.0F, width - 20.0F * scale), 3.0F * scale,
                    1.5F * scale, withAlpha(PoorestTheme.surface(), animation), sw, sh);
            GLRenderer.roundedRect(x + 20.0F * scale, barY, Math.max(0.0F, width - 20.0F * scale) * ratio, 3.0F * scale,
                    1.5F * scale, withAlpha(bar, animation), sw, sh);
        }
    }

    private static void renderHudItem(Minecraft mc, ItemStack stack, float centerX, float centerY, float itemScale) {
        if (guiGraphics == null || stack.isEmpty() || itemScale <= 0.01F) return;

        // HUD layout coordinates are stored in physical framebuffer pixels, while
        // GuiGraphics uses the scaled GUI coordinate space. Convert before drawing
        // so armor/held-item icons stay inside their cells on every GUI scale.
        double guiScale = Math.max(1.0D, mc.getWindow().getGuiScale());
        float guiX = (float) (centerX / guiScale);
        float guiY = (float) (centerY / guiScale);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(guiX, guiY, 0.0F);
        guiGraphics.pose().scale(itemScale, itemScale, 1.0F);
        guiGraphics.renderItem(stack, -8, -8);
        guiGraphics.pose().popPose();
    }

    private static void drawArmorItems(Minecraft mc) {
        if (guiGraphics == null || mc.player == null || !enabled("Armor HUD")) return;
        float animation = ELEMENT_ANIMATION.getOrDefault("Armor HUD", 0.0F);
        if (animation < 0.03F) return;

        float scale = PoorestHudLayout.scale("Armor HUD");
        float x = PoorestHudLayout.x("Armor HUD");
        float y = PoorestHudLayout.y("Armor HUD") + (1.0F - animation) * 10.0F;
        float w = Math.max(280.0F, PoorestHudLayout.width("Armor HUD")) * scale;
        float cellW = (w - 20.0F * scale) / 4.0F;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(slots[i]);
            if (stack.isEmpty()) continue;
            float centerX = x + 10.0F * scale + i * cellW + cellW * 0.5F;
            float centerY = y + 46.0F * scale;
            renderHudItem(mc, stack, centerX, centerY, scale);
        }

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        if (!main.isEmpty()) renderHudItem(mc, main, x + 18.0F * scale, y + 112.0F * scale, scale);
        if (!off.isEmpty()) renderHudItem(mc, off, x + w * 0.5F + 13.0F * scale, y + 112.0F * scale, scale);
    }

    private static float durabilityRatio(ItemStack stack) {
        return Math.max(0.0F, Math.min(1.0F,
                1.0F - (float) stack.getDamageValue() / Math.max(1, stack.getMaxDamage())));
    }

    private static void drawPotionHud(Minecraft mc, float sw, float sh) {
        List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
        float animation = hudAnimation("Potion HUD", !effects.isEmpty() && enabled("Potion HUD"));
        if (animation < 0.01F) return;

        float scale = PoorestHudLayout.scale("Potion HUD") * (0.96F + animation * 0.04F);
        float x = PoorestHudLayout.x("Potion HUD");
        float y = PoorestHudLayout.y("Potion HUD") + (1.0F - animation) * 12.0F;
        float w = PoorestHudLayout.width("Potion HUD") * scale;
        float h = 26 + effects.size() * 20;
        GLRenderer.roundedRect(x, y, w, h, 11, withAlpha(PoorestTheme.panel(), animation), sw, sh);
        GLRenderer.outline(x, y, w, h, 11, 1, withAlpha(PoorestTheme.border(), animation), sw, sh);
        hudString("EFFECTS", x + 10, y + 7, 11, withAlpha(PoorestTheme.secondary(), animation), sw, sh);
        int i = 0;
        for (MobEffectInstance effect : effects) {
            String name = effect.getEffect().value().getDisplayName().getString();
            String duration = formatEffectTime(effect.getDuration());
            String text = name + " " + duration;
            if (text.length() > 38) text = text.substring(0, 35) + "...";
            hudString(text, x + 10, y + 25 + i * 20, 10, withAlpha(PoorestTheme.text(), animation), sw, sh);
            i++;
        }
    }

    private static String formatEffectTime(int ticks) {
        if (ticks == MobEffectInstance.INFINITE_DURATION) return "∞";
        int totalSeconds = Math.max(0, ticks / 20);
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    private static void drawArraylist(float sw, float sh) {
        List<Module> modules = ModuleManager.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingDouble((Module m) -> -hudWidth(m.getName(), 9)))
                .toList();
        float animation = hudAnimation("Arraylist", !modules.isEmpty() && enabled("Arraylist"));
        if (animation < 0.01F) return;

        float y = PoorestHudLayout.y("Arraylist") + (1.0F - animation) * 14.0F;
        float right = sw - 10;
        int index = 0;
        for (Module module : modules) {
            String name = module.getName();
            float tw = hudWidth(name, 11);
            float x = right - tw - 10;
            int color = index % 2 == 1 ? PoorestTheme.secondary() : PoorestTheme.accentBright();
            int alphaColor = withAlpha(color, animation);
            GLRenderer.roundedRect(x - 7, y, tw + 14, 20, 7, withAlpha(PoorestTheme.panel(), animation), sw, sh);
            hudString(name, x, y + 4, 11, alphaColor, sw, sh);
            y += 23;
            index++;
            if (y > sh - 24) break;
        }
    }

    private static float hudAnimation(String id, boolean active) {
        float target = active ? 1.0F : 0.0F;
        float progress = ELEMENT_ANIMATION.getOrDefault(id, 0.0F);
        progress += (target - progress) * 0.18F;
        if (Math.abs(target - progress) < 0.001F) progress = target;
        ELEMENT_ANIMATION.put(id, progress);
        return easeOut(progress);
    }

    private static float easeOut(float value) {
        value = Math.max(0.0F, Math.min(1.0F, value));
        return 1.0F - (float) Math.pow(1.0F - value, 3.0D);
    }

    private static int colorAlpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    private static void panel(float x, float y, float width, float height, String icon, String label, String value, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        float sw = mc.getWindow().getWidth();
        float sh = mc.getWindow().getHeight();
        GLRenderer.roundedRect(x, y, width, height, 11, withAlpha(PoorestTheme.panel(), alpha), sw, sh);
        GLRenderer.outline(x, y, width, height, 11, 1, withAlpha(PoorestTheme.border(), alpha), sw, sh);
        GLRenderer.roundedRect(x + 6, y + 6, 20, 20, 7, withAlpha(PoorestTheme.accentSurface(), alpha), sw, sh);
        hudString(icon, x + 11, y + 7, 11, withAlpha(PoorestTheme.accentBright(), alpha), sw, sh);
        hudString(label, x + 34, y + 5, 10, withAlpha(PoorestTheme.secondary(), alpha), sw, sh);
        float valueWidth = hudWidth(value, 11);
        hudString(value, x + width - valueWidth - 9, y + 5, 11, withAlpha(PoorestTheme.text(), alpha), sw, sh);
    }

    private static void drawCrosshair(float width, float height) {
        float cx = width * 0.5F, cy = height * 0.5F;
        var custom = ModuleManager.getModule("Crosshair+");
        float length = custom != null && custom.isEnabled() ? (float) ((com.poorest.client.modules.visual.ColorfulCrosshairModule) custom).getLength() : 6.0F;
        float thickness = custom != null && custom.isEnabled() ? (float) ((com.poorest.client.modules.visual.ColorfulCrosshairModule) custom).getThickness() : 2.0F;
        float gap = custom != null && custom.isEnabled() ? (float) ((com.poorest.client.modules.visual.ColorfulCrosshairModule) custom).getGap() : 2.0F;
        int color = PoorestTheme.accentBright();
        float half = thickness * 0.5F;
        GLRenderer.roundedRect(cx - gap - length, cy - half, length, thickness, half, color, width, height);
        GLRenderer.roundedRect(cx + gap, cy - half, length, thickness, half, color, width, height);
        GLRenderer.roundedRect(cx - half, cy - gap - length, thickness, length, half, color, width, height);
        GLRenderer.roundedRect(cx - half, cy + gap, thickness, length, half, color, width, height);
    }

    private static void hudString(String text, float x, float y, float size, int color, float sw, float sh) {
        float scaled = size * PoorestTheme.hudTextScale();
        PoorestFont.drawString(text, x, y, scaled, color, sw, sh);
    }

    private static float hudWidth(String text, float size) {
        return PoorestFont.width(text, size * PoorestTheme.hudTextScale());
    }

    private static int withAlpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((color >>> 24) & 0xFF) * alpha)));
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static float normalize(float rot) { return ((rot % 360.0F) + 360.0F) % 360.0F; }

    private static String direction(float rot) {
        if (rot < 22.5F || rot >= 337.5F) return "S";
        if (rot < 67.5F) return "SW";
        if (rot < 112.5F) return "W";
        if (rot < 157.5F) return "NW";
        if (rot < 202.5F) return "N";
        if (rot < 247.5F) return "NE";
        if (rot < 292.5F) return "E";
        return "SE";
    }

    private static boolean enabled(String name) {
        var module = ModuleManager.getModule(name);
        return module != null && module.isEnabled();
    }
    private static <T> T module(String name, Class<T> type) {
        Module module = ModuleManager.getModule(name);
        return type.isInstance(module) ? type.cast(module) : null;
    }

}
