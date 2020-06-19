package ru.happyshark.cloudstorage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

public class Controller {

    @FXML
    ListView leftPanel;

    @FXML
    ListView rightPanel;

    @FXML
    GridPane connectionSetupPanel;

    public void exitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void connectAction(ActionEvent actionEvent) {
        connectionSetupPanel.setVisible(false);
        rightPanel.setVisible(true);
    }
}
