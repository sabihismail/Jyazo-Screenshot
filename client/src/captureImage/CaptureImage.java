package captureImage;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settings.Config;
import settings.Settings;
import upload.Upload;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class allows for the capturing of a section of the screen through the
 * {@link Robot#createScreenCapture(Rectangle)} method.
 * <p>
 * The screen capture region start is selected upon pressing down on the mouse and the end of the capture is wherever
 * the user releases the mouse. The image is then uploaded to the desired server.
 *
 * @since 1.0
 */
public class CaptureImage extends JFrame {
    private JFXPanel fxPanel;
    private GraphicsContext gc;
    private int width, height;

    private boolean mousePressed = false;
    private boolean mouseDragged = false;
    private boolean mouseReleased = false;
    private double startX, startY, endX, endY, selectionWidth, selectionHeight;

    private Settings settings;
    private Config config;

    /**
     * Creates an overlay that allows for a visible screen capture region for any image using the mouse.
     *
     * @param settings This contains the user settings that is passed in from {@link #createInstance(Settings, Config)}.
     * @param config   This contains the encrypted configuration data that is passed in from
     *                 {@link #createInstance(Settings, Config)}.
     */
    public CaptureImage(Settings settings, Config config) {
        this.settings = settings;
        this.config = config;

        width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        setSize(width, height);
        setType(Type.UTILITY);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new java.awt.Color(0, 0, 0, 0));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        fxPanel = new JFXPanel();
        generateScene();
        getContentPane().add(fxPanel);

        setVisible(true);
    }

    /**
     * Creates {@link Canvas} and enables drawing of capture region.
     */
    private void generateScene() {
        Canvas canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        drawShapes();

        Pane root = new Pane();
        root.getChildren().add(canvas);
        Scene fxScene = new Scene(root);
        fxScene.setFill(Color.TRANSPARENT);
        fxScene.setCursor(Cursor.CROSSHAIR);

        fxScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dispose();
            }
        });

        fxScene.setOnMouseDragged(e -> {
            if (mousePressed && !mouseDragged)
                mouseDragged = true;

            if (!mouseReleased) {
                endX = e.getSceneX();
                endY = e.getSceneY();
                selectionWidth = Math.abs(endX - startX);
                selectionHeight = Math.abs(endY - startY);

                drawShapes();
            }
        });

        fxScene.setOnMousePressed(e -> {
            if (!mousePressed && !mouseReleased) {
                mousePressed = true;
                startX = e.getSceneX();
                startY = e.getSceneY();
            }
        });

        fxScene.setOnMouseReleased(e -> {
            if (mouseDragged && mousePressed && !mouseReleased) {
                mouseReleased = true;

                Platform.runLater(() -> gc.clearRect(0, 0, width, height));

                new Thread(this::saveImageAndUpload).start();
            }
        });

        fxPanel.setScene(fxScene);
    }

    /**
     * Saves image based on {@link #startX}, {@link #startY}, {@link #endX}, and {@link #endY} values which store the
     * information about the screen capture region size. These co-ordinates will be used to create a
     * {@link Rectangle} which will be passed into {@link Robot#createScreenCapture(Rectangle)} which will return a
     * {@link BufferedImage} of that region of the screeen.
     * <p>
     * If {@link Settings#saveAllImages} is {@code true}, the image will be stored in the directory located at
     * {@link Settings#saveDirectory}.
     * <p>
     * The image will then be uploaded to the url at {@link Config#server} and the {@link CaptureImage} instance will
     * be disposed using {@link #dispose()}.
     */
    private void saveImageAndUpload() {
        double minX = 0, minY = 0;
        if (startX < endX && startY < endY) {
            minX = startX;
            minY = startY;
            gc.clearRect(startX, startY, selectionWidth, selectionHeight);
        } else if (startX < endX && endY < startY) {
            minX = startX;
            minY = endY;
            gc.clearRect(startX, endY, selectionWidth, selectionHeight);
        } else if (endX < startX && endY < startY) {
            minX = endX;
            minY = endY;
            gc.clearRect(endX, endY, selectionWidth, selectionHeight);
        } else if (endX < startX && startY < endY) {
            minX = endX;
            minY = startY;
            gc.clearRect(endX, startY, selectionWidth, selectionHeight);
        }

        Rectangle screenRect = new Rectangle((int) minX, (int) minY, (int) selectionWidth, (int) selectionHeight);
        BufferedImage screenCapture = null;
        File tempFile = null;
        try {
            screenCapture = new Robot().createScreenCapture(screenRect);
        } catch (AWTException e) {
            e.printStackTrace();
        }

        try {
            tempFile = File.createTempFile("screenshot", ".png");
            ImageIO.write(screenCapture, "png", tempFile);

            if (settings.isSaveAllImages())
                ImageIO.write(screenCapture, "png",
                        new File(settings.getSaveDirectory() + Long.toString(System.currentTimeMillis()) + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Upload.uploadFile(tempFile, settings, config);

        dispose();
    }

    /**
     * Draws the screen capture selection overlay including the width and height of the selection in real time. Also
     * displays a transparent layer above the entire screen to show which part of the screen capture is not in the
     * selection.
     */
    private void drawShapes() {
        gc.clearRect(0, 0, width, height);

        gc.setGlobalAlpha(0.35);
        gc.setFill(Color.GREY);
        gc.fillRect(0, 0, width, height);
        if (startX < endX && startY < endY)
            gc.clearRect(startX, startY, selectionWidth, selectionHeight);
        else if (startX < endX && endY < startY)
            gc.clearRect(startX, endY, selectionWidth, selectionHeight);
        else if (endX < startX && endY < startY)
            gc.clearRect(endX, endY, selectionWidth, selectionHeight);
        else if (endX < startX && startY < endY)
            gc.clearRect(endX, startY, selectionWidth, selectionHeight);

        String x = "w: " + Integer.toString((int) selectionWidth);
        String y = "h: " + Integer.toString((int) selectionHeight);

        gc.setFont(Font.font("Calibri", FontWeight.BOLD, 24));

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affineTransform, true, true);
        java.awt.Font font = new java.awt.Font(gc.getFont().getName(), java.awt.Font.PLAIN, (int) gc.getFont().getSize());
        int textWidth = (int) (font.getStringBounds(x, frc).getWidth());
        int textHeight = (int) (font.getStringBounds(x, frc).getHeight());

        AffineTransform affineTransform2 = new AffineTransform();
        FontRenderContext frc2 = new FontRenderContext(affineTransform2, true, true);
        int textWidth2 = (int) (font.getStringBounds(y, frc2).getWidth());
        int textHeight2 = (int) (font.getStringBounds(y, frc2).getHeight());

        int largestWidth = Math.max(textWidth, textWidth2);
        int largestHeight = Math.max(textHeight, textHeight2);

        gc.setGlobalAlpha(1);
        gc.setFill(Color.BLACK);

        if (endX > startX && endY < startY)
            gc.fillRect(endX - largestWidth * 2 - 50, endY - largestHeight - largestHeight / 2, largestWidth * 2 + 50,
                    largestHeight + largestHeight / 2);
        else
            gc.fillRect(endX - largestWidth * 2 - 50, endY, largestWidth * 2 + 50, largestHeight + largestHeight / 2);

        gc.setFill(Color.WHITE);

        if (endX > startX && endY < startY) {
            gc.fillText(x, endX - largestWidth * 2 - 30, endY - largestHeight / 2);
            gc.fillText(y, endX - largestWidth - 20, endY - largestHeight / 2);
        } else {
            gc.fillText(x, endX - largestWidth * 2 - 30, endY + largestHeight);
            gc.fillText(y, endX - largestWidth - 20, endY + largestHeight);
        }
    }

    /**
     * Creates an instance of {@link CaptureImage}.
     * <p>
     * This method is used to maintain method naming consistency between the creation of {@link CaptureImage} and
     * {@link captureGIF.CaptureGIF}.
     *
     * @param settings This contains the user settings that is passed in from {@link tray.CreateTrayIcon} and is
     *                 immediately passed into {@link CaptureImage#CaptureImage(Settings, Config)}.
     * @param config   This contains the encrypted configuration data that is passed in from
     *                 {@link tray.CreateTrayIcon} and is immediately passed into
     *                 {@link CaptureImage#CaptureImage(Settings, Config)}.
     */
    public static void createInstance(Settings settings, Config config) {
        new CaptureImage(settings, config);
    }
}
