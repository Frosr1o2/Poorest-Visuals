package com.poorest.client.modules.visual;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleCategory;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.ModeSetting;

import java.util.ArrayDeque;
import java.util.Deque;

public final class JumpCirclesModule extends Module {
    public record Circle(double x, double y, double z, long createdAt) {}

    private final NumberSetting duration = addSetting(new NumberSetting(
            "Duration", "Circle lifetime in milliseconds.", 700.0, 150.0, 2000.0, 50.0
    ));
    private final NumberSetting radius = addSetting(new NumberSetting(
            "Radius", "Maximum circle radius.", 1.15, 0.25, 3.0, 0.05
    ));
    private final NumberSetting width = addSetting(new NumberSetting(
            "Line Width", "Circle line width.", 1.0, 1.0, 1.0, 1.0
    ));
    private final ModeSetting color = addSetting(new ModeSetting(
            "Color", "Jump circle color.", "Accent",
            "Accent", "Purple", "Blue", "Cyan", "Pink", "Green", "Orange", "Red", "White", "Rainbow"
    ));

    private final Deque<Circle> circles = new ArrayDeque<>();
    private boolean wasOnGround;

    public JumpCirclesModule() {
        super("Jump Circles", "Animated expanding rings when the player lands after a jump.", ModuleCategory.VISUAL);
    }

    @Override
    protected void onEnable() {
        circles.clear();
        wasOnGround = true;
    }

    @Override
    public void onTick() {
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) return;

        boolean onGround = player.onGround();
        if (onGround && !wasOnGround && player.getDeltaMovement().y <= 0.05D) {
            circles.addLast(new Circle(player.getX(), player.getY() + 0.03D, player.getZ(), System.currentTimeMillis()));
        }
        wasOnGround = onGround;
        cleanup();
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        long life = (long) duration.getValueAsDouble();
        while (!circles.isEmpty() && now - circles.peekFirst().createdAt() > life) {
            circles.removeFirst();
        }
    }

    public Deque<Circle> circles() { return circles; }
    public float duration() { return (float) duration.getValueAsDouble(); }
    public float radius() { return (float) radius.getValueAsDouble(); }
    public float lineWidth() { return (float) width.getValueAsDouble(); }
    public String color() { return color.getValue(); }
}
