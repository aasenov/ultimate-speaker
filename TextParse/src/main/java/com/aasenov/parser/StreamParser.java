package com.aasenov.parser;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamParser {

    /**
     * Read from given file stream and extract the content. Save any useful information about parsing file to the
     * metadata object.
     * 
     * @param in - Stream to file to parse.
     * @param metadata - Metadata to be initialized during parsing.
     * @param out - Stream to write parsed information.
     */
    public void parse(InputStream in, ContentMetadata metadata, OutputStream out);
}
