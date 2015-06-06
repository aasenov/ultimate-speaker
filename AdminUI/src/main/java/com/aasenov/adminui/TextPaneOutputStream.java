package com.aasenov.adminui;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

/**
 * All writes to this stream will be displayed on local text pane object.
 */
public class TextPaneOutputStream extends OutputStream {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(AdminFrame.class);

    /**
     * {@link JTextPane} objects to append lines to.
     */
    private final JTextPane mDestination;

    /**
     * Document that is inside used text pane.
     */
    private final Document mDestinationDoc;

    /**
     * Attribute set to change color to Red.
     */
    private final SimpleAttributeSet mRedText;

    /**
     * Attribute set to change color to Black.
     */
    private final SimpleAttributeSet mBlackText;

    /**
     * Initialize the output stream to write to given {@link JTextPane} object.
     * 
     * @param destination - where to place the output.
     */
    public TextPaneOutputStream(JTextPane destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination is null");
        }

        mDestination = destination;
        mDestinationDoc = mDestination.getDocument();

        mRedText = new SimpleAttributeSet();
        StyleConstants.setForeground(mRedText, Color.RED);
        mBlackText = new SimpleAttributeSet();
        StyleConstants.setForeground(mBlackText, Color.BLACK);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // change color based on message level
                SimpleAttributeSet attr = mBlackText;
                if (text.startsWith("ERROR")) {
                    attr = mRedText;
                }
                try {
                    mDestinationDoc.insertString(mDestinationDoc.getLength(), text, attr);
                } catch (BadLocationException e) {
                    sLog.error(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }
}
