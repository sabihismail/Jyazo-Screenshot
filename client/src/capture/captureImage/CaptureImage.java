package capture.captureImage;

import capture.Callback;
import javafx.application.Platform;
import javafx.stage.Stage;
import settings.Config;
import settings.Settings;
import upload.Upload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * This class is specific to capturing a single image based on screen region capture.
 *
 * @since 1.0
 */
public class CaptureImage extends Callback {
    private Settings settings;
    private Config config;

    public CaptureImage(Settings settings, Config config) {
        this.settings = settings;
        this.config = config;
    }

    /**
     * On mouse release, captures a single frame using {@link Robot#createScreenCapture(Rectangle)} based on the
     * parameter 'selection' and then upload to the server.
     *
     * @param stage     The {@link Stage} from the screen region capture.
     * @param selection The screen region selection from the capture.
     */
    @Override
    public void onRelease(Stage stage, Rectangle selection) {
        BufferedImage screenCapture = null;
        File tempFile;
        try {
            screenCapture = new Robot().createScreenCapture(selection);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        try {
            tempFile = File.createTempFile("screenshot", ".png");
            ImageIO.write(Objects.requireNonNull(screenCapture), "png", tempFile);

            if (settings.isSaveAllImages()) {
                String output = settings.getSaveDirectory() + System.currentTimeMillis() + ".png";

                ImageIO.write(screenCapture, "png", new File(output));
            }

            Upload.uploadFile(tempFile, settings, config);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        Platform.runLater(stage::close);
    }
}
