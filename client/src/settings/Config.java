package settings;

import org.json.JSONException;
import org.json.JSONObject;
import tools.Constants;
import tools.Encryption;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.*;
import java.security.GeneralSecurityException;

/**
 * Contains all configuration data. All {@link String} variables are salted and encrypted in AES. For an in-depth
 * viewing of the encryption method, look at {@link Encryption}.
 * <p>
 * This class contains the designated server where images will be uploaded to, the password for that server if it
 * exists, and an option to enable GIF uploading to Gfycat instead of uploading to the designated server due to the
 * large file sizes. If {@link #enableGfycatUpload} is set to true, the Gfycat Client ID and Client Secret are
 * required to upload the GIF.
 * <p>
 * All configuration data saves to {@link Constants#CONFIG_FILE} which by default is named "config.json".
 *
 * @since 1.0
 */
public class Config {
    private String salt;

    private String server = "";
    private String serverPassword = "";
    private boolean enableGfycatUpload = true;
    private String gfycatClientID = "";
    private String gfycatClientSecret = "";

    /**
     * Checks if configuration data file already exists. If it does not then a file will be created storing the
     * default values. If the file does exist, the default values will be replaced by the data contained in the file.
     */
    public Config() {
        File configFile = new File(Constants.CONFIG_FILE);

        if (configFile.exists()) {
            updateConfig();
        } else {
            saveConfig(server, serverPassword, enableGfycatUpload, gfycatClientID, gfycatClientSecret);
        }
    }

    /**
     * This method allows for current configuration data to be updated and will also store all data in the file
     * designated by {@link Constants#CONFIG_FILE}.
     *
     * @param server             The designated server which accepts uploaded images.
     * @param serverPassword     The password for that server if it exists.
     * @param enableGfycatUpload True if GIFs should be uploaded to Gfycat.
     * @param gfycatClientID     The Client ID for Gfycat. Required to upload to Gfycat.
     * @param gfycatClientSecret The Client Secret for Gfycat. Required to upload to Gfycat.
     */
    public void saveConfig(String server, String serverPassword, boolean enableGfycatUpload, String gfycatClientID,
                           String gfycatClientSecret) {
        this.server = server;
        this.serverPassword = serverPassword;
        this.enableGfycatUpload = enableGfycatUpload;
        this.gfycatClientID = gfycatClientID;
        this.gfycatClientSecret = gfycatClientSecret;

        try {
            File configFile = new File(Constants.CONFIG_FILE);
            configFile.getParentFile().mkdirs();
            configFile.createNewFile();

            salt = new String(Encryption.generateRandomSalt());

            SecretKeySpec key = Encryption.createSecretKey(salt.getBytes());

            JSONObject json = new JSONObject();

            json.put("salt", salt);
            json.put("server", Encryption.encrypt(server, key));
            json.put("serverPassword", Encryption.encrypt(serverPassword, key));
            json.put("enableGfycatUpload", enableGfycatUpload);
            json.put("gfycatClientID", Encryption.encrypt(gfycatClientID, key));
            json.put("gfycatClientSecret", Encryption.encrypt(gfycatClientSecret, key));

            BufferedWriter w = new BufferedWriter(new FileWriter(Constants.CONFIG_FILE));
            w.write(json.toString(6));
            w.newLine();
            w.close();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all data in the configuration file located at {@link Constants#CONFIG_FILE}.
     *
     * All encrypted data will first be decrypted by the salt contained in the configuration file and the
     * {@link Encryption} class.
     *
     * If an {@link IOException} is caught, the file will be recreated and all default data will be stored in that file.
     */
    private void updateConfig() {
        StringBuilder stringBuilder = new StringBuilder();
        File configFile = new File(Constants.CONFIG_FILE);

        try {
            BufferedReader r = new BufferedReader(new FileReader(configFile));
            String s;
            while ((s = r.readLine()) != null)
                stringBuilder.append(s);
            r.close();

            try {
                JSONObject json = new JSONObject(stringBuilder.toString());
                salt = json.getString("salt");

                SecretKeySpec key = Encryption.createSecretKey(salt.getBytes());

                if (!json.isNull("server")) {
                    server = Encryption.decrypt(json.getString("server"), key);
                } else {
                    server = "";
                }
                if (!json.isNull("serverPassword")) {
                    serverPassword = Encryption.decrypt(json.getString("serverPassword"), key);
                } else {
                    serverPassword = "";
                }
                enableGfycatUpload = json.isNull("enableGfycatUpload") || json.getBoolean("enableGfycatUpload");
                if (!json.isNull("gfycatClientID")) {
                    gfycatClientID = Encryption.decrypt(json.getString("gfycatClientID"), key);
                } else {
                    gfycatClientID = "";
                }
                if (!json.isNull("gfycatClientSecret")) {
                    gfycatClientSecret = Encryption.decrypt(json.getString("gfycatClientSecret"), key);
                } else {
                    gfycatClientID = "";
                }
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        } catch (IOException | JSONException e) {
            if (configFile.exists()) {
                configFile.delete();
            }

            JOptionPane.showMessageDialog(null, "The config file is corrupted! All values have been reset.");

            saveConfig(server, serverPassword, enableGfycatUpload, gfycatClientID, gfycatClientSecret);
        }
    }

    public String getServer() {
        return server;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public boolean isEnableGfycatUpload() {
        return enableGfycatUpload;
    }

    public String getGfycatClientID() {
        return gfycatClientID;
    }

    public String getGfycatClientSecret() {
        return gfycatClientSecret;
    }
}
