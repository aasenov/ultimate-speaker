package com.aasenov.parser;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public interface PPTParser {
    /**
     * Content types that represent post 2003 presentation formats (pptx)
     */
    public static final List<String> sPPTXContentTypes = Arrays
            .asList("application/vnd.openxmlformats-officedocument.presentationml.presentation");

    /**
     * Content types that represent pre 2003 presentation formats (ppt)
     */
    public static final List<String> sPPTContentTypes = Arrays.asList("application/vnd.ms-powerpoint");

    /**
     * Content types that represent pre 2003 presentation formats (ppt)
     */
    public static final List<String> PRESENTATION_CONTENT_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-powerpoint");

    /**
     * Read PPT/PPTX from given file stream and process it in order to extract needed information.
     * 
     * @param in - Stream to file to parse.
     * @param contentType - type of file to parse.
     * @return {@link PPTParseResult} initialized with information from given file.
     */
    public PPTParseResult parse(InputStream in, String contentType);
}
