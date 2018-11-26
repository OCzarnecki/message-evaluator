package com.github.oczarnecki.messageevaluator.ui;

import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class HistogramChartController {
    @FXML
    private XYChart<Number, Number> histogramChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private TimeUnit unit;

    public HistogramChartController() {
        unit = TimeUnit.AUTOMATIC;
    }

    void setBounds(LocalDateTime lower, LocalDateTime upper) {
        xAxis.setLowerBound(getUnixIndex(lower));
        xAxis.setUpperBound(getUnixIndex(upper));
    }

    private long getUnixIndex(LocalDateTime time) {
        return time.getLong(getEffectiveHistogramUnit().getChronoField());
    }

    private TimeUnit getEffectiveHistogramUnit() {
        if (unit.equals(TimeUnit.AUTOMATIC)) {
            long timeInterval = ChronoUnit.DAYS.between(xAxis.getLowerBound(), xAxis.getUpperBound());
            if (timeInterval < 5 * 30) {
                return  TimeUnit.DAY;
            } else if (timeInterval < 5 * 365) {
                return TimeUnit.MONTH;
            } else {
                return TimeUnit.YEAR;
            }
        } else {
            return unit;
        }
    }
}
