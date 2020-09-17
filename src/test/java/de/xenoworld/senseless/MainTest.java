package de.xenoworld.senseless;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    void stripTrailingSlashes() {
        var path = "/some/path//";

        assertThat(Main.stripTrailingSlashes(path)).isEqualTo("/some/path");
    }

    @Test
    void mostSpecific() {
        Map<String, Collection> map = new HashMap<>();

        map.put("/some", new Collection("/some"));
        var result = Main.mostSpecific(map, "/some/path");

        assertThat(result.prefix()).isEqualTo("/some");

        map.put("/some/path", new Collection("/some/path"));

        result = Main.mostSpecific(map, "/some/path");
        assertThat(result.prefix()).isEqualTo("/some/path");

        result = Main.mostSpecific(map, "/some/path/very/specific");
        assertThat(result.prefix()).isEqualTo("/some/path");
    }
}