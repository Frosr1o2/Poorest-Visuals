package com.poorest.client.core.setting;

public abstract class Setting<T> {

    private final String name;
    private final String description;

    private T value;

    protected Setting(
            String name,
            String description,
            T defaultValue
    ) {
        this.name = name;
        this.description = description;
        this.value = defaultValue;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final T getValue() {
        return value;
    }

    public final void setValue(T value) {
        if (!isValid(value)) {
            return;
        }

        this.value = value;
        onChanged(value);
    }

    protected boolean isValid(T value) {
        return value != null;
    }

    protected void onChanged(T value) {
    }
}
