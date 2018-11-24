package com.github.oczarnecki.messageevaluator;

import com.github.oczarnecki.messageevaluator.importer.telegram.ImportException;
import com.github.oczarnecki.messageevaluator.ui.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


public class MessageEvaluator extends Application {

    private TelegramDataModel tgModel = new TelegramDataModel();

    @Override
    public void start(Stage stage) throws IOException {
        processProgramArguments(tgModel);
        prepareUI(stage);
    }

    private void prepareUI(Stage stage) throws IOException {
        MainWindowController mainWindowController = new MainWindowController(stage, tgModel);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(ResourcePaths.MAIN_WINDOW_FXML));
        loader.setController(mainWindowController);

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(ResourcePaths.MAIN_STYLESHEET);

        stage.setScene(scene);
        stage.show();
    }

    private void processProgramArguments(TelegramDataModel tgModel) {
        if (!getParameters().getUnnamed().isEmpty()) {
            try {
                File dataFile = Paths.get(getParameters().getUnnamed().get(0)).toFile();
                tgModel.importTgData(dataFile);
            } catch (ImportException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Entry point to the application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
