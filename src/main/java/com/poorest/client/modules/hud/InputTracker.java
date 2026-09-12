package com.poorest.client.modules.hud;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InputTracker {
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

    private InputTracker() {
    }

    public static void recordClick(int button) {
        long now = System.currentTimeMillis();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            LEFT_CLICKS.addLast(now);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            RIGHT_CLICKS.addLast(now);
        }
        trim(now);
    }

    public static int getLeftCps() {
        trim(System.currentTimeMillis());
        return LEFT_CLICKS.size();
    }

    public static int getRightCps() {
        trim(System.currentTimeMillis());
        return RIGHT_CLICKS.size();
    }

    private static void trim(long now) {
        while (!LEFT_CLICKS.isEmpty() && now - LEFT_CLICKS.peekFirst() > 1000L) {
            LEFT_CLICKS.removeFirst();
        }
        while (!RIGHT_CLICKS.isEmpty() && now - RIGHT_CLICKS.peekFirst() > 1000L) {
            RIGHT_CLICKS.removeFirst();
        }
    }
}
