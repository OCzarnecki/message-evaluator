package com.github.oczarnecki.messageevaluator.ui;

import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.scene.chart.Axis;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class DateTimeAxis extends Axis<LocalDateTime> {
    public static final LocalDateTime ZERO_DATE = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
    private LocalDateTime min = LocalDateTime.of(2017, 8, 10, 16, 0);
    private LocalDateTime max = LocalDateTime.now();

    private Property<LocalDateTime> lowerBound = new ObjectPropertyBase<LocalDateTime>(min) {
        /**
         * The method {@code invalidated()} can be overridden to receive
         * invalidation notifications. This is the preferred option in
         * {@code Objects} defining the property, because it requires less memory.
         * <p>
         * The default implementation is empty.
         */
        @Override
        protected void invalidated() {
            if (!isAutoRanging()) {
                invalidateRange();
                requestAxisLayout();
            }
        }

        @Override
        public Object getBean() {
            return DateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "lowerBound";
        }
    };
    private Property<LocalDateTime> upperBound = new ObjectPropertyBase<LocalDateTime>(max) {
        @Override
        public Object getBean() {
            return DateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "upperBound";
        }
    };
    private Property<TimeUnit> timeUnit = new ObjectPropertyBase<TimeUnit>(TimeUnit.MONTH) {
        @Override
        public Object getBean() {
            return DateTimeAxis.this;
        }

        @Override
        public String getName() {
            return "timeUnit";
        }
    };
    private double scale = 1;

    /**
     * This calculates the upper and lower bound based on the data provided to invalidateRange() method. This must not
     * effect the state of the axis, changing any properties of the axis. Any results of the auto-ranging should be
     * returned in the range object. This will we passed to setRange() if it has been decided to adopt this range for
     * this axis.
     *
     * @param length The length of the axis in screen coordinates
     * @return Range information, this is implementation dependent
     */
    @Override
    protected Object autoRange(double length) {
        if (isAutoRanging()) {

        } else {
            return getRange();
        }
    }

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range   A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
    @Override
    protected void setRange(Object range, boolean animate) {
        if (animate) {
            throw new UnsupportedOperationException("Animation of DateTimeAxis is not supported");
        }
    }

    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
    @Override
    protected Object getRange() {
        return null;
    }

    /**
     * Get the display position of the zero line along this axis.
     *
     * @return display position or Double.NaN if zero is not in current range;
     */
    @Override
    public double getZeroPosition() {
        if (ZERO_DATE.compareTo(getLowerBound()) < 0 || getUpperBound().compareTo(ZERO_DATE) < 0) {
            return Double.NaN;
        } else {
            return getDisplayPosition(ZERO_DATE);
        }
    }

    /**
     * Get the display position along this axis for a given value
     *
     * @param value The data value to work out display position for
     * @return display position or Double.NaN if zero is not in current range;
     */
    @Override
    public double getDisplayPosition(LocalDateTime value) {
        long valueAsNumber = value.getLong(ChronoField.INSTANT_SECONDS);
        long lowerBoundAsNumber = getLowerBound().getLong(ChronoField.INSTANT_SECONDS);
        return (valueAsNumber - lowerBoundAsNumber) * scale;
    }

    /**
     * Get the data value for the given display position on this axis. If the axis
     * is a CategoryAxis this will be the nearest value.
     *
     * @param displayPosition A pixel position on this axis
     * @return the nearest data value to the given pixel position or
     * null if not on axis;
     */
    @Override
    public LocalDateTime getValueForDisplay(double displayPosition) {
        return null;
    }

    /**
     * Checks if the given value is plottable on this axis
     *
     * @param value The value to check if its on axis
     * @return true if the given value is plottable on this axis
     */
    @Override
    public boolean isValueOnAxis(LocalDateTime value) {
        return false;
    }

    /**
     * All axis values must be representable by some numeric value. This gets the numeric value for a given data value.
     *
     * @param value The data value to convert
     * @return Numeric value for the given data value
     */
    @Override
    public double toNumericValue(LocalDateTime value) {
        return value.getLong(ChronoField.INSTANT_SECONDS);
    }

    /**
     * All axis values must be representable by some numeric value. This gets the data value for a given numeric value.
     *
     * @param value The numeric value to convert
     * @return Data value for given numeric value
     */
    @Override
    public LocalDateTime toRealValue(double value) {
        return LocalDateTime.ofEpochSecond(
                Math.round(value),
                (int) (Math.round(value * 1e9) % 1_000_000_000),
                ZoneOffset.UTC);
    }

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range  A range object returned from autoRange()
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override
    protected List<LocalDateTime> calculateTickValues(double length, Object range) {
        return null;
    }

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     * @return A formatted string for the given value
     */
    @Override
    protected String getTickMarkLabel(LocalDateTime value) {
        return null;
    }

    /**
     * Called when data has changed and the range may not be valid any more. This is only called by the chart if
     * isAutoRanging() returns true. If we are auto ranging it will cause layout to be requested and auto ranging to
     * happen on next layout pass.
     *
     * @param data The current set of all data that needs to be plotted on this axis
     */
    @Override
    public void invalidateRange(List<LocalDateTime> data) {
        if (data.isEmpty()) {
            min = getLowerBound();
            max = getUpperBound();
        } else {
            min = LocalDateTime.MIN;
            max = LocalDateTime.MAX;
        }
        for(LocalDateTime date : data) {
            if (min.compareTo(date) > 0) {
                min = date;
            }
            if (date.compareTo(max) > 0) {
                max = date;
            }
        }
        super.invalidateRange(data);
    }

    public LocalDateTime getLowerBound() {
        return lowerBound.getValue();
    }

    public LocalDateTime getUpperBound() {
        return upperBound.getValue();
    }
}
