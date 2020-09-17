package de.xenoworld.senseless;

import org.junit.jupiter.api.Test;

import static de.xenoworld.senseless.ValueFormatter.Round.*;
import static org.assertj.core.api.Assertions.assertThat;

class DoubleValueTest {

    @Test
    void testMinMax() {
        var value = new DoubleValue(12.50);

        assertThat(value.rawMax).isEqualTo(12.50);
        assertThat(value.rawMin).isEqualTo(12.50);

        value.value(24.75);

        assertThat(value.rawMax).isEqualTo(24.75);
        assertThat(value.rawMin).isEqualTo(12.50);

        value.value(6.25);

        assertThat(value.rawMax).isEqualTo(24.75);
        assertThat(value.rawMin).isEqualTo(6.25);
    }

    @Test
    void testValueOperations() {
        var value = new DoubleValue(12.50);
        assertThat(value.value()).isEqualTo("12.5");

        value.operations().offset(-1.0);
        assertThat(value.value()).isEqualTo("11.5");

        value.operations().scale(2.5);
        assertThat(value.value()).isEqualTo("30.25");

        value.operations().round(CEIL);
        assertThat(value.value()).isEqualTo("31.0");

        value.operations().round(FLOOR);
        assertThat(value.value()).isEqualTo("30.0");

        value.operations().round(ROUND);
        assertThat(value.value()).isEqualTo("30.0");

        value.operations().round(NONE);
        assertThat(value.value()).isEqualTo("30.25");

        value.operations().offset(-0.99);
        value.operations().format("Sometext #.#");
        assertThat(value.value()).isEqualTo("Sometext 30.3");

        value.operations().format("0000.000");
        assertThat(value.value()).isEqualTo("0030.260");
    }
}