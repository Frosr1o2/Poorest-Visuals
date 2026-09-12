package com.poorest.client.core.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import com.poorest.client.PoorestClient;
import com.poorest.client.core.module.Module;
import com.poorest.client.core.module.ModuleManager;
import com.poorest.client.core.ClientConfigManager;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Client module keybinds and the .bind chat command.
 * Examples:
 *   .bind Fullbright G
 *   .bind Hit Sound F6
 *   .bind primerfunc primerknopka
 *   .bind Fullbright none
 */
public final class KeybindManager {
    public static final KeyMapping OPEN_MENU = new KeyMapping(
            "key." + PoorestClient.MOD_ID + ".open_menu",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "key.categories." + PoorestClient.MOD_ID
    );

    private static final Map<String, Integer> BINDS = new HashMap<>();

    private KeybindManager() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_MENU);
    }

    public static void load(Properties properties) {
        BINDS.clear();
        for (Module module : ModuleManager.getModules()) {
            String value = properties.getProperty("bind." + module.getName());
            if (value == null) continue;
            try {
                int code = Integer.parseInt(value);
                if (code != 0) BINDS.put(module.getName().toLowerCase(Locale.ROOT), code);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public static void save(Properties properties) {
        for (Module module : ModuleManager.getModules()) {
            Integer code = BINDS.get(module.getName().toLowerCase(Locale.ROOT));
            if (code != null && code != 0) {
                properties.setProperty("bind." + module.getName(), Integer.toString(code));
            }
        }
    }

    public static void handleKey(int keyCode) {
        if (keyCode <= GLFW.GLFW_KEY_UNKNOWN) return;
        for (Map.Entry<String, Integer> entry : BINDS.entrySet()) {
            if (!Integer.valueOf(keyCode).equals(entry.getValue())) continue;
            Module module = ModuleManager.getModule(entry.getKey());
            if (module != null) module.toggle();
        }
    }

    public static void handleMouse(int button) {
        int encoded = mouseCode(button);
        for (Map.Entry<String, Integer> entry : BINDS.entrySet()) {
            if (!Integer.valueOf(encoded).equals(entry.getValue())) continue;
            Module module = ModuleManager.getModule(entry.getKey());
            if (module != null) module.toggle();
        }
    }

    public static void handleChatCommand(ClientChatEvent event) {
        String message = event.getMessage().trim();
        if (handleConfigCommand(event, message)) return;
        if (!message.toLowerCase(Locale.ROOT).startsWith(".bind")) return;

        String[] parts = message.split("\\s+");
        if (!parts[0].equalsIgnoreCase(".bind")) return;

        // Dot-commands do not use Brigadier's native completion, so provide
        // command-style help directly in chat for an intuitive experience.
        if (parts.length == 1 || parts[1].equalsIgnoreCase("help") || parts[1].equalsIgnoreCase("?")) {
            event.setCanceled(true);
            showHelp();
            return;
        }

        if (parts.length >= 2 && parts[1].equalsIgnoreCase("unbind")) {
            event.setCanceled(true);
            if (parts.length == 2) {
                info("Usage: .unbind <module>");
                info("Example: .unbind Fullbright");
                return;
            }

            String moduleText = joinParts(parts, 2, parts.length);
            Module module = findModule(moduleText);
            if (module == null) {
                info("Unknown module: " + moduleText);
                showModuleMatches(moduleText);
                return;
            }

            BINDS.remove(module.getName().toLowerCase(Locale.ROOT));
            info("Unbound " + module.getName());
            com.poorest.client.core.ClientConfigManager.save();
            return;
        }

        if (parts.length == 2) {
            event.setCanceled(true);
            showKeyHint(parts[1]);
            return;
        }

        String keyName = parts[parts.length - 1];
        StringBuilder moduleName = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            if (i > 1) moduleName.append(' ');
            moduleName.append(parts[i]);
        }

        Module module = findModule(moduleName.toString());
        if (module == null) {
            event.setCanceled(true);
            info("Unknown module: " + moduleName);
            return;
        }

        Integer code = parseKey(keyName);
        if (code == null) {
            event.setCanceled(true);
            info("Unknown key: " + keyName);
            return;
        }

        event.setCanceled(true);
        String id = module.getName().toLowerCase(Locale.ROOT);
        if (code == 0) {
            BINDS.remove(id);
            info("Unbound " + module.getName());
        } else {
            BINDS.put(id, code);
            info("Bound " + module.getName() + " to " + displayKey(code));
        }
        com.poorest.client.core.ClientConfigManager.save();
    }

    public static String bindLabel(Module module) {
        Integer code = BINDS.get(module.getName().toLowerCase(Locale.ROOT));
        return code == null ? "NONE" : displayKey(code);
    }

    private static boolean handleConfigCommand(ClientChatEvent event, String message) {
        String[] parts = message.split("\\s+");
        if (parts.length == 0 || !parts[0].equalsIgnoreCase(".config")) return false;
        event.setCanceled(true);

        if (parts.length == 1 || parts[1].equalsIgnoreCase("help")) {
            info("Usage: .config export <name> | .config import <name>");
            info("Configs are stored in config/poorest_client/exports/");
            List<String> exports = ClientConfigManager.exports();
            if (!exports.isEmpty()) info("Available: " + String.join(", ", exports));
            return true;
        }

        if (parts.length < 3) {
            info("Usage: .config export <name> | .config import <name>");
            return true;
        }

        String name = parts[2];
        if (parts[1].equalsIgnoreCase("export")) {
            if (ClientConfigManager.exportConfig(name)) info("Exported config: " + name + ".cfg");
            else info("Could not export config: " + name);
            return true;
        }

        if (parts[1].equalsIgnoreCase("import")) {
            if (ClientConfigManager.importConfig(name)) info("Imported config: " + name + ".cfg");
            else info("Config not found or invalid: " + name + ".cfg");
            return true;
        }

        info("Usage: .config export <name> | .config import <name>");
        return true;
    }

    private static void showHelp() {
        List<String> modules = new ArrayList<>();
        for (Module module : ModuleManager.getModules()) modules.add(module.getName());
        modules.sort(Comparator.comparing(String::toLowerCase));
        info("Usage: .bind <module> <key> | .bind <module> none | .unbind <module>");
        info("Config: .config export <name> | .config import <name>");
        info("Modules: " + String.join(", ", modules));
        info("Keys: A-Z, 0-9, F1-F12, SPACE, ENTER, TAB, SHIFT, CTRL, ALT, arrows, INSERT, DELETE, HOME, END, PAGEUP, PAGEDOWN, MOUSE1-MOUSE8");
        info("Example: .bind Fullbright G");
    }

    private static void showKeyHint(String moduleText) {
        Module module = findModule(moduleText);
        if (module == null) {
            info("Unknown module: " + moduleText);
            showModuleMatches(moduleText);
            return;
        }
        info(".bind " + module.getName() + " <key>");
        info("Current key: " + bindLabel(module));
        info("Keys: A-Z, 0-9, F1-F12, SPACE, ENTER, TAB, SHIFT, CTRL, ALT, UP, DOWN, LEFT, RIGHT, INSERT, DELETE, HOME, END, PAGEUP, PAGEDOWN, MOUSE1-MOUSE8, NONE");
    }

    private static void showModuleMatches(String query) {
        String normalized = normalize(query);
        List<String> matches = new ArrayList<>();
        for (Module module : ModuleManager.getModules()) {
            String name = normalize(module.getName());
            if (name.contains(normalized) || normalized.contains(name)) matches.add(module.getName());
        }
        matches.sort(Comparator.comparing(String::toLowerCase));
        if (!matches.isEmpty()) info("Did you mean: " + String.join(", ", matches));
        else info("Use .bind help to see all modules and keys.");
    }

    private static String joinParts(String[] parts, int from, int to) {
        StringBuilder result = new StringBuilder();
        for (int i = from; i < to; i++) {
            if (i > from) result.append(' ');
            result.append(parts[i]);
        }
        return result.toString();
    }

    private static Module findModule(String name) {
        Module direct = ModuleManager.getModule(name);
        if (direct != null) return direct;
        String normalized = normalize(name);
        for (Module module : ModuleManager.getModules()) {
            if (normalize(module.getName()).equals(normalized)) return module;
        }
        return null;
    }

    private static String normalize(String value) {
        return value.trim().replace('_', ' ').replace('-', ' ').replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static Integer parseKey(String raw) {
        String key = raw.toLowerCase(Locale.ROOT).trim();
        if (key.equals("none") || key.equals("clear") || key.equals("off")) return 0;
        if (key.startsWith("mouse")) {
            try {
                int button = Integer.parseInt(key.substring(5));
                if (button >= 1 && button <= 8) return mouseCode(button - 1);
            } catch (NumberFormatException ignored) {
            }
        }

        return switch (key) {
            case "space" -> GLFW.GLFW_KEY_SPACE;
            case "enter", "return" -> GLFW.GLFW_KEY_ENTER;
            case "tab" -> GLFW.GLFW_KEY_TAB;
            case "backspace" -> GLFW.GLFW_KEY_BACKSPACE;
            case "escape", "esc" -> GLFW.GLFW_KEY_ESCAPE;
            case "shift", "lshift" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "rshift" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "ctrl", "lctrl" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "rctrl" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "alt", "lalt" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "ralt" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "insert", "ins" -> GLFW.GLFW_KEY_INSERT;
            case "delete", "del" -> GLFW.GLFW_KEY_DELETE;
            case "home" -> GLFW.GLFW_KEY_HOME;
            case "end" -> GLFW.GLFW_KEY_END;
            case "pageup", "pgup" -> GLFW.GLFW_KEY_PAGE_UP;
            case "pagedown", "pgdown" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "up" -> GLFW.GLFW_KEY_UP;
            case "down" -> GLFW.GLFW_KEY_DOWN;
            case "left" -> GLFW.GLFW_KEY_LEFT;
            case "right" -> GLFW.GLFW_KEY_RIGHT;
            case "capslock" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "numlock" -> GLFW.GLFW_KEY_NUM_LOCK;
            case "f1" -> GLFW.GLFW_KEY_F1;
            case "f2" -> GLFW.GLFW_KEY_F2;
            case "f3" -> GLFW.GLFW_KEY_F3;
            case "f4" -> GLFW.GLFW_KEY_F4;
            case "f5" -> GLFW.GLFW_KEY_F5;
            case "f6" -> GLFW.GLFW_KEY_F6;
            case "f7" -> GLFW.GLFW_KEY_F7;
            case "f8" -> GLFW.GLFW_KEY_F8;
            case "f9" -> GLFW.GLFW_KEY_F9;
            case "f10" -> GLFW.GLFW_KEY_F10;
            case "f11" -> GLFW.GLFW_KEY_F11;
            case "f12" -> GLFW.GLFW_KEY_F12;
            default -> parseSimpleKey(key);
        };
    }

    private static Integer parseSimpleKey(String key) {
        if (key.length() == 1) {
            char c = key.charAt(0);
            if (c >= 'a' && c <= 'z') return GLFW.GLFW_KEY_A + (c - 'a');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }
        return null;
    }

    private static int mouseCode(int button) {
        return -1000 - button;
    }

    private static String displayKey(int code) {
        if (code <= -1000) return "MOUSE" + (-1000 - code + 1);
        return switch (code) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            default -> {
                String name = GLFW.glfwGetKeyName(code, 0);
                yield name == null ? Integer.toString(code) : name.toUpperCase(Locale.ROOT);
            }
        };
    }

    private static void info(String text) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.getChat().addMessage(Component.literal("§b[Poorest] §f" + text));
        }
    }
}
