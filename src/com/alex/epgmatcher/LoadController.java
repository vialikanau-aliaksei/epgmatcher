package com.alex.epgmatcher;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by Alex on 21.04.2017.
 */
public class LoadController {
    @FXML
    public ProgressBar progressBar;
    @FXML
    public Label label;

    private Task task;

    private Stage stage;
    private LoadController controller;

    public LoadController(){
    }

    public LoadController(Stage parent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("resources/load.fxml"));
        Parent root = fxmlLoader.load();
        controller = fxmlLoader.getController();
        stage = new Stage();
        stage.setTitle(parent.getTitle());
        stage.setScene(new Scene(root, 240, 120));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(parent);
        stage.setResizable(false);
        controller.setStage(stage);
    }

    public void show(LoadTask task) {
        controller.init(task);
        stage.show();
    }

    public void hide() {
        stage.hide();
    }

    @FXML
    public void actionCancel(ActionEvent actionEvent) {
        if (task != null) {
            task.cancel(true);
        }
        if (stage != null) {
            stage.hide();
        }
    }

    public void init(LoadTask task) {
        this.task = task;
        progressBar.progressProperty().bind(task.progressProperty());
        label.textProperty().bind(task.messageProperty());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
