package de.xenoworld.senseless.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration of a collection.
 */
public class CollectionConfig {
    @JsonProperty
    public String path;

    @JsonProperty
    public String writeToken;

    @JsonProperty
    public String readToken;

    @JsonProperty
    public List<ValueConfig> valueConfig;
}
