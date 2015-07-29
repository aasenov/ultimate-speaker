package com.aasenov.parser;

import java.util.List;

/**
 * This object contain information from parsed PPT/PPTX files.
 */
public class PPTParseResult {

    /**
     * List of String, representing content of each slide.
     */
    private List<String> slidesText;

    /**
     * List of strings, representing base64 encoded image representation of the slide.
     */
    private List<String> slidesImagesBase64Encoded;

    /**
     * Getter for the {@link PPTParseResult#slidesText} property.
     * 
     * @return the {@link PPTParseResult#slidesText}
     */
    public List<String> getSlidesText() {
        return slidesText;
    }

    /**
     * Setter for the {@link PPTParseResult#slidesText} property
     * 
     * @param slidesText the {@link PPTParseResult#slidesText} to set
     */
    public void setSlidesText(List<String> slidesText) {
        this.slidesText = slidesText;
    }

    /**
     * Getter for the {@link PPTParseResult#slidesImagesBase64Encoded} property.
     * 
     * @return the {@link PPTParseResult#slidesImagesBase64Encoded}
     */
    public List<String> getSlidesImagesBase64Encoded() {
        return slidesImagesBase64Encoded;
    }

    /**
     * Setter for the {@link PPTParseResult#slidesImagesBase64Encoded} property
     * 
     * @param slidesImagesBase64Encoded the {@link PPTParseResult#slidesImagesBase64Encoded} to set
     */
    public void setSlidesImagesBase64Encoded(List<String> slidesImagesBase64Encoded) {
        this.slidesImagesBase64Encoded = slidesImagesBase64Encoded;
    }

}
