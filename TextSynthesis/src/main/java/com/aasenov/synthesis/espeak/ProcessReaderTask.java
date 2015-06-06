package com.aasenov.synthesis.espeak;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class ProcessReaderTask extends Thread {
    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(ProcessReaderTask.class);
    /**
     * Stream to read from.
     */
    private InputStream mStreamToRead;

    /**
     * Name of process to read from.
     */
    private String mProcessName;

    /**
     * Result from stream reading in text mode
     */
    private StringBuffer mTextResult;

    /**
     * Whether input will be read as binary or not.
     */
    private boolean mReadBinary = false;

    /**
     * Location to store readed binary input.
     */
    private OutputStream mBynariResult;

    /**
     * Constructor used to read textual input.
     * 
     * @param in - Stream to read from.
     * @param processName - name of process to read input from, used to better format resulting message.
     */
    public ProcessReaderTask(InputStream in, String processName) {
        super();
        if (in == null) {
            String message = "Invalid parameters. InputStream is null.";
            sLog.error(message);
            new RuntimeException(message);
        }
        mStreamToRead = in;
        mProcessName = processName;
    }

    /**
     * Constructor used to read binary input.
     * 
     * @param in - Stream to read from.
     * @param bynariResult - place to store readed bytes.
     */
    public ProcessReaderTask(InputStream in, OutputStream bynariResult) {
        super();
        if (in == null || bynariResult == null) {
            String message = "Invalid parameters. Some stream is null.";
            sLog.error(message);
            new RuntimeException(message);
        }
        mStreamToRead = in;
        mBynariResult = bynariResult;
        mReadBinary = true;
    }

    @Override
    public void run() {
        if (mReadBinary) {
            byte[] buffer = new byte[1000];
            int bytesRead = 0;
            try {
                while ((bytesRead = mStreamToRead.read(buffer)) > 0) {
                    mBynariResult.write(buffer, 0, bytesRead);
                }
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            } finally {
                try {
                    mStreamToRead.close();
                } catch (IOException e) {
                }
                try {
                    mBynariResult.close();
                } catch (IOException e) {
                }
            }

        } else {
            mTextResult = new StringBuffer(System.lineSeparator());

            InputStreamReader reader = null;
            BufferedReader buffRead = null;
            try {
                reader = new InputStreamReader(mStreamToRead);
                buffRead = new BufferedReader(reader);
                String line;
                while ((line = buffRead.readLine()) != null) {
                    mTextResult.append(mProcessName);
                    mTextResult.append(": ");
                    mTextResult.append(line);
                    mTextResult.append(System.lineSeparator());
                }

                if (sLog.isDebugEnabled()) {
                    sLog.debug("Readed output: " + mTextResult.toString());
                }
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
                if (buffRead != null) {
                    try {
                        buffRead.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Retrieve result from reading.
     * 
     * @return Lines red.
     */
    public String getTextResult() {
        if (mTextResult != null) {
            return mTextResult.toString();
        } else {
            return null;
        }
    }
}
