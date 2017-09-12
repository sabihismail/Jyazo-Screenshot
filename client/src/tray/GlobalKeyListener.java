package tray;

import captureGIF.CaptureGIF;
import captureImage.CaptureImage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import settings.Config;
import settings.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class registers and creates a key listener that performs actions when a certain key or combination of keys is
 * pressed.
 * <p>
 * This project requires a different library as, by default, JavaFX and Swing key listeners do not detect key
 * combinations when a {@link javafx.stage.Stage} or {@link javax.swing.JFrame} is not visible or active.
 *
 * @since 1.0
 */
public class GlobalKeyListener implements NativeKeyListener {
    private Settings settings;
    private Config config;
    private List<String> keys = new ArrayList<>();

    /**
     * Sets the class {@link Settings} and {@link Config} classes to match the one passed in by {@link CreateTrayIcon}.
     *
     * @param settings {@link Settings} class passed in from {@link CreateTrayIcon}.
     * @param config   {@link Config} class passed in from {@link CreateTrayIcon}.
     */
    private GlobalKeyListener(Settings settings, Config config) {
        this.settings = settings;
        this.config = config;
    }

    /**
     * Primarily, the pressed key is added to {@link #keys} which contains all keys that have been pressed without
     * having been released.
     * <p>
     * If {@link Settings#enableImageShortcut} is enabled and the and the list of keys match
     * {@link Settings#captureImageShortcut}, the {@link CaptureImage} GUI will be instantiated.
     * <p>
     * The {@link CaptureImage} GUI will also be instantiated by clicking on the "Print Screen" key if
     * {@link Settings#enablePrintScreen} is true.
     * <p>
     * Similarly, if {@link Settings#enableGIFShortcut} is enabled and the and the list of keys match
     * {@link Settings#captureGIFShortcut}, the {@link CaptureGIF} GUI will be instantiated. This also requires
     * {@link Settings#enableGIF} to be true.
     *
     * @param keyEvent Information about the key that was pressed.
     */
    public void nativeKeyPressed(NativeKeyEvent keyEvent) {
        String key = NativeKeyEvent.getKeyText(keyEvent.getKeyCode());
        if (key.equalsIgnoreCase("Left Control") || key.equalsIgnoreCase("Right Control")) {
            key = "Ctrl";
        } else if (key.equalsIgnoreCase("Left Shift") || key.equalsIgnoreCase("Right Shift")) {
            key = "Shift";
        }

        if (!keys.contains(key)) {
            keys.add(key);
        }

        if (settings.isEnableImageShortcut()) {
            if (keys.size() == settings.getKeyCodes().size()) {
                boolean[] allOuterBooleans = new boolean[keys.size()];
                Arrays.fill(allOuterBooleans, false);

                for (int i = 0; i < keys.size(); i++) {
                    boolean innerBoolean = false;

                    for (int j = 0; j < settings.getKeyCodes().size(); j++) {
                        String clickedKey = keys.get(i);
                        String savedKey = settings.getKeyCodes().get(j).getName();
                        if (clickedKey.equalsIgnoreCase(savedKey)) {
                            innerBoolean = true;
                            break;
                        }
                    }

                    allOuterBooleans[i] = innerBoolean;
                }

                if (areAllTrue(allOuterBooleans)) {
                    CaptureImage.createInstance(settings, config);
                }
            }
        }

        if (settings.isEnableGIF() && settings.isEnableGIFShortcut()) {
            if (keys.size() == settings.getKeyCodes2().size()) {
                boolean[] allOuterBooleans = new boolean[keys.size()];
                Arrays.fill(allOuterBooleans, false);

                for (int i = 0; i < keys.size(); i++) {
                    boolean innerBoolean = false;

                    for (int j = 0; j < settings.getKeyCodes2().size(); j++) {
                        String clickedKey = keys.get(i);
                        String savedKey = settings.getKeyCodes2().get(j).getName();
                        if (clickedKey.equalsIgnoreCase(savedKey)) {
                            innerBoolean = true;
                            break;
                        }
                    }

                    allOuterBooleans[i] = innerBoolean;
                }

                if (areAllTrue(allOuterBooleans)) {
                    CaptureGIF.createInstance();
                }
            }
        }

        if (settings.isEnablePrintScreen()) {
            if (keyEvent.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
                CaptureImage.createInstance(settings, config);
            }
        }
    }

    /**
     * Removes any key that was released from {@link #keys}.
     *
     * @param keyEvent Information about the key that was released.
     */
    public void nativeKeyReleased(NativeKeyEvent keyEvent) {
        String key = NativeKeyEvent.getKeyText(keyEvent.getKeyCode());
        if (key.equalsIgnoreCase("Left Control") || key.equalsIgnoreCase("Right Control")) {
            key = "Ctrl";
        } else if (key.equalsIgnoreCase("Left Shift") || key.equalsIgnoreCase("Right Shift")) {
            key = "Shift";
        }

        if (keys.contains(key)) {
            keys.remove(key);
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    /**
     * Checks if every entry in inputted boolean array is true.
     *
     * @param array The {@link Boolean} array that is to be checked.
     * @return Returns true if all entries in the array are true.
     */
    private static boolean areAllTrue(boolean[] array) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }

        return true;
    }

    /**
     * Adds a new instance of {@link GlobalKeyListener} to the global key listener.
     *
     * @param settings The settings class that is passed in from {@link CreateTrayIcon}.
     * @param config   The config class that is passed in from {@link CreateTrayIcon}.
     */
    public static void beginListening(Settings settings, Config config) {
        Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(new GlobalKeyListener(settings, config));
    }
}
