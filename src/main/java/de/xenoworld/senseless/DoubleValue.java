package de.xenoworld.senseless;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * A DoubleValue that holds some comparable number and contains mild statistics about it
 */
@JsonIgnoreProperties(value = {"value", "unformattedValue", "min", "max", "stale", "age", "timestamp"}, allowGetters = true)
public class DoubleValue extends Value {
    /**
     * The raw value.
     */
    @JsonProperty
    public Double rawValue;

    /**
     * The raw maximum value.
     */
    @JsonProperty
    public Double rawMax;

    /**
     * The raw minimum value.
     */
    @JsonProperty
    public Double rawMin;

    public DoubleValue() {
        super(Instant.now(), Instant.now());
    }

    public DoubleValue(double value) {
        this();
        value(value);
    }

    /**
     * Update the value.
     * <p>
     * This does several things, like updating timestamps, maximum, and minimum values.
     */
    public void value(double value) {
        if (rawMin == null || rawMin > value || stale(tsOfMinValue)) {
            rawMin = value;
            tsOfMinValue = Instant.now();
        }

        if (rawMax == null || rawMax < value || stale(tsOfMaxValue)) {
            rawMax = value;
            tsOfMaxValue = Instant.now();
        }

        this.timestamp = Instant.now();
        this.rawValue = value;
    }

    /**
     * Return the value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String value() {
        return operations.apply(rawValue);
    }

    /**
     * Return the value without applying the {@link java.text.DecimalFormat}.
     */
    @JsonProperty
    public Double unformattedValue() {
        return operations.applyWithoutFormat(rawValue);
    }

    /**
     * Return the maximum value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String max() {
        return operations.apply(rawMax);
    }

    /**
     * Return the minimum value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String min() {
        return operations.apply(rawMin);
    }

    /**
     * Set the value from a String.
     */
    @Override
    public void value(String value) {
        value(Double.parseDouble(value));
    }
}
