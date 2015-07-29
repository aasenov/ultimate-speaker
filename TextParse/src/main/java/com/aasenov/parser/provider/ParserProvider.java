package com.aasenov.parser.provider;

import com.aasenov.parser.PPTParser;
import com.aasenov.parser.StreamParser;
import com.aasenov.parser.apache.tika.TikaPPTParser;
import com.aasenov.parser.apache.tika.TikaStreamParser;

/**
 * Provider used to retrieve required {@link StreamParser} instances.
 */
public class ParserProvider {

    /**
     * Type of parser to use.
     */
    private static ParserType sParserType = ParserType.ApacheTika;

    /**
     * Getter for the {@link ParserProvider#sParserType} property.
     * 
     * @return the {@link ParserProvider#sParserType}
     */
    public static ParserType getParserType() {
        return sParserType;
    }

    /**
     * Setter for the {@link ParserProvider#sParserType} property
     * 
     * @param sParserType the {@link ParserProvider#sParserType} to set
     */
    public static void setParserType(ParserType sParserType) {
        ParserProvider.sParserType = sParserType;
    }

    /**
     * Retrieve default {@link StreamParser} instance.
     * 
     * @return Initialized parser object.
     */
    public static StreamParser getDefaultParser() {
        switch (sParserType) {
        case ApacheTika:
        default:
            return TikaStreamParser.getInstance();
        }
    }

    /**
     * Retrieve default {@link PPTParser} instance.
     * 
     * @return Initialized parser object.
     */
    public static PPTParser getDefaultPPTParser() {
        switch (sParserType) {
        case ApacheTika:
        default:
            return TikaPPTParser.getInstance();
        }
    }
}
