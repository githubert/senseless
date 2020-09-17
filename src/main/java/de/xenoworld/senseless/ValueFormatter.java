package de.xenoworld.senseless;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

/**
 * This class enables several transformations of values in order to have a nicely formatted value for displaying.
 */
public class ValueFormatter {
    public ValueFormatter() {
    }

    private Round round = Round.NONE;

    private Double offset;

    private Double scale;

    private DecimalFormat format;

    public ValueFormatter(ValueFormatter copyFrom) {
        // FIXME: HOw to copy these things? Clone? I don't know!
        this.round = copyFrom.round;
        this.offset = copyFrom.offset;
        this.scale = copyFrom.scale;
        this.format = copyFrom.format;
    }

    /**
     * Add the given format string through {@link DecimalFormat}
     */
    public void format(String format) {
        if (format == null) {
            return;
        }

        this.format = new DecimalFormat(format);
    }

    /**
     * Scale the value by the given factor.
     */
    public void scale(Double scale) {
        this.scale = scale;
    }

    public boolean hasFormat() {
        return format == null;
    }

    /**
     * Add the given offset to the value.
     */
    public void offset(Double offset) {
        this.offset = offset;
    }

    /**
     * Add the given rounding operation to the value. See {@link Round} for more details.
     */
    public void round(Round round) {
        this.round = round;
    }

    /**
     * Apply all enabled transformations to the given value.
     */
    public String apply(Double d) {
        d = applyWithoutFormat(d);

        if (d == null) return null;

        if (format != null) {
            return format.format(d);
        } else {
            return String.valueOf(d);
        }
    }

    /**
     * Apply all enabled transformations to the given value, except for the format string.
     */
    @Nullable
    public Double applyWithoutFormat(Double d) {
        if (d == null) {
            return null;
        }

        if (scale != null) {
            d *= scale;
        }

        if (offset != null) {
            d += offset;
        }

        switch (round) {
            case CEIL:
                d = Math.ceil(d);
            case FLOOR:
                d = Math.floor(d);
            case ROUND:
                d = (double) Math.round(d);
        }
        return d;
    }

    /**
     * The type of rounding operation to apply.
     */
    public enum Round {
        /**
         * Use {@link Math#ceil(double)}.
         */
        CEIL,

        /**
         * Use {@link Math#floor(double)}.
         */
        FLOOR,

        /**
         * Use {@link Math#round(float)}.
         */
        ROUND,

        /**
         * Do not apply any rounding to the value.
         */
        NONE;
    }
}
