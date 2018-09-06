package com.humanharvest.organz.controller.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.humanharvest.organz.touch.MultitouchHandler;

public class TouchAlertController {

    private final ObjectProperty<Boolean> resultProperty = new SimpleObjectProperty<>();

    @FXML
    private Text title;
    @FXML
    private Text body;
    @FXML
    private Button cancelButton;
    @FXML
    private Pane pageHolder;

    private Stage stage;
    private Pane pane;

    @FXML
    public void initialize() {
        pageHolder.getStyleClass().add("window");
    }

    public void setup(Alert.AlertType alertType, String title, String body, Stage stage, Pane pane) {
        this.title.setText(alertType + ": " + title);
        this.body.setText(body);
        this.stage = stage;
        this.pane = pane;

        if (alertType != Alert.AlertType.CONFIRMATION) {
            cancelButton.setVisible(false);
            cancelButton.setManaged(false);
        }
    }

    public Property<Boolean> getResultProperty() {
        return resultProperty;
    }

    @FXML
    private void ok() {
        resultProperty.setValue(true);
        MultitouchHandler.removePane(pane);
        stage.close();
    }

    @FXML
    private void cancel() {
        resultProperty.setValue(false);
        MultitouchHandler.removePane(pane);
        stage.close();
    }
}
