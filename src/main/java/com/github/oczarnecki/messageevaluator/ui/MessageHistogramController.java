package com.github.oczarnecki.messageevaluator.ui;

import com.github.oczarnecki.messageevaluator.TelegramDataModel;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramChat;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.EPOCH_DAY;

public class MessageHistogramController {
    @FXML
    private XYChart<Number, Number> histogramChart;

    @FXML
    private VBox chartSelectionBox;

    private Map<String, XYChart.Series<Number, Number>> chatSeries;

    /**
     * Constructor
     */
    public MessageHistogramController() {
        chatSeries = new HashMap<>();
    }

    @FXML
    private void initialize() {
        histogramChart.setAnimated(false);
    }

    public void selectAll() {
        setCheckBoxesSelected(true);
    }

    public void deselectAll() {
        setCheckBoxesSelected(false);
    }

    private void setCheckBoxesSelected(boolean selected) {
        chartSelectionBox.getChildren().forEach(checkBox -> ((CheckBox) checkBox).setSelected(selected));
    }

    public void chatsChanged(TelegramDataModel tgModel) {
        chartSelectionBox.getChildren().clear();
        chatSeries.clear();

        Collection<TelegramChat> chats = tgModel.getChats();
        chartSelectionBox.getChildren().addAll(
                chats.stream()
                        .map(this::chartCheckboxChanged)
                        .collect(Collectors.toList()));

        chats.forEach(chat -> chatSeries.put(chat.getName(), toDataSeries(chat)));
        selectAll();
    }

    private CheckBox chartCheckboxChanged(TelegramChat chat) {
        CheckBox checkBox = new CheckBox(chat.getName());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            XYChart.Series<Number, Number> series = chatSeries.get(checkBox.getText());
            if (newValue) {
                histogramChart.getData().add(series);
            } else {
                histogramChart.getData().remove(series);
            }
        });
        return checkBox;
    }

    private XYChart.Series<Number, Number> toDataSeries(TelegramChat telegramChat) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(telegramChat.getName());
        Map<Long, Double> histogram = new HashMap<>();
        telegramChat.getMessages().forEach(message -> {
            long days = message.getTimestamp().getLong(EPOCH_DAY) / 30;
            histogram.put(days, 1 + histogram.getOrDefault(days, 0.0));
        });
        histogram.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, (value))));
        return series;
    }
}
