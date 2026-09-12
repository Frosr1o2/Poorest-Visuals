package com.poorest.client.core.setting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ModeSetting extends Setting<String> {

    private final List<String> modes;

    public ModeSetting(
            String name,
            String description,
            String defaultValue,
            String... modes
    ) {
        super(name, description, defaultValue);

        if (modes.length == 0) {
            throw new IllegalArgumentException("Mode setting must have at least one mode.");
        }

        this.modes = Collections.unmodifiableList(Arrays.asList(modes));

        if (!this.modes.contains(defaultValue)) {
            throw new IllegalArgumentException(
                    "Default value must be one of the available modes."
            );
        }
    }

    @Override
    protected boolean isValid(String value) {
        return value != null && modes.contains(value);
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return getValue().equalsIgnoreCase(mode);
    }

    public void cycle() {
        int currentIndex = modes.indexOf(getValue());
        int nextIndex = (currentIndex + 1) % modes.size();

        setValue(modes.get(nextIndex));
    }
}
