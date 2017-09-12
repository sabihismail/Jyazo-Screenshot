package tray;

import captureGIF.CaptureGIF;
import captureImage.CaptureImage;
import captureSettings.CaptureSettings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import settings.Config;
import settings.Settings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main class for the screen capturing program.
 * <p>
 * This class is a JavaFX {@link Application} with {@link java.awt} package utilities like {@link TrayIcon} and
 * {@link JFrame}. This class will have a JavaFX thread continuously running even though the {@link CaptureImage} and
 * {@link captureGIF.CaptureGIF} are both {@link JFrame}. This is due to the {@link CaptureSettings} class which
 * opens a JavaFX {@link Stage}.
 *
 * @since 1.0
 */
public class CreateTrayIcon extends Application {
    private Settings settingsClass;
    private Config config;

    private JMenuItem captureImage = new JMenuItem();
    private JMenuItem captureGIF = new JMenuItem();
    private JMenuItem viewAllImages = new JMenuItem("View All Images");
    private JMenuItem settingsTray = new JMenuItem("Settings");
    private JMenuItem exit = new JMenuItem("Exit");

    /**
     * Fonts for the {@link TrayIconAWT} text.
     */
    private final Font TRAY_FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font TRAY_FONT_BOLD = TRAY_FONT_REGULAR.deriveFont(Font.BOLD);

    /**
     * Primarily loads the {@link GlobalKeyListener} which listens for user keyboard input, {@link Settings} which holds
     * all settings that do not need to be hashed, and {@link Config} which contains all the important information that
     * requires hashing.
     * <p>
     * This method then creates the {@link TrayIconAWT} which will be located on the System Tray.
     *
     * @param stage The primary {@link Stage} that is included by default when extending {@link Application}.
     * @throws Exception All exceptions are thrown by default for any {@link Application] implementation.
     */
    @Override
    public void start(Stage stage) throws Exception {
        initializeJavaFX(stage);

        settingsClass = new Settings();
        config = new Config();

        GlobalKeyListener.beginListening(settingsClass, config);

        captureImage = new JMenuItem("Capture Image (" +
                settingsClass.getCaptureImageShortcut().replaceAll(" ", " + ") + ")");
        captureGIF = new JMenuItem("Capture GIF (" +
                settingsClass.getCaptureGIFShortcut().replaceAll(" ", " + ") + ")");

        captureImage.setFont(TRAY_FONT_BOLD);
        captureGIF.setFont(TRAY_FONT_BOLD);
        viewAllImages.setFont(TRAY_FONT_REGULAR);
        settingsTray.setFont(TRAY_FONT_REGULAR);
        exit.setFont(TRAY_FONT_REGULAR);

        JPopupMenu popup = new JPopupMenu();
        popup.add(captureImage);
        popup.add(captureGIF);
        popup.addSeparator();
        popup.add(viewAllImages);
        popup.add(settingsTray);
        popup.addSeparator();
        popup.add(exit);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                captureImage.setText("Capture Image (" +
                        settingsClass.getCaptureImageShortcut().replaceAll(" ", " + ") + ")");
                captureGIF.setText("Capture GIF (" +
                        settingsClass.getCaptureGIFShortcut().replaceAll(" ", " + ") + ")");
            }
        }, 1, 1000);

        BufferedImage tempImage = ImageIO.read(this.getClass().getResourceAsStream("/images/icon.png"));
        int tempWidth = new TrayIcon(tempImage).getSize().width;
        Image iconImage = tempImage.getScaledInstance(tempWidth, -1, Image.SCALE_SMOOTH);

        TrayIconAWT icon = new TrayIconAWT(iconImage, popup);
        icon.addMouseListener(new IconClickListener());

        SystemTray tray = SystemTray.getSystemTray();
        tray.add(icon);

        captureImage.addActionListener(e -> CaptureImage.createInstance(settingsClass, config));
        captureGIF.addActionListener(e -> CaptureGIF.createInstance());
        viewAllImages.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(settingsClass.getSaveDirectory()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        settingsTray.addActionListener(e ->
                Platform.runLater(() -> CaptureSettings.createInstance(settingsClass, config)));
        exit.addActionListener(e -> {
            tray.remove(icon);
            System.exit(0);
        });
    }

    /**
     * Disables exit upon last {@link Stage} being closed. Shows and then hides the {@link Stage} which creates the
     * JavaFX {@link Application} thread for use by {@link CaptureSettings}.
     *
     * @param stage The JavaFX stage which allows for the JavaFX thread to be created.
     * @throws Exception Throws all exceptions just as {@link Application#start(Stage)} does.
     */
    private void initializeJavaFX(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.show();
        stage.hide();
    }

    /**
     * Primary main method for screenshot application.
     *
     * @param args No arguments are supplied/utilized.
     */
    public static void main(String[] args) {
        WindowInformation.beginObservingWindows();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException ignored) {
        }

        Application.launch();
    }

    /**
     * Listener for when {@link TrayIconAWT} is clicked. By default, clicking the {@link TrayIconAWT} will open the
     * {@link JFrame} for regular image screenshot.
     */
    private class IconClickListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            captureGIF.setEnabled(settingsClass.isEnableGIF());
            viewAllImages.setEnabled(settingsClass.isSaveAllImages());

            if (e.getButton() == MouseEvent.BUTTON1) {
                CaptureImage.createInstance(settingsClass, config);
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}
