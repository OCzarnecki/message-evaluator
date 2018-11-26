package com.github.oczarnecki.messageevaluator.ui;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;

public enum TimeUnit {
    AUTOMATIC("Automatic", null),
    DAY("Day", ChronoField.EPOCH_DAY),
    MONTH("Month", ChronoField.PROLEPTIC_MONTH),
    YEAR("Year", ChronoField.YEAR);

    private final String display;
    private TemporalField chronoField;

    TimeUnit(String display, TemporalField chronoField) {
        this.display = display;
        this.chronoField = chronoField;
    }

    @Override
    public String toString() {
        return display;
    }

    public TemporalField getChronoField() {
        return chronoField;
    }
}
