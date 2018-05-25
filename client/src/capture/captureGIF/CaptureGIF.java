package capture.captureGIF;

import capture.Callback;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import settings.Config;
import settings.Settings;
import upload.Upload;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class is specific to capturing a GIF based on screen region capture.
 *
 * @since 1.0
 */
public class CaptureGIF extends Callback {
    private final int DELAY_IN_MILLISECONDS = 50;
    private final int BUTTON_GAP_X = 10;
    private final int BUTTON_GAP_Y = 10;

    private Settings settings;
    private Config config;

    private File tempFile;
    private GifSequenceWriter gifWriter;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    private BufferedImage screenImageCapture;

    private ImageView cancel, pause, resume, complete;
    private BufferedImage cursor;
    private boolean completed, paused;

    /**
     * Initialize GIF writer and create GIF temp file as well as load all GUI images.
     */
    public CaptureGIF(Settings settings, Config config) {
        this.settings = settings;
        this.config = config;

        try {
            cursor = ImageIO.read(this.getClass().getResourceAsStream("/images/cursor.png"));
            cancel = new ImageView(new javafx.scene.image.Image(this.getClass().getResourceAsStream("/images/cancel.png")));
            pause = new ImageView(new javafx.scene.image.Image(this.getClass().getResourceAsStream("/images/pause.png")));
            complete = new ImageView(new javafx.scene.image.Image(this.getClass().getResourceAsStream("/images/complete.png")));
            resume = new ImageView(new Image(this.getClass().getResourceAsStream("/images/resume.png")));

            tempFile = File.createTempFile("TempGIF" + System.currentTimeMillis(), ".gif");
            gifWriter = new GifSequenceWriter(new FileImageOutputStream(tempFile), BufferedImage.TYPE_INT_RGB,
                    DELAY_IN_MILLISECONDS, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * On mouse release, create a border overlay for what the GIF is capturing and begin execution new threads to
     * capture the screen every {@link #DELAY_IN_MILLISECONDS} milliseconds.
     * <p>
     * Also introduce some buttons that the user can interact with to pause/resume, cancel, and complete the capture.
     *
     * Upon completion, upload the image to the server or Gfycat depending on {@link Config#isEnableGfycatUpload()}.
     *
     * @param stage     The {@link Stage} from the screen region capture.
     * @param selection The screen region selection from the capture.
     */
    @Override
    public void onRelease(Stage stage, Rectangle selection) {
        Rectangle outline = new Rectangle((int) selection.getMinX() - 1, (int) selection.getMinY(),
                (int) selection.getWidth() + 2, (int) selection.getHeight() + 2);

        Platform.runLater(() -> {
            double centerX = selection.getWidth() / 2;
            double middleImageX = centerX - pause.getImage().getWidth() / 2;
            double leftImageX = middleImageX - cancel.getImage().getWidth() - BUTTON_GAP_X;
            double rightImageX = middleImageX + complete.getImage().getWidth() + BUTTON_GAP_X;

            double y = selection.getHeight() + BUTTON_GAP_Y;

            Pane pane = new Pane();
            pane.prefWidthProperty().bind(stage.widthProperty());
            pane.prefHeightProperty().bind(stage.heightProperty());

            Pane outlinePane = new Pane();
            outlinePane.setMinWidth(outline.getWidth());
            outlinePane.setMinHeight(outline.getHeight());
            outlinePane.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

            Scene scene = new Scene(pane, outline.getWidth(),
                    outline.getHeight() + pause.getImage().getHeight() + BUTTON_GAP_Y);
            scene.setFill(Color.TRANSPARENT);

            pause.setTranslateX(middleImageX);
            pause.setTranslateY(y);
            pause.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                paused = true;

                pane.getChildren().remove(pause);
                pane.getChildren().add(resume);
            });

            resume.setTranslateX(middleImageX);
            resume.setTranslateY(y);
            resume.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                paused = false;

                pane.getChildren().remove(resume);
                pane.getChildren().add(pause);
            });

            cancel.setTranslateX(leftImageX);
            cancel.setTranslateY(y);
            cancel.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> Platform.runLater(stage::close));

            complete.setTranslateX(rightImageX);
            complete.setTranslateY(y);
            complete.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                paused = true;
                completed = true;

                try {
                    gifWriter.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                new Thread(() -> Upload.uploadFile(tempFile, settings, config)).start();

                stage.close();
                executorService.shutdownNow();
            });

            pane.getChildren().add(outlinePane);
            pane.getChildren().add(cancel);
            pane.getChildren().add(pause);
            pane.getChildren().add(complete);

            stage.setX(outline.getMinX());
            stage.setY(outline.getMinY());
            stage.setScene(scene);
        });

        try {
            Robot robot = new Robot();

            executorService.scheduleAtFixedRate(() -> {
                if (!completed && !paused) {
                    try {
                        screenImageCapture = captureAndCheckCursorPosition(robot.createScreenCapture(selection),
                                selection, MouseInfo.getPointerInfo().getLocation());

                        gifWriter.writeToSequence(screenImageCapture);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add cursor image if it was in view.
     *
     * @param selection The screen selection
     * @param location  The current location of the mouse when the {@link Robot#createScreenCapture(Rectangle)} was
     *                  called.
     * @return Returns the {@link BufferedImage} after cursor was added if it was in view.
     */
    private BufferedImage captureAndCheckCursorPosition(BufferedImage image, Rectangle selection, Point location) {
        Graphics graphics = image.getGraphics();
        if (location.x - selection.getMinX() < selection.getWidth() &&
                location.y - selection.getMinY() < selection.getHeight()) {
            graphics.drawImage(cursor,
                    (int) (location.x - selection.getMinX()),
                    (int) (location.y - selection.getMinY()), null);
        }

        return image;
    }
}