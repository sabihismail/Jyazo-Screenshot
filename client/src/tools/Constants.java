package tools;

/**
 * General constants used by multiple classes.
 * <p>
 * These constants are kept in one class instead of multiple classes for ease of editing.
 *
 * @since 1.0
 */
public class Constants {
    /**
     * Developer's constants
     */
    public static final String PROGRAM_NAME = "Jyazo";
    public static final String CREATOR = "ArkaPrime";
    public static final String GITHUB = "http://github.com/sabihismail/Jyazo/";

    /**
     * The save directory for all files created by the program.
     */
    private static final String SAVE_DIRECTORY = System.getProperty("user.home") + "\\" + CREATOR + "\\" +
            PROGRAM_NAME + "\\";

    /**
     * The default folder to store images if {@link settings.Settings#saveAllImages} is set to true.
     */
    public static final String DEFAULT_ALL_IMAGES_FOLDER = SAVE_DIRECTORY + "All Images\\";

    /**
     * The default keyboard shortcut to initiate image capture.
     */
    public static final String DEFAULT_IMAGE_SHORTCUT = "Ctrl Shift C";

    /**
     * The default keyboard shortcut to initiate GIF capture.
     */
    public static final String DEFAULT_GIF_SHORTCUT = "Ctrl Shift G";

    /**
     * The default settings file save location
     */
    public static final String SETTINGS_FILE = SAVE_DIRECTORY + "settings.cfg";

    /**
     * The default configuration file save location
     */
    public static final String CONFIG_FILE = SAVE_DIRECTORY + "config.json";
}
