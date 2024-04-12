package net.jchad.installer.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import net.jchad.installer.config.ConfigManager;
import net.jchad.installer.core.progressBar.Bar;
import net.jchad.installer.core.progressBar.BarDisplay;
import net.jchad.installer.core.progressBar.BarStatus;

import java.io.File;
import java.nio.file.Path;
import java.util.EventListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GUI extends Application implements BarDisplay {

    private ProgressBar progressBar;
    private ConfigManager configManager = new ConfigManager();

    private VBox vbox;
    private int selectedIndex;

    private static final double BASE_WIDTH = 800;
    private static final double BASE_HEIGHT = 600;

    private static final double MAX_SCALE = 1.3;
    private static final double MIN_SCALE = 1.0;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws InstantiationException {
        primaryStage.setTitle("JChad Setup");

        vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: white;");


        Label welcomeLabel = new Label("Welcome to the JChad setup!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        vbox.getChildren().add(welcomeLabel);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button chooseDirectoryButton = new Button("Choose Installation Directory");
        chooseDirectoryButton.setTranslateY(30);
        chooseDirectoryButton.setStyle("-fx-font-size: 13px;");
        chooseDirectoryButton.setOnAction(e -> {
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                Path directory = selectedDirectory.toPath();
                configManager.setDownloadPath(directory.toString());
                chooseDirectoryButton.setText(directory.toString());
            }
        });
        vbox.getChildren().add(chooseDirectoryButton);
        ComboBox<String> softwareComboBox = new ComboBox<>();
        softwareComboBox.setTranslateY(40);
        softwareComboBox.getItems().addAll("Client and Server", "Server", "Client");
        softwareComboBox.setPromptText("Select Software to Install");
        softwareComboBox.setStyle("-fx-font-size: 13px;");
        softwareComboBox.setOnAction(e -> {
            selectedIndex = softwareComboBox.getSelectionModel().getSelectedIndex();
            configManager.setSoftwareToInstall(selectedIndex);
        });
        vbox.getChildren().add(softwareComboBox);

        progressBar = new ProgressBar(); // Initialisierung der progressBar-Variable
        progressBar.setPrefWidth(500);
        progressBar.setPrefHeight(30);
        progressBar.setTranslateY(70);
        vbox.getChildren().add(progressBar);
        Button installButton = new Button("Install");
        installButton.setTranslateY(70);
        installButton.setStyle("-fx-font-size: 13px;");
        installButton.setOnAction(e -> {
            if (selectedIndex == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No Software selected");
                alert.setContentText("Please select Software!");
                alert.showAndWait();
            } else {
                Task<Void> installTask = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        // Perform your installation process here
                        configManager.process();
                        return null;
                    }
                };

                installTask.setOnRunning(event -> {
                    // Update UI to show progress
                    progressBar.setProgress(-1); // Indeterminate progress
                    installButton.setDisable(true); // Disable the button during installation
                });

                installTask.setOnSucceeded(event -> {
                    // Update UI after installation is complete
                    progressBar.setProgress(1); // Set progress to complete
                    progressBar.setStyle("-fx-accent: green;");
                    installButton.setDisable(false); // Enable the button
                });

                installTask.setOnFailed(event -> {
                    // Handle failure

                    progressBar.setProgress(0); // Set progress to zero
                    installButton.setDisable(false); // Enable the button
                });

                new Thread(installTask).start(); // Start the installation task in a new thread
            }});

        vbox.getChildren().add(installButton);

        Scene scene = new Scene(vbox, 400, 200);

        scene.setFill(Color.WHITE);

        scene.widthProperty().addListener((observable, oldValue, newValue) -> {
            // Update the scale factor to maintain the aspect ratio
            double newScale = calculateScale(newValue.doubleValue(), scene.getHeight());
            vbox.setScaleX(newScale);
            vbox.setScaleY(newScale);
        });

        scene.heightProperty().addListener((observable, oldValue, newValue) -> {
            // Update the scale factor to maintain the aspect ratio
            double newScale = calculateScale(scene.getWidth(), newValue.doubleValue());
            vbox.setScaleX(newScale);
            vbox.setScaleY(newScale);
        });

        primaryStage.setScene(scene);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
        hook("RepoDownloader");
    }

    private double calculateScale(double width, double height) {
        // Calculate the aspect ratio
        double aspectRatio = BASE_WIDTH / BASE_HEIGHT;
        // Calculate the scale factor based on the current width and height
        double scaleX = width / BASE_WIDTH;
        double scaleY = height / BASE_HEIGHT;
        // Choose the smaller scale factor to maintain the aspect ratio
        double scale = Math.min(scaleX, scaleY);
        // Limit the scale within the specified range
        return Math.max(Math.min(scale, MAX_SCALE), MIN_SCALE);
    }

    @Override
    public void update(Bar bar) {

    }

    @Override
    public void updateOnFailed(Bar bar) {
        //TODO Recolor the bar to red
        progressBar.setVisible(false);
        ProgressBar progressBar2 = new ProgressBar();
        progressBar2.setPrefWidth(300);
        progressBar2.setPrefHeight(26);
        Platform.runLater(() -> {
            vbox.getChildren().remove(progressBar);
            vbox.getChildren().add(progressBar2);
            progressBar2.setProgress(1);
            progressBar2.setStyle("-fx-accent: red;");
        });
        if (bar.getBarStatus().getException().isPresent()) {
            Pattern getErrorCode = Pattern.compile(".*HTTP response code: ([\\d]{3}).*");
            Matcher findErrorCode = getErrorCode.matcher(bar.getBarStatus().getException().get().getMessage());
            if (findErrorCode.find()) {
                //An error occurred with https error code
                //To get the error code do: findErrorCode.group(1)
                System.out.println(findErrorCode.group(1));
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Occured");
                    alert.setContentText("An error occured: " + findErrorCode.group(1));
                    alert.showAndWait();
                });

            } else {
                //An error occurred without any https error code:
                System.out.println("An error occurred!");
            }

        }
    }

    @Override
    public void updateOnStart(Bar bar) {

    }

    @Override
    public void updateOnEnd(Bar bar) {

    }

    @Override
    public void updateOnUpdate(Bar bar) {

        Platform.runLater(() -> progressBar.setProgress((double) bar.getCurrentProgress() / bar.getMaxProgress()));
    }
}
