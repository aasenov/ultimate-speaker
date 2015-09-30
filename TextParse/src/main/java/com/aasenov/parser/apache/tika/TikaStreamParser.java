package com.aasenov.parser.apache.tika;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import com.aasenov.parser.ContentMetadata;
import com.aasenov.parser.LanguageDetected;
import com.aasenov.parser.StreamParser;

/**
 * {@link StreamParser} implementation using Apache Tika library.
 * 
 */
public class TikaStreamParser implements StreamParser {
    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(TikaStreamParser.class);

    /**
     * Property under which is saved number of pages.
     */
    private static String NUM_PAGES_PROPERTY = "xmpTPg:NPages";

    /**
     * Size of buffer used for reading.
     */
    private static int READ_BUFF_SIZE = 65536; // 64KB buffer

    /**
     * Number of bytes to detect language from.
     */
    private static int LANGUAGE_DETECT_SIMPLE_SIZE = 1048576; // 1MB simple

    /**
     * ISO 639 language identifiers which will be detected by tika for BULGARIAN.
     */
    private static List<String> LANGUAGE_DETECT_BULGARIAN = Arrays.asList("bg", "bul", "ru", "rus");

    /**
     * ISO 639 language identifiers which will be detected by tika for ENGLISH.
     */
    private static List<String> LANGUAGE_DETECT_ENGLISH = Arrays.asList("en", "eng", "it", "ita");

    /**
     * Default language to use, when it's unknown.
     */
    private static LanguageDetected DEFAULT_LANGUAGE_DETECTED = LanguageDetected.BULGARIAN;

    /**
     * Static instance of this class.
     */
    private static TikaStreamParser sInstance;

    /**
     * Private constructor to prevent instnacing.
     */
    private TikaStreamParser() {
    }

    /**
     * Retrieve static instance of this parser.
     * 
     * @return Initialized {@link TikaStreamParser} instance.
     */
    public static synchronized TikaStreamParser getInstance() {
        if (sInstance == null) {
            sInstance = new TikaStreamParser();
        }
        return sInstance;
    }

    @Override
    public void parse(InputStream in, ContentMetadata metadata, OutputStream out) {
        ContentHandler contenthandler;
        Metadata apacheMetadata = new Metadata();
        ParseContext context = new ParseContext();
        AutoDetectParser parser = new AutoDetectParser();
        // write output to temporary file, to be able to detect language afterwards.
        Writer tmpOutWriter = null;
        File tmpFile = null;
        boolean parseSuccessful = false;
        try {
            tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            // convert bytes red in UTF8
            tmpOutWriter = Files.newBufferedWriter(Paths.get(tmpFile.getCanonicalPath()), StandardCharsets.UTF_8);
            contenthandler = new BodyContentHandler(tmpOutWriter);
            parser.parse(in, contenthandler, apacheMetadata, context);
            initMetadata(apacheMetadata, metadata);
            parseSuccessful = true;
            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Successfully parse file and store it in %s.", tmpFile.getAbsoluteFile()));
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (tmpOutWriter != null) {
                try {
                    tmpOutWriter.close();
                } catch (IOException e) {
                }
            }
        }

        FileInputStream tmpInStream = null;
        byte[] buff = new byte[READ_BUFF_SIZE];
        ByteArrayOutputStream detectStream = null;
        if (parseSuccessful) {
            try {
                // detect language as it's not extracted dynamically from the input file and move to original stream
                tmpInStream = new FileInputStream(tmpFile);
                detectStream = new ByteArrayOutputStream(LANGUAGE_DETECT_SIMPLE_SIZE);
                int len = tmpInStream.read(buff);
                if (len > 0) {
                    out.write(buff, 0, len);
                    if (detectStream.size() < LANGUAGE_DETECT_SIMPLE_SIZE) {
                        detectStream.write(buff, 0, len);
                    }
                }

                LanguageIdentifier identifier = new LanguageIdentifier(new String(detectStream.toByteArray(),
                        StandardCharsets.UTF_8));
                if (LANGUAGE_DETECT_BULGARIAN.contains(identifier.getLanguage())) {
                    metadata.setLanguage(LanguageDetected.BULGARIAN);
                } else if (LANGUAGE_DETECT_ENGLISH.contains(identifier.getLanguage())) {
                    metadata.setLanguage(LanguageDetected.ENGLISH);
                } else {
                    sLog.error(String.format("Unable to detect language for '%s'. Dafaults to: %s",
                            identifier.getLanguage(), DEFAULT_LANGUAGE_DETECTED));
                    metadata.setLanguage(DEFAULT_LANGUAGE_DETECTED);
                }

                if (sLog.isDebugEnabled()) {
                    sLog.debug(String.format("Successfully copy tmp file and detect language as %s.",
                            metadata.getLanguage()));
                }
            } catch (Exception e) {
                sLog.error(e.getMessage(), e);
            } finally {
                if (tmpInStream != null) {
                    try {
                        tmpInStream.close();
                    } catch (IOException e) {
                    }
                }
                if (detectStream != null) {
                    try {
                        detectStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        // clean tmp file
        if (tmpFile.exists() && !tmpFile.delete()) {
            sLog.error("Unable to delete temporary file: " + tmpFile.getAbsolutePath());
        }
    }

    /**
     * Transform extracted fileMetadata to library independednt {@link ContentMetadata} object.
     * 
     * @param fileMetadata - metadata extracted during parsing.
     * @param metadataToInit - library independent metadata to initialize.
     */
    private static void initMetadata(Metadata fileMetadata, ContentMetadata metadataToInit) {
        metadataToInit.setContentType(fileMetadata.get(Metadata.CONTENT_TYPE));
        metadataToInit.setTitle(fileMetadata.get(TikaCoreProperties.TITLE));
        metadataToInit.setNumPages(parseIntProperty(fileMetadata.get(NUM_PAGES_PROPERTY), 0));
        metadataToInit.setAuthor(fileMetadata.get(TikaCoreProperties.CREATOR));
        metadataToInit.setWordCount(parseIntProperty(fileMetadata.get(Office.WORD_COUNT), 0));
    }

    /**
     * Try to convert given property value to integer.
     * 
     * @param originalProperty - value to convert
     * @param defaultValue - value to set in case of problem during convertion.
     * @return Parsed value, or defaultValue in case of error.
     */
    private static int parseIntProperty(String originalProperty, int defaultValue) {
        try {
            return Integer.parseInt(originalProperty);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

}
