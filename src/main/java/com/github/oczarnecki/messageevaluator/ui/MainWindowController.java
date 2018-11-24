package com.github.oczarnecki.messageevaluator.ui;

import com.github.oczarnecki.messageevaluator.TelegramDataModel;
import com.github.oczarnecki.messageevaluator.importer.telegram.ImportException;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public final class MainWindowController {
    @FXML
    private MessageHistogramController histogramController;

    private final Stage stage;
    private TelegramDataModel tgModel;

    public MainWindowController(Stage stage, TelegramDataModel tgModel) {
        this.stage = stage;
        this.tgModel = tgModel;
    }

    @FXML
    private void initialize() {
        tgModel.addChangeListener(histogramController::chatsChanged);
    }

    @FXML
    private void importTgData() throws ImportException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Json", "*.json"),
                new FileChooser.ExtensionFilter("All files", "*.*"));
        fileChooser.setTitle("Please choose a telegram export file to import from...");
        File dataFile = fileChooser.showOpenDialog(stage);
        if (dataFile != null) {
            tgModel.importTgData(dataFile);
        }
    }
}
