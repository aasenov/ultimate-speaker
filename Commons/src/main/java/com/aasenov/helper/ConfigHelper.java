package com.aasenov.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.AbstractMap;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Use this class to retrieve various configuration variables.
 */
public class ConfigHelper {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(ConfigHelper.class);

    /**
     * File name under which we will store system configuration.
     */
    private static String CONFIG_FILE_NAME = "ultimateSpeaker.config";

    /**
     * Default port to use for Rest API interface.
     */
    public static int DEFAULT_REST_API_LISTEN_PORT = 8181;

    /**
     * Default directory under which we will store uploaded files.
     */
    private static String DEFAULT_STORAGE_DIR = PathHelper.getJarContainingFolder();
    /**
     * Static instance of this class.
     */
    private static ConfigHelper sInstance;

    /**
     * Current configuration.
     */
    private Properties mCurrentConfig;

    /**
     * Private constructor to avoid unwanted initialization.
     */
    private ConfigHelper() {
        mCurrentConfig = new Properties();
        loadConfiguration();
    }

    /**
     * Retrieve instance of this class.
     * 
     * @return Initialized {@link ConfigHelper} instance.
     */
    public static synchronized ConfigHelper getInstance() {
        if (sInstance == null) {
            sInstance = new ConfigHelper();
        }
        return sInstance;
    }

    /**
     * Retrieve value of given property;
     * 
     * @param property -property to retrieve.
     * @return Value of given property.
     */
    public String getConfigPropertyValue(ConfigProperty property) {
        return mCurrentConfig.getProperty(property.getValue());
    }

    /**
     * Update value of given property;
     * 
     * @param property -property to update.
     * @param value - value to set.
     */
    public void setConfigPropertyValue(ConfigProperty property, String value) {
        mCurrentConfig.setProperty(property.getValue(), value);
    }

    /**
     * Store current configuration in file system.
     */
    public void storeConfiguration() {
        File confFile = new File(PathHelper.getJarContainingFolder(), CONFIG_FILE_NAME);
        if (confFile.exists()) {
            confFile.delete();
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(confFile);
            mCurrentConfig.store(out, "Ultimate Speaker configuration file");
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Load configuration file from fileSystem.
     */
    private void loadConfiguration() {
        File confFile = new File(PathHelper.getJarContainingFolder(), CONFIG_FILE_NAME);
        if (confFile.exists() && confFile.canWrite()) {
            // load saved configuration
            InputStream in = null;
            try {
                in = new FileInputStream(confFile);
                mCurrentConfig.load(in);
            } catch (Exception ex) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }

            AbstractMap.SimpleEntry<Boolean, String> validationResult = validateSettings(
                    getConfigPropertyValue(ConfigProperty.RestAPIPort),
                    getConfigPropertyValue(ConfigProperty.StorageDir));
            if (!validationResult.getKey()) {
                // unable to validate - load defaults
                sLog.error("Unable to validate loaded config." + validationResult.getValue());
                loadDefaults();
            }

        } else {
            // create default config file
            loadDefaults();
            storeConfiguration();
        }
    }

    /**
     * Load in memory default configuration options.
     */
    private void loadDefaults() {
        mCurrentConfig.setProperty(ConfigProperty.RestAPIPort.getValue(),
                Integer.toString(DEFAULT_REST_API_LISTEN_PORT));
        mCurrentConfig.setProperty(ConfigProperty.StorageDir.getValue(), DEFAULT_STORAGE_DIR);
    }

    /**
     * Validate settings from config page.
     */
    public AbstractMap.SimpleEntry<Boolean, String> validateSettings(String restPort, String storageDir) {
        // Rest API port.
        try {
            int port = Integer.parseInt(restPort);
            if (port < 1 || port > 65535) {
                return new AbstractMap.SimpleEntry<Boolean, String>(false, "Port should be in range 1-65535");
            }
        } catch (NumberFormatException ex) {
            return new AbstractMap.SimpleEntry<Boolean, String>(false, "Unable to parse port with value: " + restPort);
        }

        // storageDir
        File dir = new File(storageDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return new AbstractMap.SimpleEntry<Boolean, String>(false,
                    "Storage path doesn't exist or is not a directory: " + storageDir);
        }

        return new AbstractMap.SimpleEntry<Boolean, String>(true, "OK");
    }
}
