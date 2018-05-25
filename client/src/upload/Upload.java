package upload;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Config;
import settings.Settings;
import tools.Logging;
import tray.WindowInformation;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for handling where the created image should be uploaded.
 *
 * @since 1.0
 */
public class Upload {
    /**
     * Checks if the image is a GIF and if so, will upload the image to Gfycat's servers. If the image is not a GIF,
     * the image will be uploaded to the main server designated by {@link Config#server}.
     *
     * @param imageFile The image that will be uploaded.
     * @param settings  The settings class retrieved from {@link tray.CreateTrayIcon}.
     * @param config    The config class retrieved from {@link tray.CreateTrayIcon}.
     */
    public static void uploadFile(File imageFile, Settings settings, Config config) {
        String result;

        if (imageFile.getName().endsWith("gif")) {
            GfycatUpload gfycatUpload = new GfycatUpload(config, imageFile);

            result = gfycatUpload.getGfyURL();
        } else {
            result = uploadToServer(imageFile, config);
        }

        if (!result.equals("")) {
            playSound(settings);

            copyToClipboard(result);
            openLink(result);

            imageFile.delete();
        }
    }

    /**
     * Plays sound on a new thread if {@link Settings#enableSound} is true.
     *
     * @param settings The settings class retrieved from {@link tray.CreateTrayIcon}.
     */
    private static void playSound(Settings settings) {
        if (settings.isEnableSound()) {
            new Thread(() -> {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(Upload.class
                            .getResourceAsStream("/sounds/sound.wav"));
                    clip.open(inputStream);
                    clip.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Uploads the image to the server designated by {@link Config#server}. Also applies the server password designated
     * by {@link Config#serverPassword} if applicable, and the title of the last viewed window designated by
     * {@link WindowInformation#ACTIVE_WINDOW}.
     * <p>
     * For an example of a php web script file, please check {@link tools.Constants#GITHUB} for more information.
     *
     * @param imageFile The image file that is to be uploaded.
     * @param config    The config class with the data for the server and
     * @return Returns the URL to the image
     */
    private static String uploadToServer(File imageFile, Config config) {
        JSONObject obj = null;
        try {
            ContentType contentType = ContentType.create(Files.probeContentType(Paths.get(imageFile.getAbsolutePath())));

            HttpClient httpClient = HttpClients.createMinimal();
            HttpEntity httpEntity = MultipartEntityBuilder.create()
                    .addBinaryBody("uploaded_image", imageFile, contentType, imageFile.getName())
                    .build();

            Header[] headers;
            if (config.getServerPassword() == null || config.getServerPassword().equals("")) {
                headers = new Header[1];
            } else {
                headers = new Header[2];

                headers[1] = new BasicHeader("uploadpassword", config.getServerPassword());
            }

            headers[0] = new BasicHeader("title", WindowInformation.ACTIVE_WINDOW);

            HttpPost httpRequest = new HttpPost(config.getServer());
            httpRequest.setHeaders(headers);
            httpRequest.setEntity(httpEntity);

            HttpResponse httpResponse = httpClient.execute(httpRequest);
            String response = EntityUtils.toString(httpResponse.getEntity());

            try {
                obj = new JSONObject(response);
            } catch (JSONException e) {
                System.out.println(response);
                e.printStackTrace();
            }
        } catch (IOException e) {
            Logging.log("Internet connection is not established or the there was a problem connecting to the " +
                    "server.", e);

            return "";
        }

        if (obj != null && obj.getBoolean("success")) {
            return obj.getString("output");
        } else {
            if (obj != null) {
                JOptionPane.showMessageDialog(null, "The server responded with:\n" +
                        obj.getString("error"));
            }

            return "";
        }
    }

    /**
     * Copy the url to the clipboard.
     *
     * @param text The text which will be copied to the clipboard.
     */
    private static void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(text), null);
    }

    /**
     * Opens any URL in the default browser.
     *
     * @param url The URL which will be opened.
     */
    private static void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {
            Logging.log("Opening the URL in the browser is not supported on your system.", e);
        }
    }
}
