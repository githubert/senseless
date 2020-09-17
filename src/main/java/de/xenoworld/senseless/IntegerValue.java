package de.xenoworld.senseless;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * A IntegerValue that holds some comparable number and contains mild statistics about it
 */
@JsonIgnoreProperties(value = {"value", "unformattedValue", "min", "max", "stale", "age", "timestamp"}, allowGetters = true)
public class IntegerValue extends Value {
    /**
     * The raw value.
     */
    @JsonProperty
    public Integer rawValue;

    /**
     * The raw maximum value.
     */
    @JsonProperty
    public Integer rawMax;

    /**
     * The raw minimum value.
     */
    @JsonProperty
    public Integer rawMin;

    public IntegerValue() {
        super(Instant.now(), Instant.now());
        operations.format("0.#"); // FIXME: This is currently the main difference between IntegerValue and DoubleValue…
    }

    public IntegerValue(int value) {
        this();
        value(value);
    }

    /**
     * Update the value.
     * <p>
     * This does several things, like updating timestamps, maximum, and minimum values.
     */
    public void value(int value) {
        if (rawMin == null || rawMin > value || stale(tsOfMinValue)) {
            rawMin = value;
            tsOfMinValue = Instant.now();
        }

        if (rawMax == null || rawMax < value || stale((tsOfMaxValue))) {
            rawMax = value;
            tsOfMaxValue = Instant.now();
        }

        timestamp = Instant.now();
        this.rawValue = value;
    }

    /**
     * Return the value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String value() {
        if (rawValue == null) {
            return null;
        }

        return operations.apply(Double.valueOf(rawValue));
    }

    /**
     * Return the value without applying the {@link java.text.DecimalFormat}.
     */
    @JsonProperty
    public Double unformattedValue() {
        if (rawValue == null) {
            return null;
        }

        var v = operations.applyWithoutFormat(Double.valueOf(rawValue));
        return v;
    }

    /**
     * Return the maximum value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String max() {
        if (rawValue == null) {
            return null;
        }

        return operations.apply(Double.valueOf(rawMax));
    }

    /**
     * Return the minimum value with all {@link ValueFormatter} operations applied.
     */
    @JsonProperty
    public String min() {
        if (rawValue == null) {
            return null;
        }

        return operations.apply(Double.valueOf(rawMin));
    }

    /**
     * Set the value from a String.
     */
    @Override
    public void value(String value) {
        value(Integer.parseInt(value));
    }
}
