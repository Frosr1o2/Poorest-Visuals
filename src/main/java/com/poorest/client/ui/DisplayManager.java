package com.poorest.client.ui;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Client-side display mode helper used by the Poorest settings screen. */
public final class DisplayManager {
    public enum Aspect {
        ALL("All"),
        RATIO_16_9("16:9"),
        RATIO_16_10("16:10"),
        RATIO_4_3("4:3");

        private final String label;

        Aspect(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public boolean accepts(int width, int height) {
            if (this == ALL) return true;
            double ratio = width / (double) height;
            return switch (this) {
                case RATIO_16_9 -> Math.abs(ratio - (16.0 / 9.0)) < 0.025;
                case RATIO_16_10 -> Math.abs(ratio - (16.0 / 10.0)) < 0.025;
                case RATIO_4_3 -> Math.abs(ratio - (4.0 / 3.0)) < 0.025;
                default -> true;
            };
        }
    }

    public record Mode(int width, int height, int refreshRate) {
        public String label() {
            return width + " × " + height;
        }

        public String fullLabel() {
            return width + " × " + height + " @ " + refreshRate + " Hz";
        }
    }

    private static final Path CONFIG = Path.of("config", "poorest_client", "display.properties");

    private static Aspect aspect = Aspect.ALL;
    private static boolean fullscreen;
    private static int selectedWidth = 1920;
    private static int selectedHeight = 1080;
    private static int selectedRefreshRate = 60;

    private DisplayManager() {
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG)) {
                java.util.Properties properties = new java.util.Properties();
                try (InputStream input = Files.newInputStream(CONFIG)) {
                    properties.load(input);
                }
                fullscreen = Boolean.parseBoolean(properties.getProperty("fullscreen", "false"));
                selectedWidth = Integer.parseInt(properties.getProperty("width", "1920"));
                selectedHeight = Integer.parseInt(properties.getProperty("height", "1080"));
                selectedRefreshRate = Integer.parseInt(properties.getProperty("refreshRate", "60"));
            }
        } catch (Exception ignored) {
        }
        refreshCurrentMode();
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG.getParent());
            java.util.Properties properties = new java.util.Properties();
            properties.setProperty("fullscreen", Boolean.toString(fullscreen));
            properties.setProperty("width", Integer.toString(selectedWidth));
            properties.setProperty("height", Integer.toString(selectedHeight));
            properties.setProperty("refreshRate", Integer.toString(selectedRefreshRate));
            try (OutputStream output = Files.newOutputStream(CONFIG)) {
                properties.store(output, "Poorest Visuals Display");
            }
        } catch (IOException ignored) {
        }
    }

    public static Aspect aspect() {
        return aspect;
    }

    public static void setAspect(Aspect value) {
        aspect = value == null ? Aspect.ALL : value;
    }

    public static boolean fullscreen() {
        return fullscreen;
    }

    public static void setFullscreen(boolean value) {
        fullscreen = value;
    }

    public static Mode currentMode() {
        return new Mode(selectedWidth, selectedHeight, selectedRefreshRate);
    }

    public static List<Mode> supportedModes() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        long monitor = GLFW.glfwGetWindowMonitor(window);
        if (monitor == 0L) monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) return List.of(currentMode());

        GLFWVidMode.Buffer modes = GLFW.glfwGetVideoModes(monitor);
        if (modes == null) return List.of(currentMode());

        Map<String, Mode> unique = new LinkedHashMap<>();
        for (int i = 0; i < modes.limit(); i++) {
            GLFWVidMode mode = modes.get(i);
            int w = mode.width();
            int h = mode.height();
            int hz = mode.refreshRate();
            if (w < 640 || h < 480) continue;
            if (!aspect.accepts(w, h)) continue;

            String key = w + "x" + h;
            Mode old = unique.get(key);
            if (old == null || hz > old.refreshRate()) {
                unique.put(key, new Mode(w, h, hz));
            }
        }

        List<Mode> result = new ArrayList<>(unique.values());
        result.sort((a, b) -> {
            long areaA = (long) a.width * a.height;
            long areaB = (long) b.width * b.height;
            int area = Long.compare(areaB, areaA);
            if (area != 0) return area;
            return Integer.compare(b.refreshRate, a.refreshRate);
        });
        return result;
    }

    public static void refreshCurrentMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;

        int w = mc.getWindow().getWidth();
        int h = mc.getWindow().getHeight();
        if (w > 0 && h > 0) {
            selectedWidth = w;
            selectedHeight = h;
        }

        long monitor = GLFW.glfwGetWindowMonitor(mc.getWindow().getWindow());
        fullscreen = monitor != 0L;

        if (monitor != 0L) {
            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
            if (mode != null) selectedRefreshRate = mode.refreshRate();
        }
    }

    public static void apply(Mode mode) {
        if (mode == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;

        long window = mc.getWindow().getWindow();
        long monitor = GLFW.glfwGetPrimaryMonitor();
        if (monitor == 0L) return;

        selectedWidth = mode.width();
        selectedHeight = mode.height();
        selectedRefreshRate = mode.refreshRate();

        if (fullscreen) {
            GLFW.glfwSetWindowMonitor(
                    window,
                    monitor,
                    0,
                    0,
                    mode.width(),
                    mode.height(),
                    mode.refreshRate()
            );
        } else {
            GLFW.glfwSetWindowSize(window, mode.width(), mode.height());
            GLFW.glfwSetWindowPos(window, 0, 0);
        }

        save();
    }

    public static void toggleFullscreen() {
        fullscreen = !fullscreen;
        Mode mode = currentMode();
        apply(mode);
    }
}
