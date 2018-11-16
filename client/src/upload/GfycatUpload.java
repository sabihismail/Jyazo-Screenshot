package upload;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Config;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Allows for GIFs to be uploaded to Gfycat through the official Gfycat API.
 * <p>
 * This method of uploading a GIF image to Gfycat is blocking and not asynchronous.
 * <p>
 * For reference, a slug is defined as the designated ID of an uploaded image.
 */
public class GfycatUpload {
    private static String API_ENDPOINT = "https://api.gfycat.com/v1/";
    private static String API_ENDPOINT_POST_KEY = API_ENDPOINT + "gfycats";
    private static String API_ENDPOINT_GET_STATUS = API_ENDPOINT + "gfycats/fetch/status/";

    private static String URL_START = "https://gfycat.com/";
    // private static String API_ENDPOINT_GET_INFORMATION = API_ENDPOINT + "gfycats/";

    private Config config;

    private String gfyURL;

    /**
     * @param config  The config class from {@link tray.CreateTrayIcon}. {@link Config#gfycatClientID} and
     *                {@link Config#gfycatClientSecret} are both required for uploading an image.
     * @param gifFile The GIF file that is to be uploaded.
     */
    public GfycatUpload(Config config, File gifFile) {
        this.config = config;

        String oAuthKey = generateOAuthKey();
        GfycatUploadData uploadInformation = retrieveUploadInformation(oAuthKey);

        gfyURL = URL_START + upload(uploadInformation, gifFile);
    }

    /**
     * Retrieves an oAuth key through the API allowing for one image upload. Requires both a valid Client ID stored at
     * {@link Config#gfycatClientID}, and a valid Client Secret stored at {@link Config#gfycatClientSecret}.
     *
     * @return The oAuth key that allows for permission to upload a GIF.
     */
    private String generateOAuthKey() {
        HttpClient httpClient = HttpClients.createMinimal();

        try {
            JSONObject input = new JSONObject();
            input.put("client_id", config.getGfycatClientID());
            input.put("client_secret", config.getGfycatClientSecret());
            input.put("grant_type", "client_credentials");

            HttpPost request = new HttpPost("https://api.gfycat.com/v1/oauth/token");
            StringEntity params = new StringEntity(input.toString());
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");

            EntityUtils.consume(entity);

            JSONObject obj = new JSONObject(responseString);

            try {
                return obj.getString("access_token");
            } catch (JSONException e) {
                JSONObject errorMessage = obj.getJSONObject("errorMessage");

                JOptionPane.showMessageDialog(null, "Error Code: " + errorMessage.getString("code") +
                        "\nError Message: \"" + errorMessage.getString("description") + "\"");

                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Retrieves the slug and the designated url where the GIF file should be uploaded.
     *
     * @param oAuthKey Requires the oAuthKey to retrieve this information from the API.
     * @return Returns {@link GfycatUploadData} which contains the designated slug and the desired upload url.
     */
    private GfycatUploadData retrieveUploadInformation(String oAuthKey) {
        try {
            URL urlObject = new URL(API_ENDPOINT_POST_KEY);
            HttpURLConnection con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestProperty("Authorization", "Bearer " + oAuthKey);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");

            BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = r.readLine()) != null)
                response.append(inputLine);
            r.close();

            JSONObject obj = new JSONObject(response.toString());
            String gfyName = obj.getString("gfyname");
            String postGIFURL = "https://" + obj.getString("uploadType");

            return new GfycatUploadData(gfyName, postGIFURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Uploads the image to the URL designated by Gfycat's API services.
     * <p>
     * This method then checks for the status of the URL and will continue to block until the status of that image has
     * completed encoding and is ready for viewing.
     *
     * @param uploadData Contains the slug of the image and the url where the file should be uploaded.
     * @param gifFile    The file that is to be uploaded.
     * @return Returns the slug of the uploaded GIF.
     */
    private String upload(GfycatUploadData uploadData, File gifFile) {
        try {
            HttpClient httpClient = HttpClients.createMinimal();
            HttpPost uploadFile = new HttpPost(uploadData.getURL());
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            builder.addTextBody("key", uploadData.getSlug());
            builder.addPart("file", new FileBody(gifFile));

            HttpEntity multipartEntity = builder.build();
            uploadFile.setEntity(multipartEntity);
            httpClient.execute(uploadFile);

            EntityUtils.consume(multipartEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String gfySlug;
        String getURLFull = API_ENDPOINT_GET_STATUS + uploadData.getSlug();

        URL getUrl;
        try {
            getUrl = new URL(getURLFull);
        } catch (MalformedURLException e) {
            e.printStackTrace();

            return "";
        }

        while (true) {
            try {
                HttpURLConnection urlConnection = (HttpURLConnection) getUrl.openConnection();

                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = r.readLine()) != null)
                    response.append(inputLine);
                r.close();

                JSONObject obj = new JSONObject(response.toString());

                String task = obj.getString("task");
                if (task.equals("encoding")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (task.equals("complete")) {
                    gfySlug = obj.getString("gfyname");

                    break;
                }

                if (!obj.isNull("mobileUrl")) {
                    gfySlug = obj.getString("gfyName");

                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return gfySlug;
    }

    public String getGfyURL() {
        return gfyURL;
    }

    private class GfycatUploadData {
        private String slug;
        private String url;

        public GfycatUploadData(String slug, String url) {
            this.slug = slug;
            this.url = url;
        }

        public String getSlug() {
            return slug;
        }

        public String getURL() {
            return url;
        }
    }

    private class GIFData {
        private String gfyURL;
        private String gifURL;
        private String mp4URL;
        private int width;
        private int height;

        public GIFData(String gfyURL, String gifURL, String mp4URL, int width, int height) {
            this.gfyURL = gfyURL;
            this.gifURL = gifURL;
            this.mp4URL = mp4URL;
            this.width = width;
            this.height = height;
        }

        public String getGfyURL() {
            return gfyURL;
        }

        public String getGifURL() {
            return gifURL;
        }

        public String getMp4URL() {
            return mp4URL;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
