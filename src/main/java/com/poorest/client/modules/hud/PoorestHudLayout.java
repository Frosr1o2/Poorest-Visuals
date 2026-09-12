package com.poorest.client.modules.hud;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class PoorestHudLayout {

    private static final Path CONFIG = Path.of(
            "config",
            "poorest_client",
            "hud.properties"
    );

    private static final float MIN_WIDTH = 70.0F;
    private static final float ARMOR_MIN_WIDTH = 280.0F;
    private static final float MAX_WIDTH = 320.0F;
    private static final float MIN_SCALE = 0.70F;
    private static final float MAX_SCALE = 1.80F;

    private static final Map<String, Element> ELEMENTS =
            new LinkedHashMap<>();

    private static Element selected;
    private static boolean editing;
    private static boolean dragging;
    private static boolean resizing;
    private static boolean cursorWasDisabled;

    private static float dragOffsetX;
    private static float dragOffsetY;
    private static float resizeStartX;
    private static float resizeStartY;
    private static float resizeStartWidth;
    private static float resizeStartScale;
    private static int lastScreenWidth = -1;
    private static int lastScreenHeight = -1;

    static {
        register("Watermark", 14, 14, 150, 1.0F);
        register("FPS", 14, 56, 120, 1.0F);
        register("Coordinates", 14, 98, 178, 1.0F);
        register("Ping", 14, 140, 125, 1.0F);
        register("Clock", 14, 182, 110, 1.0F);
        register("Direction", 14, 224, 132, 1.0F);
        register("Biome", 14, 266, 175, 1.0F);
        register("Keystrokes", 14, 308, 150, 1.0F);
        register("CPS", 14, 420, 150, 1.0F);
        register("Target HUD", 14, 462, 250, 1.0F);
        register("Armor HUD", 14, 570, 300, 1.0F);
        register("Potion HUD", 14, 680, 250, 1.0F);
        register("Arraylist", 0, 14, 190, 1.0F);
    }

    private PoorestHudLayout() {
    }

    private static void register(
            String id,
            float x,
            float y,
            float width,
            float scale
    ) {
        ELEMENTS.put(
                id,
                new Element(
                        id,
                        x,
                        y,
                        width,
                        scale
                )
        );
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG)) {
                return;
            }

            Properties properties = new Properties();

            try (InputStream input = Files.newInputStream(CONFIG)) {
                properties.load(input);
            }

            for (Element element : ELEMENTS.values()) {
                String prefix = element.id + ".";

                element.x = parse(
                        properties.getProperty(prefix + "x"),
                        element.x
                );

                element.y = parse(
                        properties.getProperty(prefix + "y"),
                        element.y
                );

                float minWidth = element.id.equals("Armor HUD") ? ARMOR_MIN_WIDTH : MIN_WIDTH;
                element.width = clamp(
                        parse(
                                properties.getProperty(prefix + "width"),
                                element.width
                        ),
                        minWidth,
                        MAX_WIDTH
                );

                element.scale = clamp(
                        parse(
                                properties.getProperty(prefix + "scale"),
                                element.scale
                        ),
                        MIN_SCALE,
                        MAX_SCALE
                );
            }
        } catch (Exception ignored) {
            reset();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG.getParent());

            Properties properties = new Properties();

            for (Element element : ELEMENTS.values()) {
                String prefix = element.id + ".";

                properties.setProperty(
                        prefix + "x",
                        Float.toString(element.x)
                );

                properties.setProperty(
                        prefix + "y",
                        Float.toString(element.y)
                );

                properties.setProperty(
                        prefix + "width",
                        Float.toString(element.width)
                );

                properties.setProperty(
                        prefix + "scale",
                        Float.toString(element.scale)
                );
            }

            try (OutputStream output = Files.newOutputStream(CONFIG)) {
                properties.store(
                        output,
                        "Poorest Visuals HUD Layout"
                );
            }
        } catch (IOException ignored) {
        }
    }

    public static void writeTo(Properties properties, String root) {
        for (Element element : ELEMENTS.values()) {
            String prefix = root + element.id + ".";
            properties.setProperty(prefix + "x", Float.toString(element.x));
            properties.setProperty(prefix + "y", Float.toString(element.y));
            properties.setProperty(prefix + "width", Float.toString(element.width));
            properties.setProperty(prefix + "scale", Float.toString(element.scale));
        }
    }

    public static void readFrom(Properties properties, String root) {
        for (Element element : ELEMENTS.values()) {
            String prefix = root + element.id + ".";
            element.x = parse(properties.getProperty(prefix + "x"), element.x);
            element.y = parse(properties.getProperty(prefix + "y"), element.y);
            float minWidth = element.id.equals("Armor HUD") ? ARMOR_MIN_WIDTH : MIN_WIDTH;
            element.width = clamp(parse(properties.getProperty(prefix + "width"), element.width), minWidth, MAX_WIDTH);
            element.scale = clamp(parse(properties.getProperty(prefix + "scale"), element.scale), MIN_SCALE, MAX_SCALE);
        }
        clampAll();
        save();
    }

    public static void reset() {
        ELEMENTS.get("Watermark").set(14, 14, 150, 1.0F);
        ELEMENTS.get("FPS").set(14, 56, 120, 1.0F);
        ELEMENTS.get("Coordinates").set(14, 98, 178, 1.0F);
        ELEMENTS.get("Ping").set(14, 140, 125, 1.0F);
        ELEMENTS.get("Clock").set(14, 182, 110, 1.0F);
        ELEMENTS.get("Direction").set(14, 224, 132, 1.0F);
        ELEMENTS.get("Biome").set(14, 266, 175, 1.0F);
        ELEMENTS.get("Keystrokes").set(14, 308, 150, 1.0F);
        ELEMENTS.get("CPS").set(14, 420, 150, 1.0F);
        ELEMENTS.get("Target HUD").set(14, 462, 250, 1.0F);
        ELEMENTS.get("Armor HUD").set(14, 570, 300, 1.0F);
        ELEMENTS.get("Potion HUD").set(14, 680, 250, 1.0F);
        ELEMENTS.get("Arraylist").set(0, 14, 190, 1.0F);
        save();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() != null) {
            int sw = minecraft.getWindow().getWidth();
            int sh = minecraft.getWindow().getHeight();
            if (lastScreenWidth > 0 && lastScreenHeight > 0 && (sw != lastScreenWidth || sh != lastScreenHeight)) {
                reflowForResolution(lastScreenWidth, lastScreenHeight, sw, sh);
            }
            lastScreenWidth = sw;
            lastScreenHeight = sh;
        }

        if (!editing) {
            return;
        }

        if (!dragging && !resizing) {
            return;
        }

        long window = minecraft.getWindow().getWindow();

        if (GLFW.glfwGetMouseButton(
                window,
                GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) != GLFW.GLFW_PRESS) {
            dragging = false;
            resizing = false;
            save();
            return;
        }

        double[] mouseX = {0};
        double[] mouseY = {0};

        GLFW.glfwGetCursorPos(
                window,
                mouseX,
                mouseY
        );

        float sx = physicalMouseX(mouseX[0]);
        float sy = physicalMouseY(mouseY[0]);

        if (selected == null) {
            return;
        }

        if (resizing) {
            float minWidth = selected.id.equals("Armor HUD") ? ARMOR_MIN_WIDTH : MIN_WIDTH;
            selected.width = clamp(
                    resizeStartWidth + sx - resizeStartX,
                    minWidth,
                    MAX_WIDTH
            );

            float delta =
                    (sy - resizeStartY) / 220.0F;

            selected.scale = clamp(
                    resizeStartScale + delta,
                    MIN_SCALE,
                    MAX_SCALE
            );
        } else if (dragging) {
            selected.x = sx - dragOffsetX;
            selected.y = sy - dragOffsetY;
            clampElement(selected);
        }
    }

    private static void reflowForResolution(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        float sx = newWidth / (float) Math.max(1, oldWidth);
        float sy = newHeight / (float) Math.max(1, oldHeight);
        for (Element element : ELEMENTS.values()) {
            element.x *= sx;
            element.y *= sy;
            clampElement(element);
        }
        save();
    }

    private static void clampAll() {
        for (Element element : ELEMENTS.values()) clampElement(element);
    }

    public static void setEditing(
            boolean value
    ) {
        if (editing == value) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long window = minecraft.getWindow().getWindow();

        if (value) {
            cursorWasDisabled =
                    GLFW.glfwGetInputMode(
                            window,
                            GLFW.GLFW_CURSOR
                    ) == GLFW.GLFW_CURSOR_DISABLED;

            GLFW.glfwSetInputMode(
                    window,
                    GLFW.GLFW_CURSOR,
                    GLFW.GLFW_CURSOR_NORMAL
            );
        }

        editing = value;

        if (!editing) {
            dragging = false;
            resizing = false;
            selected = null;
            save();

            GLFW.glfwSetInputMode(
                    window,
                    GLFW.GLFW_CURSOR,
                    cursorWasDisabled
                            ? GLFW.GLFW_CURSOR_DISABLED
                            : GLFW.GLFW_CURSOR_NORMAL
            );
        }
    }

    public static boolean isEditing() {
        return editing;
    }

    public static boolean mousePressed(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!editing || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        double[] p = toPhysical(mouseX, mouseY);
        mouseX = p[0];
        mouseY = p[1];

        Element hit = findElement(mouseX, mouseY);

        if (hit == null) {
            return false;
        }

        selected = hit;

        float hitHeight = elementHeight(hit) * hit.scale;
        if (inside(
                mouseX,
                mouseY,
                hit.x + hit.width - 22,
                hit.y + hitHeight - 22,
                22,
                22
        )) {
            resizing = true;
            dragging = false;

            resizeStartX = (float) mouseX;
            resizeStartY = (float) mouseY;
            resizeStartWidth = hit.width;
            resizeStartScale = hit.scale;
        } else {
            dragging = true;
            resizing = false;

            dragOffsetX = (float) mouseX - hit.x;
            dragOffsetY = (float) mouseY - hit.y;
        }

        return true;
    }

    public static boolean mouseReleased(
            int button
    ) {
        if (!editing || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }

        dragging = false;
        resizing = false;
        save();

        return true;
    }

    public static boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {
        if (!editing || selected == null) {
            return false;
        }

        selected.scale = clamp(
                selected.scale + (float) delta * 0.05F,
                MIN_SCALE,
                MAX_SCALE
        );

        save();
        return true;
    }

    public static float x(String id) {
        return get(id).x;
    }

    public static float y(String id) {
        return get(id).y;
    }

    public static float width(String id) {
        return get(id).width;
    }

    public static float scale(String id) {
        return get(id).scale;
    }

    public static void renderEditorOverlay(
            float screenWidth,
            float screenHeight
    ) {
        if (!editing) {
            return;
        }

        for (Element element : ELEMENTS.values()) {
            int color =
                    element == selected
                            ? 0xF5A16DFF
                            : 0xB78E8A98;

            float width = element.width;
            float height = elementHeight(element) * element.scale;

            // Four thin lines instead of another rounded outline pass.
            com.poorest.client.ui.render.GLRenderer.roundedRect(
                    element.x,
                    element.y,
                    width,
                    1.0F,
                    0.5F,
                    color,
                    screenWidth,
                    screenHeight
            );

            com.poorest.client.ui.render.GLRenderer.roundedRect(
                    element.x,
                    element.y + height - 1,
                    width,
                    1.0F,
                    0.5F,
                    color,
                    screenWidth,
                    screenHeight
            );

            com.poorest.client.ui.render.GLRenderer.roundedRect(
                    element.x,
                    element.y,
                    1.0F,
                    height,
                    0.5F,
                    color,
                    screenWidth,
                    screenHeight
            );

            com.poorest.client.ui.render.GLRenderer.roundedRect(
                    element.x + width - 1,
                    element.y,
                    1.0F,
                    height,
                    0.5F,
                    color,
                    screenWidth,
                    screenHeight
            );

            com.poorest.client.ui.font.PoorestFont.drawString(
                    element.id,
                    element.x + 5,
                    element.y - 12,
                    8,
                    color,
                    screenWidth,
                    screenHeight
            );

            com.poorest.client.ui.render.GLRenderer.roundedRect(
                    element.x + width - 18,
                    element.y + height - 18,
                    12,
                    12,
                    4,
                    color,
                    screenWidth,
                    screenHeight
            );
        }
    }

    private static Element findElement(
            double mouseX,
            double mouseY
    ) {
        Element result = null;

        for (Element element : ELEMENTS.values()) {
            float h = elementHeight(element) * element.scale;

            if (inside(
                    mouseX,
                    mouseY,
                    element.x,
                    element.y,
                    element.width,
                    h
            )) {
                result = element;
            }
        }

        return result;
    }

    private static Element get(String id) {
        return ELEMENTS.get(id);
    }

    private static float elementHeight(Element element) {
        return switch (element.id) {
            case "Keystrokes" -> 42.0F * 4.0F + 5.0F * 3.0F;
            case "Target HUD" -> 94.0F;
            case "Armor HUD" -> 124.0F;
            case "Potion HUD" -> 126.0F;
            default -> 32.0F;
        };
    }

    private static void clampElement(
            Element element
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        float sw = minecraft.getWindow().getWidth();
        float sh = minecraft.getWindow().getHeight();
        float h = elementHeight(element) * element.scale;
        float minWidth = element.id.equals("Armor HUD") ? ARMOR_MIN_WIDTH : MIN_WIDTH;
        element.width = clamp(element.width, minWidth, Math.min(MAX_WIDTH, Math.max(minWidth, sw - 8)));

        element.x = clamp(
                element.x,
                4,
                Math.max(4, sw - element.width - 4)
        );

        element.y = clamp(
                element.y,
                4,
                Math.max(4, sh - h - 4)
        );
    }

    private static double[] toPhysical(
            double mouseX,
            double mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        double scale = minecraft.getWindow().getGuiScale();
        return new double[] {
                mouseX * scale,
                mouseY * scale
        };
    }

    private static float physicalMouseX(
            double mouseX
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        long window = minecraft.getWindow().getWindow();
        java.nio.IntBuffer wb = org.lwjgl.BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(window, wb, null);
        int windowWidth = Math.max(1, wb.get(0));
        return (float) (
                mouseX * minecraft.getWindow().getWidth() / windowWidth
        );
    }

    private static float physicalMouseY(
            double mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        long window = minecraft.getWindow().getWindow();
        java.nio.IntBuffer hb = org.lwjgl.BufferUtils.createIntBuffer(1);
        GLFW.glfwGetWindowSize(window, null, hb);
        int windowHeight = Math.max(1, hb.get(0));
        return (float) (
                mouseY * minecraft.getWindow().getHeight() / windowHeight
        );
    }

    private static boolean inside(
            double mx,
            double my,
            double x,
            double y,
            double w,
            double h
    ) {
        return mx >= x && mx <= x + w &&
                my >= y && my <= y + h;
    }

    private static float parse(
            String value,
            float fallback
    ) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float clamp(
            float value,
            float min,
            float max
    ) {
        return Math.max(
                min,
                Math.min(
                        max,
                        value
                )
        );
    }

    private static final class Element {
        private final String id;
        private float x;
        private float y;
        private float width;
        private float scale;

        private Element(
                String id,
                float x,
                float y,
                float width,
                float scale
        ) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.scale = scale;
        }

        private void set(
                float x,
                float y,
                float width,
                float scale
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.scale = scale;
        }
    }
}
