package com.aasenov.espeak;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class TextSynthesizerBase implements TextSynthesizer {
    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(TextSynthesizerBase.class);

    /**
     * Language in which the to produce voice.
     */
    private SyntheseLanguage mLanguage;

    /**
     * Base command to execute to call synthesis tool.
     */
    private List<String> mBaseCommand;

    public TextSynthesizerBase(SyntheseLanguage language, String[] options) {
        mLanguage = language;
        mBaseCommand = new ArrayList<String>();
        mBaseCommand.add("espeak");
        for (String option : options) {
            mBaseCommand.add(option);
        }

        // set language
        mBaseCommand.add(SyntheseSettings.Voice.getConfigOption());
        mBaseCommand.add(mLanguage.getValue());
    }

    @Override
    public void synthesize(String message) {
        // replace invalid characters
        String textToSynthesize = replaceInvalidCharacters(message);

        List<String> commandToExecute = new ArrayList<String>(mBaseCommand);
        commandToExecute.add(textToSynthesize);
        executeCommand(commandToExecute);
    }

    @Override
    public void synthesizeFromFile(String soureceFile) {
        List<String> commandToExecute = new ArrayList<String>(mBaseCommand);
        commandToExecute.add(SyntheseSettings.ReadFromFile.getConfigOption());
        commandToExecute.add(soureceFile);
        executeCommand(commandToExecute);
    }

    @Override
    public void synthesizeToFile(String message, String destinationFile) {
        // replace invalid characters
        String textToSynthesize = replaceInvalidCharacters(message);

        List<String> commandToExecute = new ArrayList<String>(mBaseCommand);
        commandToExecute.add(SyntheseSettings.WAVFile.getConfigOption());// write voice to wav file.
        commandToExecute.add(destinationFile);
        commandToExecute.add(textToSynthesize);

        executeCommand(commandToExecute);
    }

    @Override
    public void synthesizeFromFileToFile(String soureceFile, String destinationFile) {
        List<String> commandToExecute = new ArrayList<String>(mBaseCommand);
        commandToExecute.add(SyntheseSettings.ReadFromFile.getConfigOption());
        commandToExecute.add(soureceFile);
        commandToExecute.add(SyntheseSettings.WAVFile.getConfigOption());// write voice to wav file.
        commandToExecute.add(destinationFile);

        executeCommand(commandToExecute);
    }

    /**
     * Execute given command.
     * 
     * @param commandToExecute - Command to be executed
     */
    private void executeCommand(List<String> commandToExecute) {
        try {
            if (sLog.isDebugEnabled()) {
                sLog.debug("Executing command: " + commandToExecute);
            }

            ProcessBuilder builder = new ProcessBuilder(commandToExecute);
            builder.redirectErrorStream(true); // redirect error stream to read
            // from all streams at once
            Process process = builder.start();

            // read input
            ProcessReaderTask readTask = new ProcessReaderTask(process.getInputStream(), "espeak");
            readTask.start();

            // wait finishing
            int resultCode = process.waitFor();
            waitReadingTask(readTask, 10);

            if (sLog.isDebugEnabled()) {
                sLog.debug("Process exit with code: " + resultCode);
                sLog.debug("Process error output: " + readTask.getTextResult());
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        }
    }

    /**
     * Wait given reading task to finish , after process has exited.
     * 
     * @param readTask - task to wait.
     * @param secondsToWait - how many seconds to wait if task is still alive.
     */
    private void waitReadingTask(ProcessReaderTask readTask, int secondsToWait) {
        if (readTask.isAlive()) {
            // wait task to finish
            sLog.info("Process exited but reading task is still alive. Waiting " + secondsToWait
                    + " seconds to finish.");
            try {
                readTask.join(1000 * secondsToWait);
                if (readTask.isAlive()) {
                    sLog.info("Reading task is still alive. Interrupting it!");
                    readTask.interrupt();
                }
            } catch (Exception ex) {
                sLog.error("Error during waiting for readerTask to finish", ex);
            }
        }
    }

    /**
     * Replace all invalid characters from the given message.
     * 
     * @param message - message to check for invalid chars.
     * @return Transformed valid message.
     */
    private String replaceInvalidCharacters(String message) {
        String result = message;
        if (message.indexOf(" ") != -1) {
            result = message.replaceAll(" ", "&nbsp;");
        }
        if (!result.startsWith("\"")) {
            result = "\"" + result + "\"";
        }
        return result;
    }
}
