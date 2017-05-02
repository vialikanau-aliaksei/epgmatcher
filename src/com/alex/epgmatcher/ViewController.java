package com.alex.epgmatcher;

import com.alex.epgmatcher.beans.*;
import com.alex.epgmatcher.util.DataHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

/**
 * Controller of javafx fxml view
 */
public class ViewController implements Initializable {

    public static final String MAPPING_FILENAME = "mapping.xml";
    private static final String CONFIG_FILENAME = "config.xml";
    private static final String EXCEPTION_TITLE = "The exception stacktrace was:";


    @FXML
    private TextField m3uField;
    @FXML
    private TextField epgField;
    @FXML
    private TableView<Channel> tableView;
    @FXML
    private TableColumn orderColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn epgColumn;


    private Stage primaryStage;
    private List<EPG> epgs;
    private LoadController loadController;

    private void createCells() {
        orderColumn.setCellValueFactory(new PropertyValueFactory<Channel, String>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Channel, String>("name"));
        nameColumn.setCellFactory(new Callback() {
            @Override
            public Object call(Object column) {
                return new TableCell<Channel, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? "" : getItem());
                        setGraphic(null);

                        TableRow<Channel> currentRow = getTableRow();

                        if (!isEmpty()) {
                            Channel channel = currentRow.getItem();
                            if (channel != null && channel.getEqualityType() == Channel.EqualityType.PARTIAL)
                                currentRow.setStyle("-fx-background-color:lightcoral");
                            else
                                currentRow.setStyle("");
                        }

                    }
                };
            }
        });
        epgColumn.setCellValueFactory(new PropertyValueFactory<Channel, EPG>("epg"));
        epgColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(new ArrayList<EPG>())));

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            List<EPG> mathed = new ArrayList<>();
            if (newValue != null && newValue.getFindResults() != null) {
                for (FindResult result : newValue.getFindResults()) {
                    mathed.add(result.getEpg());
                }
            }
            mathed.add(new EPG());
            List<EPG> other = new ArrayList<>(epgs);
            other.removeAll(mathed);
            ComboBoxTableCell cell = (ComboBoxTableCell) epgColumn.getCellFactory().call(epgColumn);
            cell.getItems().setAll(mathed);
            cell.getItems().addAll(other);
        });

        epgColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Channel, EPG>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Channel, EPG> event) {
                Channel channel = event.getTableView().getItems().get(event.getTablePosition().getRow());
                EPG epg = event.getNewValue();
                if (epg == null) return;
                Set<FindResult> results = channel.getFindResults();
                for (Iterator<FindResult> iterator = results.iterator(); iterator.hasNext(); ) {
                    FindResult findResult = iterator.next();
                    if (findResult.getEpg().getName().equals(epg.getName())) {
                        iterator.remove();
                        findResult.setRate(1.1);
                        results.add(findResult);
                        return;
                    }
                }
                FindResult findResult = new FindResult(epg, 1.1);
                channel.getFindResults().add(findResult);
            }
        });
        tableView.setRowFactory(tv -> new TableRow<Channel>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            public void updateItem(Channel channel, boolean empty) {
                super.updateItem(channel, empty);
                if (channel == null) {
                    setTooltip(null);
                } else {
                    tooltip.setText(channel.toString());
                    setTooltip(tooltip);
                }
            }
        });

    }

    @FXML
    public void actionLoad(ActionEvent actionEvent) {
        String m3uUrl = m3uField.getText();
        String epgUrl = epgField.getText();

        LoadTask task = new LoadTask(m3uUrl, epgUrl);
        task.setOnRunning(event -> loadController.show(task));
        task.setOnSucceeded(event -> {
            LoadData loadData = task.getValue();
            List<Channel> channels = loadData.getChannels();
            epgs = loadData.getEpgs();
            Collections.sort(epgs);
            Map<String, String> dictionary = DataHandler.load(new TreeMap<String, String>(), MAPPING_FILENAME);
            Matcher.matchEpg(channels, epgs, dictionary);
            tableView.getItems().clear();
            tableView.getItems().addAll(channels);

            try {
                Config config = getConfig();
                DataHandler.save(config, CONFIG_FILENAME);
            } catch (IOException e) {
                showError(e);
            }
            loadController.hide();
        });
        task.setOnFailed(event -> {
            loadController.hide();
            showError(task.getException());
        });
        new Thread(task).start();


    }

    @FXML
    public void actionSave(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("M3U files (*.m3u)", "*.m3u");
        fileChooser.getExtensionFilters().add(extFilter);
        String filename = DataHandler.load(new Config(), CONFIG_FILENAME).getOutputFilename();
        fileChooser.setInitialFileName(filename);

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                DataHandler.saveM3U(tableView.getItems(), file);
                Config config = getConfig();
                TreeMap<String, String> map = getMapping(tableView);
                DataHandler.save(map, MAPPING_FILENAME);
                config.setOutputFilename(file.getCanonicalPath());
                DataHandler.save(config, CONFIG_FILENAME);

            } catch (IOException e) {
                showError(e);
            }
        }
    }

    private Config getConfig() {
        Config config = DataHandler.load(new Config(), CONFIG_FILENAME);
        config.setM3uUrl(m3uField.getText());
        config.setEpgUrl(epgField.getText());

        return config;
    }

    private TreeMap<String, String> getMapping(TableView<Channel> tableView) {
        TreeMap<String, String> channelMap = new TreeMap<>();
        for (Channel channel : tableView.getItems()) {
            if (!channel.getEPGName().isEmpty() && !channel.getEPGName().equals(channel.getName())) {
                channelMap.put(channel.getName(), channel.getEPGName());
            }
        }
        return channelMap;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Channel> tableList = FXCollections.observableArrayList(new ArrayList<>());
        Config config = DataHandler.load(new Config(), CONFIG_FILENAME);
        setFields(config);
        tableView.setItems(tableList);
        createCells();
    }

    private void setFields(Config config) {
        m3uField.setText(config.getM3uUrl());
        epgField.setText(config.getEpgUrl());
    }

    private void showError(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Main.TITLE);
        alert.setHeaderText(ex.getClass().getSimpleName());
        alert.setContentText(ex.getLocalizedMessage());

// Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label(EXCEPTION_TITLE);

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }


    public void setStage(Stage stage) throws IOException {
        this.primaryStage = stage;
        loadController = new LoadController(stage);
    }
}
