package captureGIF;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.FontWeight;
import settings.Config;
import settings.Settings;
import tools.Constants;
import upload.Upload;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class allows for the capturing of multiple frames exported as a GIF through the
 * {@link Robot#createScreenCapture(Rectangle)} method.
 * <p>
 * This class captures the screen every {@link CaptureGIF#delay} milliseconds and adds it to a {@link List<ImageData>}
 * where it will then be saved to a GIF file when the user clicks the stop button on the overlay.
 *
 * Due to the amount of data ({@link BufferedImage} byte array data) stored in memory, the creation of a GIF file is
 * a separate jar file that is instantiated every time the Capture GIF button is clicked in the tray.
 *
 * @since 1.0
 */
public class CaptureGIF extends JFrame {
    private Scene fxScene;
    private JFXPanel fxPanel;
    private GraphicsContext gc;
    private int width, height;

    private boolean mousePressed = false;
    private boolean mouseDragged = false;
    private boolean mouseReleased = false;
    private double startX, startY, endX, endY, selectionWidth, selectionHeight;

    private Settings settings;
    private Config config;

    private List<ImageData> imageByteArray;
    private int delay = 100;

    private Image cancel, pause, resume, complete;
    private BufferedImage cursor;
    private boolean completed, paused;
    private int buttonGap = 10;
    private int buttonYGap = 10;

    /**
     * Creates a {@link JFXPanel} that contains a JavaFX {@link Canvas} which allows for the GIF region capture to be
     * displayed in an overlay.
     */
    public CaptureGIF() {
        settings = new Settings();
        config = new Config();
        imageByteArray = new ArrayList<>();

        try {
            cursor = ImageIO.read(this.getClass().getResourceAsStream("/images/cursor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        width = (int) toolkit.getScreenSize().getWidth();
        height = (int) toolkit.getScreenSize().getHeight();

        setSize(width, height);
        setType(Type.UTILITY);
        setUndecorated(true);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));
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
        Platform.runLater(() -> {
            Canvas canvas = new Canvas(width, height);
            gc = canvas.getGraphicsContext2D();

            drawShapes();

            Pane root = new Pane();
            root.getChildren().add(canvas);
            fxScene = new Scene(root);
            fxScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            fxScene.setCursor(javafx.scene.Cursor.CROSSHAIR);
            fxScene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    dispose();
                    System.exit(0);
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
                    saveImageAndUpload();
                }
            });

            fxPanel.setScene(fxScene);
        });
    }

    /**
     * Creates a managed thread pool which allows for continuous image capturing and saving of the desired screen
     * capture every {@link CaptureGIF#delay} milliseconds.
     * <p>
     * Also creates the pause {@link CaptureGIF#pause}, complete {@link CaptureGIF#complete}, and cancel
     * {@link CaptureGIF#cancel} image buttons on the overlay.
     * <p>
     * When the image is confirmed, the {@link ScheduledExecutorService} loop will instead create a new thread which
     * will combine all the {@link ImageData} stored in the {@link CaptureGIF#imageByteArray} into a GIF image.
     * This occurs in the background. The {@link ScheduledExecutorService} will then be shutdown.
     */
    private void saveImageAndUpload() {
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(10);
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> {
            gc.clearRect(0, 0, width, height);

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

            cancel = new Image(this.getClass().getResourceAsStream("/images/cancel.png"));
            pause = new Image(this.getClass().getResourceAsStream("/images/pause.png"));
            complete = new Image(this.getClass().getResourceAsStream("/images/complete.png"));
            resume = new Image(this.getClass().getResourceAsStream("/images/resume.png"));

            double centerX = minX + selectionWidth / 2;
            double firstImageX = centerX - pause.getWidth() / 2;
            double leftImageX = firstImageX - cancel.getWidth() - buttonGap;
            double rightImageX = firstImageX + complete.getWidth() + buttonGap;

            double y = minY + selectionHeight + buttonYGap;

            gc.clearRect(0, 0, width, height);
            fxScene.setCursor(Cursor.NONE);

            gc.setLineWidth(1);
            gc.strokeRect(startX, startY, selectionWidth, selectionHeight);

            gc.drawImage(pause, firstImageX, y);
            gc.drawImage(cancel, leftImageX, y);
            gc.drawImage(complete, rightImageX, y);

            gc.getCanvas().setOnMouseClicked(e -> {
                if (!paused) {
                    if (clickIsWithin(pause, firstImageX, y, e.getX(), e.getY())) {
                        paused = true;

                        Platform.runLater(() -> {
                            gc.clearRect(firstImageX, y, pause.getWidth(), pause.getHeight());

                            gc.drawImage(resume, firstImageX, y);
                        });
                    }
                } else {
                    if (clickIsWithin(pause, firstImageX, y, e.getX(), e.getY())) {
                        paused = false;

                        Platform.runLater(() -> {
                            gc.clearRect(firstImageX, y, pause.getWidth(), pause.getHeight());

                            gc.drawImage(pause, firstImageX, y);
                        });
                    }
                }

                if (clickIsWithin(complete, rightImageX, y, e.getX(), e.getY())) {
                    paused = true;

                    completed = true;
                } else if (clickIsWithin(cancel, leftImageX, y, e.getX(), e.getY())) {
                    System.exit(0);
                }
            });
        });

        Robot finalRobot = robot;
        exec.scheduleAtFixedRate(() -> {
            if (!completed) {
                if (!paused) {
                    Rectangle screenRect = new Rectangle((int) startX - 1, (int) startY - 1, (int) selectionWidth + 2, (int) selectionHeight + 2);
                    byte[] screenCaptureArray = null;
                    try {
                        screenCaptureArray = readBytesAndStore(finalRobot.createScreenCapture(screenRect),
                                MouseInfo.getPointerInfo().getLocation());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    imageByteArray.add(new ImageData(screenCaptureArray, imageByteArray.size()));
                }
            } else {
                Thread thread = new Thread(() -> {
                    File tempFile = createGIF();

                    Upload.uploadFile(tempFile, settings, config);

                    dispose();
                    System.exit(0);
                });
                thread.start();

                gc.clearRect(0, 0, width, height);

                exec.shutdownNow();
            }
        }, 0, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Checks if user click is within the specified {@link Image} that is on the overlay.
     *
     * @param image  The {@link Image} that is going to be analyzed.
     * @param x      The x position of the image.
     * @param y      The y position of the image.
     * @param clickX The x co-ordinate of the click.
     * @param clickY The y co-ordinate of the click.
     * @return Returns true if the inputted image button was clicked.
     */
    private boolean clickIsWithin(Image image, double x, double y, double clickX, double clickY) {
        return Math.pow(clickX - (x + image.getWidth() / 2), 2) + Math.pow(clickY - (y + image.getHeight() / 2), 2) <
                Math.pow(image.getHeight() / 2, 2);
    }

    /**
     * Reads the {@link BufferedImage} data and stores it as a png format byte array.
     * <p>
     * Also depending on if the cursor was in view, draws an image of a cursor at the location of the cursor when the
     * screen was captured.
     *
     * @param screenCapture The result of {@link Robot#createScreenCapture(Rectangle)} which is to be modified.
     * @param location      The current location of the mouse when the {@link Robot#createScreenCapture(Rectangle)} was
     *                      called.
     * @return Returns a byte array of the image.
     * @throws IOException Should never throw IOException as there is no reading of files on the hard drive.
     */
    private byte[] readBytesAndStore(BufferedImage screenCapture, Point location) throws IOException {
        Graphics graphics = screenCapture.getGraphics();
        if (location.x - startX < selectionWidth && location.y - startY < selectionHeight)
            graphics.drawImage(cursor, (int) (location.x - startX), (int) (location.y - startY), null);

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("png").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

        jpgWriter.setOutput(outputStream);
        jpgWriter.write(null, new IIOImage(screenCapture, null, null), jpgWriteParam);

        jpgWriter.dispose();

        return compressed.toByteArray();
    }

    /**
     * Iterates through every {@link ImageData} in {@link CaptureGIF#imageByteArray} and stores it to a temporary GIF
     * file. This method creates and utilizes the {@link GifSequenceWriter} to create the GIF image.
     * <p>
     * The GIF image is set to loop continuously.
     *
     * @return Returns the GIF file that was created.
     */
    private File createGIF() {
        File file = new File("temp.gif");

        if (imageByteArray.size() > 1) {
            try {
                InputStream in = new ByteArrayInputStream(imageByteArray.get(0).getImageArray());
                BufferedImage imageFromByte = ImageIO.read(in);

                ImageOutputStream output = new FileImageOutputStream(file);
                GifSequenceWriter writer = new GifSequenceWriter(output, imageFromByte.getType(), delay, true);

                for (int i = 0; i < imageByteArray.size(); i++) {
                    ImageData imageObject = imageByteArray.get(i);

                    if (imageObject.getImageNumber() == i) {
                        in = new ByteArrayInputStream(imageObject.getImageArray());
                        imageFromByte = ImageIO.read(in);

                        writer.writeToSequence(imageFromByte);
                    }
                }

                writer.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * Draws the screen capture selection overlay including the width and height of the selection in real time. Also
     * displays a transparent layer above the entire screen to show which part of the screen capture is not in the
     * selection.
     */
    private void drawShapes() {
        gc.clearRect(0, 0, width, height);

        gc.setGlobalAlpha(0.35);
        gc.setFill(javafx.scene.paint.Color.GREY);
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

        gc.setFont(javafx.scene.text.Font.font("Calibri", FontWeight.BOLD, 24));

        AffineTransform affineTransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affineTransform, true, true);
        Font font = new Font(gc.getFont().getName(), java.awt.Font.PLAIN, (int) gc.getFont().getSize());
        int textWidth = (int) (font.getStringBounds(x, frc).getWidth());
        int textHeight = (int) (font.getStringBounds(x, frc).getHeight());

        AffineTransform affineTransform2 = new AffineTransform();
        FontRenderContext frc2 = new FontRenderContext(affineTransform2, true, true);
        int textWidth2 = (int) (font.getStringBounds(y, frc2).getWidth());
        int textHeight2 = (int) (font.getStringBounds(y, frc2).getHeight());

        int largestWidth = Math.max(textWidth, textWidth2);
        int largestHeight = Math.max(textHeight, textHeight2);

        gc.setGlobalAlpha(1);
        gc.setFill(javafx.scene.paint.Color.BLACK);

        if (endX > startX && endY < startY)
            gc.fillRect(endX - largestWidth * 2 - 50, endY - largestHeight - largestHeight / 2, largestWidth * 2 + 50,
                    largestHeight + largestHeight / 2);
        else
            gc.fillRect(endX - largestWidth * 2 - 50, endY, largestWidth * 2 + 50, largestHeight + largestHeight / 2);

        gc.setFill(javafx.scene.paint.Color.WHITE);

        if (endX > startX && endY < startY) {
            gc.fillText(x, endX - largestWidth * 2 - 30, endY - largestHeight / 2);
            gc.fillText(y, endX - largestWidth - 20, endY - largestHeight / 2);
        } else {
            gc.fillText(x, endX - largestWidth * 2 - 30, endY + largestHeight);
            gc.fillText(y, endX - largestWidth - 20, endY + largestHeight);
        }
    }

    /**
     * Creates a new instance of the corresponding GIF capture tool.
     *
     * See the class description for an explanation as to why the GIF capture is separate from the other functions of
     * this project.
     */
    public static void createInstance() {
        File file = new File(Constants.PROGRAM_NAME + "GIF.jar");
        if (!file.exists()) {
            file = new File(Constants.PROGRAM_NAME + "GIF.exe");

            if (!file.exists()) {
                JOptionPane.showMessageDialog(null,
                        "Corrupt or missing files, please reinstall the program. The program will now exit.");
                System.exit(0);
            }
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CaptureGIF();
    }

    /**
     * Stores the image data and the index of the image in the GIF.
     */
    public class ImageData {
        private byte[] imageArray;
        private int imageNumber;

        public ImageData(byte[] imageArray, int imageNumber) {
            this.imageArray = imageArray;
            this.imageNumber = imageNumber;
        }

        public byte[] getImageArray() {
            return imageArray;
        }

        public int getImageNumber() {
            return imageNumber;
        }
    }
}