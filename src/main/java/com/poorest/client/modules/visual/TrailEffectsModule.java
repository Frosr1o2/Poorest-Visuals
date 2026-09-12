package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.ModeSetting;

import java.util.ArrayDeque;
import java.util.Deque;

public final class TrailEffectsModule extends Module {
    public record Point(double x, double y, double z, long createdAt) {}

    private final NumberSetting duration = addSetting(new NumberSetting(
            "Duration", "Trail lifetime in milliseconds.", 450.0, 100.0, 1500.0, 25.0
    ));
    private final NumberSetting length = addSetting(new NumberSetting(
            "Length", "Maximum number of trail points.", 24.0, 4.0, 80.0, 1.0
    ));
    private final NumberSetting width = addSetting(new NumberSetting(
            "Line Width", "Trail line width.", 1.0, 1.0, 1.0, 1.0
    ));
    private final ModeSetting color = addSetting(new ModeSetting(
            "Color", "Trail color.", "Accent",
            "Accent", "Purple", "Blue", "Cyan", "Pink", "Green", "Orange", "Red", "White", "Rainbow"
    ));

    private final Deque<Point> points = new ArrayDeque<>();
    private int tickCounter;

    public TrailEffectsModule() {
        super("Trail Effects", "Animated glowing trail following the local player.", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        points.clear();
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return;
        tickCounter++;
        if (tickCounter % 2 == 0) {
            points.addLast(new Point(player.getX(), player.getY() + 0.05D, player.getZ(), System.currentTimeMillis()));
        }
        cleanup();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        long life = (long) duration.getValueAsDouble();
        int max = (int) length.getValueAsDouble();
        while (!points.isEmpty() && (now - points.peekFirst().createdAt() > life || points.size() > max)) {
            points.removeFirst();
        }
    }

    public Deque<Point> points() { return points; }
    public float duration() { return (float) duration.getValueAsDouble(); }
    public float lineWidth() { return (float) width.getValueAsDouble(); }
    public String color() { return color.getValue(); }
}
