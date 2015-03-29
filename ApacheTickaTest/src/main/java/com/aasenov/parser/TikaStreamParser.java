package com.aasenov.parser;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

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
    public String parse(InputStream in, ContentMetadata metadata) {
        ContentHandler contenthandler = new BodyContentHandler();
        Metadata apacheMetadata = new Metadata();
        ParseContext context = new ParseContext();
        AutoDetectParser parser = new AutoDetectParser();
        try {
            parser.parse(in, contenthandler, apacheMetadata, context);
            initMetadata(apacheMetadata, metadata);
            return contenthandler.toString();
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        }
        return null;
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
