package ru.happyshark.cloudstorage.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    ListView<String> leftPanel;

    @FXML
    ListView<String> rightPanel;

    @FXML
    GridPane connectionSetupPanel;

    private Path clientPath = Paths.get("./client-files");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            leftPanel.getItems().addAll(getFilesFromDirectory(clientPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void connectAction(ActionEvent actionEvent) {
        connectionSetupPanel.setVisible(false);
        rightPanel.setVisible(true);
    }

    private List<String> getFilesFromDirectory(Path path) throws IOException {
        List<String> out = new ArrayList<>();
        StringBuilder stb = new StringBuilder();
        List<Path> paths = Files.list(path).collect(Collectors.toList());
        for (Path p : paths) {
            stb.append(p.getFileName().toString()).append("\t\t").append('|').append('\t').append(Files.size(p)).append(" bytes");
            out.add(stb.toString());
            stb.setLength(0);
        }

        return out;
    }
}
