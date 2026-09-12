package com.poorest.client.core;

import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.core.keybind.KeybindManager;
import com.poorest.client.core.setting.BooleanSetting;
import com.poorest.client.core.setting.ModeSetting;
import com.poorest.client.core.setting.NumberSetting;
import com.poorest.client.core.setting.Setting;
import com.poorest.client.modules.hud.PoorestHudLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Persistent client configuration and portable .cfg profiles. */
public final class ClientConfigManager {
    private static final Path ROOT = Path.of("config", "poorest_client");
    private static final Path DEFAULT = ROOT.resolve("client.properties");
    private static final Path PROFILES = ROOT.resolve("profiles");
    private static final Path EXPORTS = ROOT.resolve("exports");

    private ClientConfigManager() {
    }

    public static void load() {
        loadFile(DEFAULT);
    }

    public static void save() {
        saveFile(DEFAULT);
        PoorestHudLayout.save();
    }

    public static boolean saveProfile(String name) {
        String safe = sanitize(name);
        if (safe.isBlank()) return false;
        return saveFile(PROFILES.resolve(safe + ".properties"));
    }

    public static boolean loadProfile(String name) {
        String safe = sanitize(name);
        if (safe.isBlank()) return false;
        Path file = PROFILES.resolve(safe + ".properties");
        if (!Files.exists(file)) return false;
        return loadFile(file);
    }

    public static boolean exportConfig(String name) {
        String safe = sanitize(name);
        if (safe.isBlank()) return false;
        try {
            Files.createDirectories(EXPORTS);
            Properties properties = new Properties();
            fillProperties(properties);
            PoorestHudLayout.writeTo(properties, "hud.");
            try (OutputStream output = Files.newOutputStream(EXPORTS.resolve(safe + ".cfg"))) {
                properties.store(output, "Poorest Visuals portable configuration");
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static boolean importConfig(String name) {
        String safe = sanitize(name);
        if (safe.isBlank()) return false;
        Path file = EXPORTS.resolve(safe + ".cfg");
        if (!Files.exists(file)) return false;
        try (InputStream input = Files.newInputStream(file)) {
            Properties properties = new Properties();
            properties.load(input);
            applyProperties(properties);
            PoorestHudLayout.readFrom(properties, "hud.");
            save();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static List<String> exports() {
        List<String> result = new ArrayList<>();
        try {
            Files.createDirectories(EXPORTS);
            try (var stream = Files.list(EXPORTS)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".cfg"))
                        .map(path -> path.getFileName().toString().replaceFirst("\\.cfg$", ""))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(result::add);
            }
        } catch (IOException ignored) {
        }
        return result;
    }

    public static List<String> profiles() {
        List<String> result = new ArrayList<>();
        try {
            Files.createDirectories(PROFILES);
            try (var stream = Files.list(PROFILES)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".properties"))
                        .map(path -> path.getFileName().toString().replaceFirst("\\.properties$", ""))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(result::add);
            }
        } catch (IOException ignored) {
        }
        return result;
    }

    private static boolean loadFile(Path file) {
        if (!Files.exists(file)) return false;
        try (InputStream input = Files.newInputStream(file)) {
            Properties properties = new Properties();
            properties.load(input);
            applyProperties(properties);
            PoorestHudLayout.readFrom(properties, "hud.");
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void applyProperties(Properties properties) {
        KeybindManager.load(properties);
        for (Module module : ModuleManager.getModules()) {
            String prefix = "module." + module.getName() + ".";
            String enabled = properties.getProperty(prefix + "enabled");
            if (enabled != null) module.setEnabled(Boolean.parseBoolean(enabled));

            for (Setting<?> setting : module.getSettings()) {
                String value = properties.getProperty(prefix + "setting." + setting.getName());
                if (value == null) continue;
                apply(setting, value);
            }
        }
    }

    private static boolean saveFile(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Properties properties = new Properties();
            fillProperties(properties);
            try (OutputStream output = Files.newOutputStream(file)) {
                properties.store(output, "Poorest Visuals configuration");
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static void fillProperties(Properties properties) {
        KeybindManager.save(properties);
        for (Module module : ModuleManager.getModules()) {
            String prefix = "module." + module.getName() + ".";
            properties.setProperty(prefix + "enabled", Boolean.toString(module.isEnabled()));
            for (Setting<?> setting : module.getSettings()) {
                Object value = setting.getValue();
                if (value != null) properties.setProperty(prefix + "setting." + setting.getName(), value.toString());
            }
        }
    }

    private static void apply(Setting<?> setting, String value) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.setEnabled(Boolean.parseBoolean(value));
        } else if (setting instanceof NumberSetting numberSetting) {
            try {
                numberSetting.setValue(Double.parseDouble(value));
            } catch (NumberFormatException ignored) {
            }
        } else if (setting instanceof ModeSetting modeSetting) {
            modeSetting.setValue(value);
        }
    }

    private static String sanitize(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
