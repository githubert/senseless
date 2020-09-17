package de.xenoworld.senseless;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class defines a collection of values that are combined beneath the given prefix path.
 */
public class Collection {
    private String prefix;
    private String writeToken;
    private String readToken;
    private Map<String, Value> values;
    private ValueFormatter defaultValueFormatter = new ValueFormatter();

    @JsonCreator
    public Collection(@JsonProperty("prefix") String prefix) {
        values = new HashMap<>();
        this.prefix = prefix;
    }

    public void defaultValueOperations(ValueFormatter valueFormatter) {
        this.defaultValueFormatter = valueFormatter;
    }

    public ValueFormatter defaultValueOperations() {
        return defaultValueFormatter;
    }

    @JsonProperty
    public String prefix() {
        return prefix;
    }

    public void valueUsingDefaults(String path, Value value) {
        value.operations = new ValueFormatter(defaultValueFormatter);
        values.put(path, value);
    }

    public void value(String path, Value value) {
        values.put(path, value);
    }

    public Value value(String path) {
        return values.get(path);
    }

    public void writeToken(String writeToken) {
        this.writeToken = writeToken;
    }

    public Optional<String> writeToken() {
        return Optional.ofNullable(writeToken);
    }

    public void readToken(String readToken) {
        this.readToken = readToken;
    }

    public Optional<String> readToken() {
        return Optional.ofNullable(readToken);
    }

    @JsonProperty
    public Map<String, Value> values() {
        return values;
    }

}
