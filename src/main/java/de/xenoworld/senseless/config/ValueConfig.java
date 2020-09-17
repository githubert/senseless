package de.xenoworld.senseless.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.xenoworld.senseless.ValueFormatter;

/**
 * Configuration of a value in a collection. This is used to set formatting and other things like the lifetime on a
 * value.
 */
public class ValueConfig {
    public enum Type {INTEGER, DOUBLE,}

    @JsonProperty
    public Integer lifetime;

    @JsonProperty
    public Type type = Type.DOUBLE;

    @JsonProperty
    public ValueFormatter.Round round = ValueFormatter.Round.NONE;

    /**
     * The path relative to the parent path of the collection. Say the collection is behind the path "/weather", then
     * a path of "pressure" for the value will end up being "/weather/pressure".
     */
    @JsonProperty
    public String path;

    @JsonProperty
    public Double offset;

    @JsonProperty
    public Double scale;

    @JsonProperty
    public String format;
}
