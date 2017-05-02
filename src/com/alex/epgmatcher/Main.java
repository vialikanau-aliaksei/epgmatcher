package com.alex.epgmatcher;

import com.alex.epgmatcher.beans.LoadData;
import com.alex.epgmatcher.util.DataHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.alex.epgmatcher.ViewController.MAPPING_FILENAME;

/**
 * Main start class for GUI and console modes.
 */
public class Main extends Application {

    static final String TITLE = "EPG Matcher";
    private final static Logger logger = Logger.getGlobal();
    private static final String ERROR_M3U_SAVE = "Error on save m3u: ";

    private static final String ERROR_GET_DATA = "Error on get M3U or EPG: ";
    private static final Level DEFAULT_LEVEL = Level.FINE;
    private static final String ICON_FILENAME = "resources/epg.png";

    public static void main(String[] args) {
        if (args.length > 0) {
            setLogger(logger, DEFAULT_LEVEL);
            if (args.length < 3) {
                showHelp();
            } else {
                String m3uUrl = "";
                String epgUrl = "";
                String saveFilename = "";
                for (String arg : args) {
                    if (arg.length() < 6) {
                        showHelp();
                    }
                    int index = arg.indexOf(":");
                    if (index < 1) {
                        showHelp();
                    }
                    String argType = arg.substring(0, index).toLowerCase();
                    switch (argType) {
                        case "-m3u":
                            m3uUrl = arg.substring(index + 1, arg.length()).replaceAll("\"", "");
                            break;
                        case "-epg":
                            epgUrl = arg.substring(index + 1, arg.length()).replaceAll("\"", "");
                            break;
                        case "-output":
                            saveFilename = arg.substring(index + 1, arg.length()).replaceAll("\"", "");
                            break;
                        case "-loglevel":
                            String logLevel = arg.substring(index + 1, arg.length()).toUpperCase().replaceAll("\"", "");
                            try {
                                setLogger(logger, Level.parse(logLevel));
                            } catch (Exception e) {
                                logger.severe(e.getMessage());
                                System.exit(0);
                            }
                            break;
                        default:
                            showHelp();
                    }
                }
                if (m3uUrl.isEmpty() || epgUrl.isEmpty() || saveFilename.isEmpty()) {
                    showHelp();
                } else {
                    match(m3uUrl, epgUrl, saveFilename);
                }
            }
            System.exit(0);
        } else {
            setLogger(logger, Level.OFF);
            launch(args);
        }
    }


    /**
     * Start matching in console mode.
     *
     * @param m3uUrl       url of m3u playlist
     * @param epgUrl       url of EPG xml
     * @param saveFilename name of saved M3U file
     */
    private static void match(String m3uUrl, String epgUrl, String saveFilename) {
        LoadTask task = new LoadTask(m3uUrl, epgUrl);
        LoadData loadData;
        try {
            loadData = task.call();
        } catch (Exception e) {
            logger.severe(ERROR_GET_DATA + e.getMessage());
            return;
        }

        Map<String, String> dictionary = DataHandler.load(new TreeMap<String, String>(), MAPPING_FILENAME);
        Matcher.matchEpg(loadData.getChannels(), loadData.getEpgs(), dictionary);
        File file = new File(saveFilename);
        try {
            DataHandler.saveM3U(loadData.getChannels(), file);
        } catch (IOException e) {
            logger.severe(ERROR_M3U_SAVE + e.getMessage());
        }
    }

    /**
     * Prints help for console mode usage.
     */
    private static void showHelp() {
        System.out.println("EPG Matcher.");
        System.out.println("Match channels with EPG.");
        System.out.println("Usage:");
        System.out.println("epgmatcher -m3u:\"iptv playlist url in m3u format\" " +
                "-epg:\"EPG url in xml or xml.gz format\" " +
                "-output:\"new m3u filename\" " +
                "[-loglevel:\"ALL|SEVERE|OFF\"] ");
        System.exit(0);
    }

    /**
     * Set log level and add {@link ConsoleHandler}
     *
     * @param logger logger for add ConsoleHandler and setting level
     * @param level  level of logging
     */
    private static void setLogger(Logger logger, Level level) {
        logger.setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        logger.addHandler(handler);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("resources/main.fxml"));
        Parent root = fxmlLoader.load();
        ViewController controller = fxmlLoader.getController();
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(new Scene(root, 480, 600));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream(ICON_FILENAME)));
        controller.setStage(primaryStage);
        primaryStage.show();
    }

}
