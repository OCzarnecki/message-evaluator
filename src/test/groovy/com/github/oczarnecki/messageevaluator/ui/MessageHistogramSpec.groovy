package com.github.oczarnecki.messageevaluator.ui


import com.github.oczarnecki.messageevaluator.TelegramDataModel
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.testfx.api.FxToolkit
import org.testfx.framework.spock.ApplicationSpec
import spock.lang.Subject

class MessageHistogramSpec extends ApplicationSpec {

    @Subject
    MessageHistogramController controller

    TelegramDataModel model

    @Override
    void init() throws Exception {
        model = new TelegramDataModel()
        FxToolkit.registerStage { new Stage() }
    }

    @Override
    void start(Stage stage) {
        def url = getClass().getClassLoader().getResource('fxml/MessageHistogram.fxml')
        def loader = new FXMLLoader(url as URL)
        stage.setScene(new Scene(loader.load() as Parent))
        controller = loader.getController()
        model.addChangeListener({ chatModel -> controller.chatsChanged(chatModel) })
        stage.show()
    }

    @Override
    void stop() throws Exception {
        FxToolkit.hideStage()
    }

    def "the correct checkboxes are shown"() {
        given: 'the checkbox container'
        Pane checkBoxes = lookup('#chartSelectionBox').query()


        when: 'the application starts'

        then: 'no checkboxes are present'
        checkBoxes.getChildren().isEmpty()


        when: 'data are loaded'
        Platform.runLater {
            model.importTgData(new File(getClass().getClassLoader().getResource('telegramTestData/twoChats.json').toURI()))
        }
        Thread.sleep(10)

        then: 'the correct checkboxes appear'
        checkBoxes.getChildren().stream()
                .map({ cb -> ((CheckBox) cb).getText() })
                .allMatch({ text -> ['Chat 1', 'Chat 2'].contains(text) })

        and: 'they are checked by default'
        checkBoxes.getChildren().stream()
                .allMatch({ cb -> ((CheckBox) cb).isSelected() })


        when: 'the deselection button is clicked'
        clickOn('Select none')

        then: 'no checkboxes are selected'
        checkBoxes.getChildren().stream()
                .allMatch({ cb -> !((CheckBox) cb).isSelected() })


        when: 'the selections button is clicked'
        clickOn('Select all')

        then: 'all checkboxes are selected again'
        checkBoxes.getChildren().stream()
                .allMatch( {cb -> ((CheckBox)cb).isSelected()} )


        when: 'the data are unloaded'
        Platform.runLater {
            model.setChats(Collections.emptyList())
        }
        Thread.sleep(10)

        then: 'the checkboxes are removed'
        checkBoxes.getChildren().isEmpty()
    }
}
