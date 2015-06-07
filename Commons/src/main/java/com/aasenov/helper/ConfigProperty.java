package com.aasenov.helper;

public enum ConfigProperty {

    RestAPIPort("restAPIPort"), StorageDir("storageDir");

    private String mValue;

    private ConfigProperty(String value) {
        mValue = value;
    }

    /**
     * Retrieve value of enum constant. Used for key when storing in configuration file.
     * 
     * @return Value of enum constant.
     */
    public String getValue() {
        return mValue;
    }
}
