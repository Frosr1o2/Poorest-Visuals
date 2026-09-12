package com.poorest.client.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class PoorestTheme {

    private static final Path CONFIG = Path.of(
            "config",
            "poorest_client",
            "theme.properties"
    );

    private static int accent = 0xFF9B6CFF;
    private static float brightness = 1.08F;
    private static float opacity = 0.97F;
    private static float rounding = 18.0F;
    private static boolean shadows = true;
    private static float uiScale = 1.0F;
    private static float hudTextScale = 1.0F;
    private static float uiTextScale = 1.0F;

    private PoorestTheme() {
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

            accent = parseColor(properties.getProperty(
                    "accent",
                    Integer.toHexString(accent)
            ));

            brightness = clamp(
                    parseFloat(properties.getProperty("brightness", "1.08"), 1.08F),
                    0.65F,
                    1.25F
            );

            opacity = clamp(
                    parseFloat(properties.getProperty("opacity", "0.97"), 0.97F),
                    0.70F,
                    1.0F
            );

            rounding = clamp(
                    parseFloat(properties.getProperty("rounding", "18"), 18.0F),
                    8.0F,
                    28.0F
            );

            uiScale = clamp(
                    parseFloat(properties.getProperty("uiScale", "1.0"), 1.0F),
                    0.85F,
                    1.15F
            );

            hudTextScale = clamp(
                    parseFloat(properties.getProperty("hudTextScale", "1.0"), 1.0F),
                    0.75F,
                    2.0F
            );

            uiTextScale = clamp(
                    parseFloat(properties.getProperty("uiTextScale", "1.0"), 1.0F),
                    0.80F,
                    1.50F
            );

            shadows = Boolean.parseBoolean(
                    properties.getProperty("shadows", "true")
            );
        } catch (Exception ignored) {
            reset();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG.getParent());

            Properties properties = new Properties();
            properties.setProperty("accent", Integer.toHexString(accent));
            properties.setProperty("brightness", Float.toString(brightness));
            properties.setProperty("opacity", Float.toString(opacity));
            properties.setProperty("rounding", Float.toString(rounding));
            properties.setProperty("uiScale", Float.toString(uiScale));
            properties.setProperty("hudTextScale", Float.toString(hudTextScale));
            properties.setProperty("uiTextScale", Float.toString(uiTextScale));
            properties.setProperty("shadows", Boolean.toString(shadows));

            try (OutputStream output = Files.newOutputStream(CONFIG)) {
                properties.store(output, "Poorest Visuals Theme");
            }
        } catch (IOException ignored) {
        }
    }

    public static void reset() {
        accent = 0xFF9B6CFF;
        brightness = 1.08F;
        opacity = 0.97F;
        rounding = 18.0F;
        shadows = true;
        uiScale = 1.0F;
        hudTextScale = 1.0F;
        uiTextScale = 1.0F;
        save();
    }

    public static int accent() {
        return accent;
    }

    public static void setAccent(int color) {
        accent = 0xFF000000 | (color & 0x00FFFFFF);
        save();
    }

    public static float brightness() {
        return brightness;
    }

    public static void setBrightness(float value) {
        brightness = clamp(value, 0.65F, 1.25F);
        save();
    }

    public static float opacity() {
        return opacity;
    }

    public static void setOpacity(float value) {
        opacity = clamp(value, 0.70F, 1.0F);
        save();
    }

    public static float rounding() {
        return rounding;
    }

    public static void setRounding(float value) {
        rounding = clamp(value, 8.0F, 28.0F);
        save();
    }

    public static float uiScale() {
        return uiScale;
    }

    public static void setUiScale(float value) {
        uiScale = clamp(value, 0.85F, 1.15F);
        save();
    }

    public static float hudTextScale() {
        return hudTextScale;
    }

    public static void setHudTextScale(float value) {
        hudTextScale = clamp(value, 0.75F, 2.0F);
        save();
    }

    public static float uiTextScale() {
        return uiTextScale;
    }

    public static void setUiTextScale(float value) {
        uiTextScale = clamp(value, 0.80F, 1.50F);
        save();
    }

    public static boolean shadows() {
        return shadows;
    }

    public static void setShadows(boolean value) {
        shadows = value;
        save();
    }

    public static int panel() {
        return color(0x161922, opacity);
    }

    public static int header() {
        return color(0x1B1F29, Math.min(1.0F, opacity + 0.02F));
    }

    public static int card() {
        return color(0x1C1F29, Math.max(0.82F, opacity - 0.05F));
    }

    public static int cardHover() {
        return color(0x282C38, Math.max(0.88F, opacity - 0.02F));
    }

    public static int cardEnabled() {
        return color(mix(0x24202F, accent, 0.18F), opacity - 0.03F);
    }

    public static int surface() {
        return color(0x20242E, Math.max(0.86F, opacity - 0.03F));
    }

    public static int text() {
        return adjust(0xFFF7F5FA, brightness);
    }

    public static int secondary() {
        return adjust(0xFFBBB7C3, brightness);
    }

    public static int muted() {
        return adjust(0xFF88848F, brightness);
    }

    public static int accentBright() {
        return mix(accent, 0xFFFFFFFF, 0.32F);
    }

    public static int accentSurface() {
        return color(mix(0x16141D, accent, 0.25F), opacity);
    }

    public static int border() {
        return color(mix(0x2A2D38, accent, 0.14F), 0.48F);
    }

    public static int switchOff() {
        return color(0x30323C, 0.95F);
    }

    public static int switchOn() {
        return accent;
    }

    public static int switchKnobOff() {
        return 0xFF77737E;
    }

    public static int switchKnobOn() {
        return 0xFFF8F4FF;
    }

    public static int mix(int a, int b, float amount) {
        amount = clamp(amount, 0.0F, 1.0F);

        int ar = (a >>> 16) & 0xFF;
        int ag = (a >>> 8) & 0xFF;
        int ab = a & 0xFF;

        int br = (b >>> 16) & 0xFF;
        int bg = (b >>> 8) & 0xFF;
        int bb = b & 0xFF;

        return 0xFF000000
                | ((int) (ar + (br - ar) * amount) << 16)
                | ((int) (ag + (bg - ag) * amount) << 8)
                | (int) (ab + (bb - ab) * amount);
    }

    private static int color(int rgb, float alpha) {
        alpha = clamp(alpha, 0.0F, 1.0F);
        return ((int) (alpha * 255.0F) << 24) | (rgb & 0x00FFFFFF);
    }

    private static int adjust(int color, float factor) {
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.min(255, Math.round(r * factor));
        g = Math.min(255, Math.round(g * factor));
        b = Math.min(255, Math.round(b * factor));

        return (color & 0xFF000000)
                | (r << 16)
                | (g << 8)
                | b;
    }

    private static int parseColor(String value) {
        String clean = value == null ? "" : value.trim();
        if (clean.startsWith("#")) {
            clean = clean.substring(1);
        }
        if (clean.length() == 6) {
            clean = "FF" + clean;
        }
        return (int) Long.parseLong(clean, 16);
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
