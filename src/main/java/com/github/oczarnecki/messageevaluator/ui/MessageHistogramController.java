package com.github.oczarnecki.messageevaluator.ui;

import com.github.oczarnecki.messageevaluator.TelegramDataModel;
import com.github.oczarnecki.messageevaluator.importer.telegram.TelegramChat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageHistogramController {
    @FXML
    private BorderPane histogramPane;

    /**
     * Container of all the check boxes used to de-/select a chart for display
     */
    @FXML
    private VBox chartSelectionControlBox;

    @FXML
    private ComboBox<TimeUnit> histogramUnitComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private HistogramChartController histogramChartController;

    /**
     * Maps a chat's name to its data series
     */
    private Map<String, XYChart.Series<Number, Number>> chatSeries;

    private TelegramDataModel telegramDataModel;

    /**
     * Constructor
     */
    public MessageHistogramController() {
        chatSeries = new HashMap<>();
    }

    @FXML
    private void initialize() {
        histogramUnitComboBox.setItems(FXCollections.observableArrayList(TimeUnit.values()));
        histogramUnitComboBox.setValue(TimeUnit.AUTOMATIC);
        histogramUnitComboBox.valueProperty().addListener((ignored) -> recalculateChartSeries());

        fromDatePicker.valueProperty().addListener((ignored) -> adjustBounds());
        toDatePicker.valueProperty().addListener((ignored) -> adjustBounds());
    }

    @FXML
    private void selectAll() {
        setCheckBoxesSelected(true);
    }

    @FXML
    private void deselectAll() {
        setCheckBoxesSelected(false);
    }

    @FXML
    private void maximalRange() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        adjustBounds();
    }

    private void setCheckBoxesSelected(boolean selected) {
        getChartSelectionCheckBoxes().forEach(checkBox -> checkBox.setSelected(selected));
    }

    /**
     * Called when the DataModel changes.
     *
     * @param tgModel the data model, in its new state
     */
    public void chatsChanged(TelegramDataModel tgModel) {
        this.telegramDataModel = tgModel;

        // recalculate data series
        recalculateChartSeries();

        // recreate check boxes
        getChartSelectionCheckBoxes().clear();
        Collection<TelegramChat> chats = telegramDataModel.getChats();
        getChartSelectionCheckBoxes().addAll(chats.stream()
                        .map(this::createCheckboxForChat)
                        .collect(Collectors.toList()));

        histogramPane.setDisable(chats.isEmpty());
    }

    private ObservableList<CheckBox> getChartSelectionCheckBoxes() {
        //noinspection unchecked
        return (ObservableList<CheckBox>) (ObservableList<?>) chartSelectionControlBox.getChildren();
    }

    private void adjustBounds() {
        if (fromDatePicker.getValue() == null) {
            fromDatePicker.setValue(telegramDataModel.getEarliestEntryTimestamp().toLocalDate());
        }
        if (toDatePicker.getValue() == null) {
            toDatePicker.setValue(telegramDataModel.getLatestEntryTimestamp().toLocalDate());
        }
        histogramChartController.setBounds(fromDatePicker.getValue().atTime(0, 0),
                toDatePicker.getValue().atTime(23, 59, 59));
    }

    private CheckBox createCheckboxForChat(TelegramChat chat) {
        CheckBox checkBox = new CheckBox(chat.getName());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            XYChart.Series<Number, Number> series = chatSeries.get(checkBox.getText());
            if (newValue) {
                histogramChart.getData().add(series);
            } else {
                histogramChart.getData().remove(series);
            }
        });
        checkBox.setSelected(true);
        return checkBox;
    }

    private void recalculateChartSeries() {
        histogramChart.getData().clear();

        adjustBounds();

        Collection<TelegramChat> chats = telegramDataModel.getChats();
        chats.forEach(chat -> chatSeries.put(chat.getName(), toDataSeries(chat)));

        getChartSelectionCheckBoxes().stream()
                .filter(CheckBox::isSelected)
                .forEach(checkBox -> {
            checkBox.setSelected(false);
            checkBox.setSelected(true);
        });
    }

    private XYChart.Series<Number, Number> toDataSeries(TelegramChat telegramChat) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(telegramChat.getName());

        Map<Long, Integer> histogram = new HashMap<>();
        for (long unixIndex = getUnixIndex(telegramDataModel.getEarliestEntryTimestamp());
             unixIndex <= getUnixIndex(telegramDataModel.getLatestEntryTimestamp());
             unixIndex++) {
            histogram.put(unixIndex, 0);
        }

        telegramChat.getMessages().forEach(message -> {
            long index = getUnixIndex(message.getTimestamp());
            histogram.put(index, 1 + histogram.get(index));
        });
        histogram.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, (value))));
        return series;
    }

    // TODO set histogram unit in chart
}
