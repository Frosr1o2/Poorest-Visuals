package com.poorest.client.ui;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.setting.Setting;
import net.minecraft.client.resources.language.I18n;

import java.util.Locale;

/**
 * Translation bridge for the custom client UI.
 * Uses Minecraft's currently selected language and falls back to the original text.
 */
public final class PoorestLang {

    private PoorestLang() {
    }

    public static String text(String key, String fallback) {
        String value = I18n.get(key);
        return value.equals(key) ? fallback : value;
    }

    public static String module(Module module) {
        String slug = slug(module.getName());
        return text("module." + slug + ".name", module.getName());
    }

    public static String moduleDescription(Module module) {
        String slug = slug(module.getName());
        return text("module." + slug + ".description", module.getDescription());
    }

    public static String setting(Setting<?> setting) {
        String slug = slug(setting.getName());
        return text("setting." + slug + ".name", setting.getName());
    }

    public static String settingDescription(Setting<?> setting) {
        String slug = slug(setting.getName());
        return text("setting." + slug + ".description", setting.getDescription());
    }

    public static String category(String category) {
        return text("category." + category.toLowerCase(Locale.ROOT), category);
    }

    private static String slug(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
