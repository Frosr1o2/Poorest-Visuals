package com.poorest.client.core.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {

    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {
    }

    public static void register(Module module) {
        MODULES.add(module);
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static Module getModule(String name) {
        return MODULES.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static List<Module> getModules(ModuleCategory category) {
        return MODULES.stream()
                .filter(module -> module.getCategory() == category)
                .toList();
    }

    public static void onTick() {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public static void onRender() {
        for (Module module : MODULES) {
            if (module.isEnabled()) {
                module.onRender();
            }
        }
    }
}
