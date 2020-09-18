package de.xenoworld.senseless;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.xenoworld.senseless.config.ValueConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DoubleValue.class),
        @JsonSubTypes.Type(value = IntegerValue.class),
})
public abstract class Value {
    /**
     * Timestamp of last update of the maximum value.
     */
    Instant tsOfMaxValue;

    /**
     * Timestamp of the last update of the minimum value.
     */
    Instant tsOfMinValue;

    @JsonProperty
    Instant timestamp;

    @JsonProperty
    int lifetime = -1;

    ValueFormatter operations = new ValueFormatter();

    public Value(Instant tsOfMinValue, Instant tsOfMaxValue) {
        this.tsOfMinValue = tsOfMinValue;
        this.tsOfMaxValue = tsOfMaxValue;
        this.timestamp = Instant.now();
    }

    /**
     * Check if the given {@link Instant} is older than `24` hours.
     */
    static boolean stale(Instant instant) {
        return Instant.now().minus(24, ChronoUnit.HOURS).isAfter(instant);
    }

    /**
     * Check if the given {@link Instant} is older than `amount` `chronoUnit`.
     */
    static boolean stale(Instant instant, int amount, ChronoUnit chronoUnit) {
        return Instant.now().minus(amount, chronoUnit).isAfter(instant);
    }

    /**
     * Return display value transformation operations.
     */
    public ValueFormatter operations() {
        return operations;
    }

    public void operations(ValueConfig valueConfig) {
        operations.format(valueConfig.format);
        operations.scale(valueConfig.scale);
        operations.offset(valueConfig.offset);
        operations.round(valueConfig.round);
    }

    @JsonProperty
    public boolean isStale() {
        return lifetime == -1 ? false : stale(timestamp, lifetime, ChronoUnit.SECONDS);
    }

    @JsonProperty
    public long age() {
        return Instant.now().getEpochSecond() - timestamp.getEpochSecond();
    }

    public abstract void value(String value);
}
