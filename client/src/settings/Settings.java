package settings;

import captureSettings.CaptureSettings;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import tools.Constants;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Contains all settings data pertaining to the usage of the main functions of the program.
 * <p>
 * All settings data saves to {@link Constants#SETTINGS_FILE} which by default is named "settings.txt".
 *
 * @since 1.0
 */
public class Settings {
    private Properties mySettings;

    private boolean enableGIF = true;
    private boolean saveAllImages = true;
    private String saveDirectory = Constants.DEFAULT_ALL_IMAGES_FOLDER;
    private boolean enableImageShortcut = true;
    private boolean enableGIFShortcut = true;
    private String captureImageShortcut = Constants.DEFAULT_IMAGE_SHORTCUT;
    private String captureGIFShortcut = Constants.DEFAULT_GIF_SHORTCUT;
    private List<String> keys = getListFromString(captureImageShortcut);
    private List<String> keys2 = getListFromString(captureGIFShortcut);
    private List<KeyCode> keyCodes = stringToKeyCodes(captureImageShortcut);
    private List<KeyCode> keyCodes2 = stringToKeyCodes(captureGIFShortcut);
    private boolean enablePrintScreen = true;
    private boolean enableSound = true;

    /**
     * Checks if settings data file already exists. If the file does exist, the default values will be replaced by the
     * data contained in the file.
     * <p>
     * If the file does not exist then a file will be created storing the default values.
     * <p>
     * Also checks if the configuration file exists and if it doesn't, the {@link CaptureSettings} GUI will display a
     * window which allows for the user to begin setting up the screen capturing application.
     */
    public Settings() {
        mySettings = new Properties();

        if (new File(Constants.SETTINGS_FILE).exists()) {
            updateSettings();
        } else {
            saveSettings(enableGIF, saveAllImages, saveDirectory, enableImageShortcut, keyCodes, enableGIFShortcut,
                    keyCodes2, enablePrintScreen, enableSound);
        }

        if (!new File(Constants.CONFIG_FILE).exists()) {
            JDialog dialog = new JDialog();
            dialog.setAlwaysOnTop(true);

            JOptionPane.showMessageDialog(dialog, "This must be your first run. Please input your server's " +
                    "image upload host location. An example php host file is located at '" + Constants.GITHUB + "'.");

            Platform.runLater(() -> CaptureSettings.createInstance(this, new Config()));
        }
    }

    /**
     * This method allows for current settings data to be updated and will also store all data in the file
     * designated by {@link Constants#SETTINGS_FILE}.
     *
     * @param enableGIF             True if GIF capturing capabilities is enabled.
     * @param saveAllImages         True if the user would like to save all images to a folder as well as upload it to
     *                              a website.
     * @param saveDirectory         The directory which to save those images. Only applicable if {@link #saveAllImages}
     *                              is true.
     * @param enableImageShortcut   True if the use would like to enable an image capturing key shortcut.
     * @param imageShortcutKeycodes The list of {@link KeyCode} that make up the desired key combination to enable image
     *                              screen capture. Only applicable if {@link #enableImageShortcut} is true.
     * @param enableGIFShortcut     True if the use would like to enable a GIF capturing key shortcut.
     * @param gifShortcutKeycodes   The list of {@link KeyCode} that make up the desired key combination to enable GIF
     *                              screen capture. Only applicable if {@link #enableGIFShortcut} is true.
     * @param enablePrintScreen     True if the print screen key should initiate image screen capturing.
     * @param enableSound           True if a sound should be played after screen capturing is completed.
     */
    public void saveSettings(boolean enableGIF, boolean saveAllImages, String saveDirectory,
                             boolean enableImageShortcut, List<KeyCode> imageShortcutKeycodes, boolean enableGIFShortcut,
                             List<KeyCode> gifShortcutKeycodes, boolean enablePrintScreen, boolean enableSound) {
        this.enableGIF = enableGIF;
        this.saveAllImages = saveAllImages;
        this.saveDirectory = saveDirectory.equals("") ? Constants.DEFAULT_ALL_IMAGES_FOLDER : saveDirectory;
        this.enableImageShortcut = enableImageShortcut;
        this.enableGIFShortcut = enableGIFShortcut;
        this.keyCodes = imageShortcutKeycodes;
        this.keyCodes2 = gifShortcutKeycodes;
        this.enablePrintScreen = enablePrintScreen;
        this.enableSound = enableSound;

        try {
            File settingsFile = new File(Constants.SETTINGS_FILE);
            settingsFile.getParentFile().mkdirs();
            settingsFile.createNewFile();

            String keyCodesString = keyCodesToString(imageShortcutKeycodes);
            String keyCodesString2 = keyCodesToString(gifShortcutKeycodes);

            if (saveDirectory == null || saveDirectory.equals(""))
                saveDirectory = this.saveDirectory;

            new File(saveDirectory).mkdirs();

            mySettings.setProperty("enableGIF", Boolean.toString(enableGIF));
            mySettings.setProperty("saveAllImages", Boolean.toString(saveAllImages));
            mySettings.setProperty("enablePrintScreen", Boolean.toString(enablePrintScreen));
            mySettings.setProperty("enableSound", Boolean.toString(enableSound));
            mySettings.setProperty("saveDirectory", saveDirectory);
            mySettings.setProperty("enableImageShortcut", Boolean.toString(enableImageShortcut));
            mySettings.setProperty("enableGIFShortcut", Boolean.toString(enableGIFShortcut));
            mySettings.setProperty("keyCodes", keyCodesString);
            mySettings.setProperty("keyCodes2", keyCodesString2);

            mySettings.store(new FileOutputStream(settingsFile, false), Long.toString(System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all data in the settings file located at {@link Constants#SETTINGS_FILE} and stores the value in the
     * correct variable.
     * <p>
     * If an {@link IOException} is caught, the file will be recreated and all default data will be stored in that file.
     */
    private void updateSettings() {
        File settingsFile = new File(Constants.SETTINGS_FILE);

        try {
            mySettings.load(new FileInputStream(settingsFile));

            enableGIF = mySettings.getProperty("enableGIF") != null &&
                    Boolean.parseBoolean(mySettings.getProperty("enableGIF"));
            keys = mySettings.getProperty("keyCodes") != null ? getListFromString(mySettings.getProperty("keyCodes")) : keys;
            keys2 = mySettings.getProperty("keyCodes2") != null ?
                    getListFromString(mySettings.getProperty("keyCodes2")) : keys2;
            keyCodes = mySettings.getProperty("keyCodes") != null ?
                    stringToKeyCodes(mySettings.getProperty("keyCodes")) : keyCodes;
            keyCodes2 = mySettings.getProperty("keyCodes2") != null ?
                    stringToKeyCodes(mySettings.getProperty("keyCodes2")) : keyCodes2;
            saveAllImages = mySettings.getProperty("saveAllImages") != null ?
                    Boolean.parseBoolean(mySettings.getProperty("saveAllImages")) : saveAllImages;
            saveDirectory = mySettings.getProperty("saveDirectory") != null ?
                    mySettings.getProperty("saveDirectory") : saveDirectory;
            enableImageShortcut = mySettings.getProperty("enableImageShortcut") != null ?
                    Boolean.parseBoolean(mySettings.getProperty("enableImageShortcut")) : enableImageShortcut;
            enableGIFShortcut = mySettings.getProperty("enableGIFShortcut") != null ?
                    Boolean.parseBoolean(mySettings.getProperty("enableGIFShortcut")) : enableGIFShortcut;
            captureImageShortcut = mySettings.getProperty("keyCodes") != null ?
                    mySettings.getProperty("keyCodes") : captureImageShortcut;
            captureGIFShortcut = mySettings.getProperty("keyCodes2") != null ?
                    mySettings.getProperty("keyCodes2") : captureGIFShortcut;
            enablePrintScreen = mySettings.getProperty("enablePrintScreen") != null ?
                    Boolean.parseBoolean(mySettings.getProperty("enablePrintScreen")) : enablePrintScreen;
            enableSound = mySettings.getProperty("enableSound") != null ?
                    Boolean.parseBoolean(mySettings.getProperty("enableSound")) : enableSound;

            new File(saveDirectory).mkdirs();
        } catch (IOException e) {
            if (settingsFile.exists()) {
                settingsFile.delete();
            }

            JOptionPane.showMessageDialog(null, "The settings file is corrupted! All values have been reset.");
            System.err.println("Settings file is corrupted. File deleted and will be set to default values.");

            saveSettings(enableGIF, saveAllImages, saveDirectory, enableImageShortcut, keyCodes, enableGIFShortcut,
                    keyCodes2, enablePrintScreen, enableSound);
        }
    }

    /**
     * Converts a given {@link String} key combination into a {@link List} of {@link KeyCode} objects.
     *
     * @param text The key combination in {@link String} format separated by " " (one space character).
     * @return A list of {@link KeyCode} objects that make up the combination of keys in order.
     */
    private List<KeyCode> stringToKeyCodes(String text) {
        List<KeyCode> keys = new ArrayList<>();
        String[] split = text.split(" ");
        for (int i = 0; i < split.length; i++) {
            keys.add(KeyCode.getKeyCode(split[i]));
        }

        return keys;
    }

    /**
     * Converts a given {@link List<KeyCode>} key combination into a {@link String} of keys.
     *
     * @param keyCodes The {@link List<KeyCode>} that is converted into a {@link String} separated by " " (one space
     *                 character) in the same order as the {@link List}.
     * @return A {@link String} of keys that are separated by " ".
     */
    private String keyCodesToString(List<KeyCode> keyCodes) {
        if (keyCodes.size() == 0)
            return "";

        String allKeyCodes = "";
        for (KeyCode keyCode : keyCodes)
            allKeyCodes += keyCode.getName() + " ";
        allKeyCodes = allKeyCodes.substring(0, allKeyCodes.length() - 1);

        return allKeyCodes;
    }

    /**
     * Splits a {@link String} into a {@link List<String>}.
     *
     * @param text The {@link String} that is separated by " " (one space character).
     * @return A {@link List<String>} of key combinations.
     */
    private List<String> getListFromString(String text) {
        String[] split = text.split(" ");

        return new ArrayList<>(Arrays.asList(split));
    }

    /**
     * Converts a given {@link List<String>} key combination into a {@link String} of keys.
     *
     * @param keys The {@link List<String>} that is converted into a {@link String} separated by " " (one space
     *             character) in the same order as the {@link List}.
     * @return A {@link String} of keys that are separated by " ".
     */
    public String getStringFromKeycodes(List<String> keys) {
        if (keys.size() == 0)
            return "";

        StringBuilder stringBuilder = new StringBuilder();

        keys.forEach(key -> stringBuilder.append(key).append(" + "));

        return stringBuilder.toString().substring(0, stringBuilder.toString().length() - 3);
    }

    public boolean isEnableGIF() {
        return enableGIF;
    }

    public boolean isSaveAllImages() {
        return saveAllImages;
    }

    public String getSaveDirectory() {
        return saveDirectory;
    }

    public boolean isEnableImageShortcut() {
        return enableImageShortcut;
    }

    public boolean isEnableGIFShortcut() {
        return enableGIFShortcut;
    }

    public String getCaptureImageShortcut() {
        return captureImageShortcut;
    }

    public String getCaptureGIFShortcut() {
        return captureGIFShortcut;
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<String> getKeys2() {
        return keys2;
    }

    public List<KeyCode> getKeyCodes() {
        return keyCodes;
    }

    public List<KeyCode> getKeyCodes2() {
        return keyCodes2;
    }

    public boolean isEnablePrintScreen() {
        return enablePrintScreen;
    }

    public boolean isEnableSound() {
        return enableSound;
    }
}
