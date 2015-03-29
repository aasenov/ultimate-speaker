package com.aasenov.parser;

import java.io.InputStream;

public interface StreamParser {

    /**
     * Read from given file stream and extract the content. Save any useful information about parsing file to the
     * metadata object.
     * 
     * @param in - Stream to file to parse.
     * @param metadata - Metadata to be initialized during parsing.
     * @return Content of given file, <b>Null</b> in case of error.
     */
    public String parse(InputStream in, ContentMetadata metadata);
}
