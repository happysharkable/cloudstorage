package ru.happyshark.cloudstorage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import ru.happyshark.cloudstorage.library.LocalUtils;
import ru.happyshark.cloudstorage.library.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    ListView<String> leftPanel;

    @FXML
    ListView<String> rightPanel;

    @FXML
    GridPane connectionSetupPanel;

    @FXML
    TextField serverAddress;

    @FXML
    TextField serverPort;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    Label connectionFailedLabel;

    private Client client;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            client = new Client();
            leftPanel.getItems().addAll(LocalUtils.getFileListFromDirectory(Paths.get("./client-files")));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectAction(ActionEvent actionEvent) {

        String address = serverAddress.getText();
        int port = Integer.parseInt(serverPort.getText());
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            client.connect(this, address, port, login, password);
            updateCloudStorageFileList();
            connectionSetupPanel.setVisible(false);
            rightPanel.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitAction(ActionEvent actionEvent) {
        client.disconnect();
        Platform.exit();
    }

    public void copyFilePressed(ActionEvent actionEvent) {
        String srcFile;
        srcFile = "./client-files/" + leftPanel.getSelectionModel().getSelectedItem();
        try {
            client.sendFile(Paths.get(srcFile));
            updateCloudStorageFileList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCloudStorageFileList() {
        NetworkUtils.sendCommand("/list", Network.getInstance().getCurrentChannel());
    }

    public void setCloudStorageFileList(String files) {
        Platform.runLater(() -> {
            rightPanel.getItems().clear();
            rightPanel.getItems().addAll(files.split(";"));
        });
    }
}
