package com.aasenov.synthesis;

public interface TextSynthesizer {

    /**
     * Produce voice from given message. Voce will be produced using system devices.
     * 
     * @param message - text to synthesize.
     */
    public void synthesize(String message);

    /**
     * Produce voice from content of given file. Voce will be produced using system devices.
     * 
     * @param soureceFile - file patch containing text to synthesize.
     */
    public void synthesizeFromFile(String soureceFile);

    /**
     * Produce voice from given message and store it in file.
     * 
     * @param message - text to synthesize.
     * @param destinationFile - path to save produced voice
     */
    public void synthesizeToFile(String message, String destinationFile);

    /**
     * Produce voice from given content of given file and store it in file.
     * 
     * @param soureceFile - file patch containing text to synthesize.
     * @param destinationFile - path to save produced voice.
     */
    public void synthesizeFromFileToFile(String soureceFile, String destinationFile);

}
