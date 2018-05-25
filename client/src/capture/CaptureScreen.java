package capture;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * This class allows for the capturing of a section of the screen through the
 * {@link Robot#createScreenCapture(Rectangle)} method.
 * <p>
 * The screen capture region start is selected upon pressing down on the mouse and the end of the capture is wherever
 * the user releases the mouse. The image is then uploaded to the desired server.
 *
 * @since 1.1
 */
public class CaptureScreen {
    private Stage stage;
    private GraphicsContext gc;
    private int width, height;

    private boolean mousePressed = false;
    private boolean mouseDragged = false;
    private boolean mouseReleased = false;
    private double startX, startY, endX, endY, selectionWidth, selectionHeight;

    private Callback callback;

    /**
     * Creates an overlay that allows for a visible screen capture region for any image using the mouse.
     *
     * @param bounds Bounds of the specified window.
     */
    private CaptureScreen(Callback callback, Rectangle bounds) {
        this.callback = callback;

        this.width = (int) bounds.getWidth();
        this.height = (int) bounds.getHeight();

        Platform.runLater(this::generateScene);
    }

    /**
     * Creates {@link Canvas} and enables drawing of capture region.
     *
     * On mouse release, execute the {@link Callback#onRelease(Stage, Rectangle)} function based on the parameter passed
     * into the constructor {@link #callback}.
     */
    private void generateScene() {
        stage = new Stage();
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.initStyle(StageStyle.TRANSPARENT);

        Canvas canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        drawShapes();

        Pane root = new Pane();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setCursor(Cursor.CROSSHAIR);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        scene.setOnMouseDragged(e -> {
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

        scene.setOnMousePressed(e -> {
            if (!mousePressed && !mouseReleased) {
                mousePressed = true;
                startX = e.getSceneX();
                startY = e.getSceneY();
            }
        });

        scene.setOnMouseReleased(e -> {
            if (mouseDragged && mousePressed && !mouseReleased) {
                mouseReleased = true;

                Platform.runLater(() -> gc.clearRect(0, 0, width, height));

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

                Rectangle selection = new Rectangle((int) minX, (int) minY, (int) selectionWidth, (int) selectionHeight);

                new Thread(() -> this.callback.onRelease(stage, selection)).start();
            }
        });

        stage.setScene(scene);

        stage.showAndWait();
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
     * Creates an instance of {@link CaptureScreen}.
     * <p>
     * This method is used to maintain method naming consistency between the creation of {@link CaptureScreen} and
     * {@link capture.captureGIF.CaptureGIF}.
     * <p>
     * As of 1.1, also calculates the size of the screen based on the amount of monitors on the client.
     *
     * @param callback The implementation function that runs after the mouse is released on screen capture.
     */
    public static void createInstance(Callback callback) {
        int w = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int h = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

        Rectangle bounds = new Rectangle(w, h);

        new CaptureScreen(callback, bounds);
        /*
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice device : ge.getScreenDevices()) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();

            width += bounds.getWidth();
            height += bounds.getWidth();

            x = (int) Math.min(x, bounds.getX());
            y = (int) Math.min(y, bounds.getY());
        }

        new CaptureImage(settings, config, new Rectangle(x, y, width, height));
        */
    }
}
