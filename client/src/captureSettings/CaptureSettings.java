package captureSettings;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import settings.Config;
import settings.Settings;
import tools.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the GUI for the {@link Settings} and {@link Config} which allows for modification of the
 * settings and config files for the program.
 *
 * @since 1.0
 */
public class CaptureSettings {
    private static boolean imageShortcutSelected = false;
    private static boolean gifShortcutSelected = false;
    private static List<String> keys = new ArrayList<>();
    private static List<String> keys2 = new ArrayList<>();
    private static List<KeyCode> imageShortcutKeycodes = new ArrayList<>();
    private static List<KeyCode> gifShortcutKeycodes = new ArrayList<>();

    /**
     * JavaFX {@link Stage} that displays a modifiable settings and config GUI that, upon saving all changes, will also
     * update the user's settings and config save files.
     *
     * @param settings The settings class that is passed in from {@link tray.CreateTrayIcon}.
     */
    public static void createInstance(Settings settings, Config config) {
        Stage stage = new Stage();
        stage.setTitle(Constants.PROGRAM_NAME + " Settings");
        stage.centerOnScreen();

        keys = settings.getKeys();
        keys2 = settings.getKeys2();
        imageShortcutKeycodes = settings.getKeyCodes();
        gifShortcutKeycodes = settings.getKeyCodes2();

        /* ENABLE GIF START */
        CheckBox enableGIF = new CheckBox();
        enableGIF.setAllowIndeterminate(false);
        enableGIF.setWrapText(true);
        enableGIF.setText("Enable GIF Capture?");
        enableGIF.setSelected(settings.isEnableGIF());
        /* ENABLE GIF END */

        /* SAVE IMAGES START */
        CheckBox saveAllImages = new CheckBox();
        saveAllImages.setAllowIndeterminate(false);
        saveAllImages.setWrapText(true);
        saveAllImages.setText("Automatically save all captured images to disk? (Unchecking this results in " +
                "\"View All Images\" to not show any images.");
        saveAllImages.setSelected(settings.isSaveAllImages());

        TextField saveDirectory = new TextField();
        saveDirectory.setEditable(false);
        if (saveAllImages.isSelected())
            saveDirectory.setText(settings.getSaveDirectory());

        Button saveDirectoryButton = new Button("Choose Save Directory");
        saveDirectoryButton.setDisable(!saveAllImages.isSelected());
        saveDirectoryButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Save Directory");

            File selectedDirectory = directoryChooser.showDialog(stage);
            if (selectedDirectory != null)
                saveDirectory.setText(selectedDirectory.getAbsolutePath());
        });

        saveAllImages.setOnAction(e -> {
            if (saveAllImages.isSelected()) {
                saveDirectory.setText(settings.getSaveDirectory());
                saveDirectoryButton.setDisable(false);
            } else {
                saveDirectory.setText("");
                saveDirectoryButton.setDisable(true);
            }
        });
        /* SAVE IMAGES END */

        /* IMAGE SHORTCUT START */
        CheckBox enableImageShortcut = new CheckBox();
        enableImageShortcut.setAllowIndeterminate(false);
        enableImageShortcut.setText("Do you want to be able to start " + Constants.PROGRAM_NAME + "'s Image Screenshot with a shortcut?");
        enableImageShortcut.setWrapText(true);
        enableImageShortcut.setSelected(settings.isEnableImageShortcut());

        Button imageShortcutClear = new Button("Clear Image Shortcut");
        imageShortcutClear.setDisable(!enableImageShortcut.isSelected());

        TextField imageShortcut = new TextField();
        imageShortcut.setEditable(false);
        if (enableImageShortcut.isSelected())
            imageShortcut.setText(settings.getStringFromKeycodes(keys));

        enableImageShortcut.setOnAction(e -> {
            if (enableImageShortcut.isSelected()) {
                imageShortcut.setText(settings.getStringFromKeycodes(keys));
                imageShortcutClear.setDisable(false);
            } else {
                imageShortcut.setText("");
                imageShortcutClear.setDisable(true);
            }
        });

        imageShortcutClear.setOnAction(e -> {
            imageShortcut.setText("");
            keys = new ArrayList<>();
            imageShortcutKeycodes = new ArrayList<>();
        });
        /* IMAGE SHORTCUT END */

        imageShortcut.focusedProperty().addListener((observable, oldValue, newValue) -> keys1FocusState(newValue));

        /* GIF SHORTCUT START */
        CheckBox enableGIFShortcut = new CheckBox();
        enableGIFShortcut.setAllowIndeterminate(false);
        enableGIFShortcut.setSelected(settings.isEnableGIFShortcut());
        enableGIFShortcut.disableProperty().bind(Bindings.createBooleanBinding(() ->
                !(enableGIF.selectedProperty().get()), enableGIF.selectedProperty()));

        Button gifShortcutClear = new Button("Clear GIF Shortcut");
        gifShortcutClear.setDisable(!enableGIFShortcut.isSelected());
        gifShortcutClear.disableProperty().bind(Bindings.createBooleanBinding(() ->
                !(enableGIF.selectedProperty().get()), enableGIF.selectedProperty()));

        TextField gifShortcut = new TextField();
        gifShortcut.setEditable(false);
        if (enableGIFShortcut.isSelected())
            gifShortcut.setText(settings.getStringFromKeycodes(keys2));
        gifShortcut.disableProperty().bind(Bindings.createBooleanBinding(() ->
                !(enableGIF.selectedProperty().get()), enableGIF.selectedProperty()));

        enableGIFShortcut.setText("Do you want to be able to start " + Constants.PROGRAM_NAME + "'s GIF Screen capture with a shortcut?");
        enableGIFShortcut.setWrapText(true);
        enableGIFShortcut.setSelected(settings.isEnableGIFShortcut());
        enableGIFShortcut.setOnAction(e -> {
            if (enableGIFShortcut.isSelected()) {
                gifShortcut.setText(settings.getStringFromKeycodes(keys2));
                gifShortcutClear.setDisable(false);
            } else {
                gifShortcut.setText("");
                gifShortcutClear.setDisable(true);
            }
        });

        gifShortcutClear.setOnAction(e -> {
            gifShortcut.setText("");
            keys2 = new ArrayList<>();
            gifShortcutKeycodes = new ArrayList<>();
        });

        gifShortcut.focusedProperty().addListener((observable, oldValue, newValue) -> keys2FocusState(newValue));
        /* GIF SHORTCUT END */

        CheckBox enablePrintScreen = new CheckBox(
                "Would you like " + Constants.PROGRAM_NAME + "'s Image Capture to be run with the Print Screen button?");
        enablePrintScreen.setAllowIndeterminate(false);
        enablePrintScreen.setWrapText(true);
        enablePrintScreen.setSelected(settings.isEnablePrintScreen());

        CheckBox enableSound = new CheckBox("Play sound to confirm capture?");
        enableSound.setAllowIndeterminate(false);
        enableSound.setWrapText(true);
        enableSound.setSelected(settings.isEnableSound());

        Button save = new Button("Save");
        save.setOnAction(e -> {
            settings.saveSettings(enableGIF.isSelected(),
                    saveAllImages.isSelected(),
                    saveDirectory.getText() != null ? saveDirectory.getText() : Constants.DEFAULT_ALL_IMAGES_FOLDER,
                    enableImageShortcut.isSelected(),
                    imageShortcutKeycodes,
                    enableGIFShortcut.isSelected(),
                    gifShortcutKeycodes,
                    enablePrintScreen.isSelected(),
                    enableSound.isSelected());

            stage.hide();
        });

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> stage.hide());

        Button advanced = new Button("Advanced Settings");
        advanced.setOnAction(e -> showAdvancedDialog(settings, config));

        /* LAYOUT START */
        VBox saveDirLayout = new VBox();
        saveDirLayout.setAlignment(Pos.CENTER);
        saveDirLayout.getChildren().addAll(saveAllImages, saveDirectory, saveDirectoryButton);

        VBox imageShortcutLayout = new VBox();
        imageShortcutLayout.setAlignment(Pos.CENTER);
        imageShortcutLayout.getChildren().addAll(enableImageShortcut, imageShortcut, imageShortcutClear);

        VBox gifShortcutLayout = new VBox();
        gifShortcutLayout.setAlignment(Pos.CENTER);
        gifShortcutLayout.getChildren().addAll(enableGIFShortcut, gifShortcut, gifShortcutClear);

        VBox pane = new VBox();
        pane.setPadding(new Insets(10));
        pane.setSpacing(16);
        pane.getChildren().addAll(enableGIF, saveDirLayout, imageShortcutLayout, gifShortcutLayout, enablePrintScreen,
                enableSound);

        BorderPane buttons = new BorderPane();
        buttons.setLeft(cancel);
        buttons.setCenter(advanced);
        buttons.setRight(save);

        BorderPane all = new BorderPane();
        all.setCenter(pane);
        all.setBottom(buttons);
        /* LAYOUT END */

        Scene scene = new Scene(all);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e ->
                checkKeycodes(e, enableImageShortcut, imageShortcut, enableGIFShortcut, gifShortcut));

        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Displays a dialog for the variables in the {@link Config} class.
     * <p>
     * The PHP server file, Gfycat Client ID, and Gfycat Client Secret are all encrypted when stored in the file.
     *
     * @param config The configuration settings that is passed in by {@link #createInstance(Settings, Config)}.
     *               {@link #createInstance(Settings, Config)} also retrieves the {@link Config} class from
     *               {@link tray.CreateTrayIcon}.
     */
    private static void showAdvancedDialog(Settings settings, Config config) {
        Stage configStage = new Stage();
        configStage.centerOnScreen();
        configStage.setAlwaysOnTop(true);
        configStage.setTitle(Constants.PROGRAM_NAME + " Advanced Settings");

        Label serverLabel = new Label("php Server Direct File (Example file on Github)");
        serverLabel.setWrapText(true);
        TextField server = new TextField();
        server.setText(config.getServer());

        Label serverPasswordLabel = new Label("php Server Password (Leave blank if not applicable)");
        serverLabel.setWrapText(true);
        TextField serverPassword = new TextField();
        serverPassword.setText(config.getServerPassword());

        CheckBox enableGfycatUpload = new CheckBox("Enable Gfycat Upload (Recommended for GIFs)");
        enableGfycatUpload.setAllowIndeterminate(false);
        enableGfycatUpload.setWrapText(true);
        enableGfycatUpload.setSelected(config.isEnableGfycatUpload());
        enableGfycatUpload.disableProperty().bind(Bindings.createBooleanBinding(() -> !settings.isEnableGIF()));

        Label gfycatClientIDLabel = new Label("Gfycat Client ID (Instructions on Github)");
        gfycatClientIDLabel.setWrapText(true);
        TextField gfycatClientID = new TextField();
        gfycatClientID.setText(config.getGfycatClientID());
        gfycatClientID.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        !(enableGfycatUpload.selectedProperty().get() && settings.isEnableGIF()),
                enableGfycatUpload.selectedProperty()));

        Label gfycatClientSecretLabel = new Label("Gfycat Client Secret (Instructions on Github)");
        gfycatClientSecretLabel.setWrapText(true);
        TextField gfycatClientSecret = new TextField();
        gfycatClientSecret.setText(config.getGfycatClientSecret());
        gfycatClientSecret.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        !(enableGfycatUpload.selectedProperty().get() && settings.isEnableGIF()),
                enableGfycatUpload.selectedProperty()));

        VBox configPane = new VBox();
        configPane.setPadding(new Insets(10));
        configPane.setSpacing(16);
        configPane.getChildren().addAll(serverLabel, server, serverPasswordLabel, serverPassword, enableGfycatUpload,
                gfycatClientIDLabel, gfycatClientID, gfycatClientSecretLabel, gfycatClientSecret);

        Button configSave = new Button("Save");
        configSave.setOnAction(ex -> {
            config.saveConfig(server.getText(),
                    serverPassword.getText(),
                    enableGfycatUpload.isSelected(),
                    gfycatClientID.getText(),
                    gfycatClientSecret.getText());

            configStage.hide();
        });

        Button configCancel = new Button("Cancel");
        configCancel.setOnAction(ex -> configStage.hide());

        BorderPane configButtons = new BorderPane();
        configButtons.setLeft(configCancel);
        configButtons.setRight(configSave);

        BorderPane configAll = new BorderPane();
        configAll.setCenter(configPane);
        configAll.setBottom(configButtons);

        configStage.setScene(new Scene(configAll));

        configStage.showAndWait();
    }

    /**
     * Checks to see which {@link TextField} has focus and stores up to 3 {@link KeyCode} objects in a
     * {@link List<KeyCode>}. Also converts the list to {@link List<String>} for visibility.
     *
     * @param e                   {@link KeyEvent} that triggered the method to be called.
     * @param enableImageShortcut {@link CheckBox} that corresponds to whether the image shortcut capture is enabled.
     * @param imageShortcut       Image shortcut {@link TextField} that will be edited when key strokes are entered.
     * @param enableGIFShortcut   {@link CheckBox} that corresponds to whether the GIF shortcut capture is enabled.
     * @param gifShortcut         GIF shortcut {@link TextField} that will be edited when key strokes are entered.
     */
    private static void checkKeycodes(KeyEvent e, CheckBox enableImageShortcut, TextField imageShortcut,
                                      CheckBox enableGIFShortcut, TextField gifShortcut) {
        if (imageShortcutSelected && enableImageShortcut.isSelected()) {
            if (keys.size() < 3) {
                if (!keys.contains(e.getCode().getName())) {
                    keys.add(e.getCode().getName());
                    imageShortcutKeycodes.add(e.getCode());
                }

                if (keys.size() > 0) {
                    String keysString = "";
                    for (String s : keys)
                        keysString += s + " + ";
                    keysString = keysString.substring(0, keysString.length() - 3);

                    imageShortcut.setText(keysString);
                }
            } else
                imageShortcutSelected = false;
        }

        if (gifShortcutSelected && enableGIFShortcut.isSelected()) {
            if (keys2.size() < 3) {
                if (!keys2.contains(e.getCode().getName())) {
                    keys2.add(e.getCode().getName());
                    gifShortcutKeycodes.add(e.getCode());
                }

                if (keys2.size() > 0) {
                    String keysString = "";
                    for (String s : keys2)
                        keysString += s + " + ";
                    keysString = keysString.substring(0, keysString.length() - 3);

                    gifShortcut.setText(keysString);
                }
            } else
                gifShortcutSelected = false;
        }
    }

    /**
     * Checks if image {@link TextField} is focused and resets both {@link List}'s that store {@link KeyCode} and
     * {@link String}.
     *
     * @param value True if the object is in focus.
     */
    private static void keys1FocusState(boolean value) {
        if (value) {
            imageShortcutSelected = true;
            keys = new ArrayList<>();
            imageShortcutKeycodes = new ArrayList<>();
        } else
            imageShortcutSelected = false;
    }

    /**
     * Checks if GIF {@link TextField} is focused and resets both {@link List}'s that store {@link KeyCode} and
     * {@link String}.
     *
     * @param value True if the object is in focus.
     */
    private static void keys2FocusState(boolean value) {
        if (value) {
            gifShortcutSelected = true;
            keys2 = new ArrayList<>();
            gifShortcutKeycodes = new ArrayList<>();
        } else
            gifShortcutSelected = false;
    }
}
