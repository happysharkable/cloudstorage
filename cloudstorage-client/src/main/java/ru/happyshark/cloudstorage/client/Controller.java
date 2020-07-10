package ru.happyshark.cloudstorage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import ru.happyshark.cloudstorage.library.LocalUtils;
import ru.happyshark.cloudstorage.library.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
        client = new Client();
        updateClientStorageFileList();
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
        // копирование файла на сервер
        if (leftPanel.isFocused()) {
            try {
                String srcFile = "./client-files/" + leftPanel.getSelectionModel().getSelectedItem();
                client.sendFile(Paths.get(srcFile));
                updateCloudStorageFileList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // копирование файла с сервера
        if (rightPanel.isFocused()) {
            try {
                String requestedFile = rightPanel.getSelectionModel().getSelectedItem();
                client.requestFile(requestedFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // обновление списка файлов клиента
    public void updateClientStorageFileList() {
        Platform.runLater(() -> {
            try {
                leftPanel.getItems().clear();
                leftPanel.getItems().addAll(LocalUtils.getFileListFromDirectory(Paths.get("./client-files")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // запрос на обновление списка файлов на сервере
    public void updateCloudStorageFileList() {
        NetworkUtils.sendCommand("/list", Network.getInstance().getCurrentChannel());
    }

    // обновление списка файлов сервера
    public void setCloudStorageFileList(String files) {
        Platform.runLater(() -> {
            rightPanel.getItems().clear();
            rightPanel.getItems().addAll(files.split(";"));
        });
    }

    public void moveFilePressed(ActionEvent actionEvent) {
        //TODO
    }

    public void deleteFilePressed(ActionEvent actionEvent) {
        if (leftPanel.isFocused()) {
            try {
                Files.delete(Paths.get("./client-files/" + leftPanel.getSelectionModel().getSelectedItem()));
                updateClientStorageFileList();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to delete file " + leftPanel.getSelectionModel().getSelectedItem(),
                        ButtonType.OK).showAndWait();
            }
        }

        if (rightPanel.isFocused()) {
            NetworkUtils.sendCommand("/delete " + rightPanel.getSelectionModel().getSelectedItem(), Network.getInstance().getCurrentChannel());
        }
    }
}
