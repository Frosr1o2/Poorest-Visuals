package com.poorest.client.core.setting;

public final class NumberSetting extends Setting<Double> {

    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(
            String name,
            String description,
            double defaultValue,
            double min,
            double max,
            double step
    ) {
        super(name, description, defaultValue);

        if (min > max) {
            throw new IllegalArgumentException("Minimum cannot be greater than maximum.");
        }

        if (step <= 0) {
            throw new IllegalArgumentException("Step must be greater than zero.");
        }

        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("Default value must be within range.");
        }

        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    protected boolean isValid(Double value) {
        return value != null && value >= min && value <= max;
    }

    public double getValueAsDouble() {
        return getValue();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
