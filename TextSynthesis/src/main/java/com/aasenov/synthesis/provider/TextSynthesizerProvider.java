package com.aasenov.synthesis.provider;

import com.aasenov.synthesis.TextSynthesizer;
import com.aasenov.synthesis.espeak.BulgarianTextSynthesizer;
import com.aasenov.synthesis.espeak.EnglishTextSynthesizer;

/**
 * Provider used to retrieve required {@link TextSynthesizer} instances.
 */
public class TextSynthesizerProvider {

    /**
     * Type of synthesizer to use.
     */
    private static SynthesizerType sSynthesizerType = SynthesizerType.ESpeak;

    /**
     * Getter for the {@link TextSynthesizerProvider#sSynthesizerType} field.
     * 
     * @return the {@link TextSynthesizerProvider#sSynthesizerType} value.
     */
    public static SynthesizerType getSynthesizerType() {
        return sSynthesizerType;
    }

    /**
     * Setter for the {@link TextSynthesizerProvider#sSynthesizerType} field.
     * 
     * @param sSynthesizerType the {@link TextSynthesizerProvider#sSynthesizerType} to set
     */
    public static void setSynthesizerType(SynthesizerType sSynthesizerType) {
        TextSynthesizerProvider.sSynthesizerType = sSynthesizerType;
    }

    /**
     * Retrieve default {@link TextSynthesizer} instance.
     * 
     * @param language - language of the synthesizer.
     * @return Initialized synthesizer object.
     */
    public static TextSynthesizer getDefaultSynthesizer(SynthesizerLanguage language) {
        switch (sSynthesizerType) {
        case ESpeak:
        default: {
            switch (language) {
            default:
            case BULGARIAN:
                return new BulgarianTextSynthesizer();
            case ENGLISH:
                return new EnglishTextSynthesizer();
            }
        }
        }
    }

    /**
     * Retrieve default {@link TextSynthesizer} instance.
     * 
     * @param language - language of the synthesizer.
     * @param options - options for the synthesizer.
     * 
     * @return Initialized synthesizer object.
     */
    public static TextSynthesizer getDefaultSynthesizer(SynthesizerLanguage language, String[] options) {
        switch (sSynthesizerType) {
        case ESpeak:
        default: {
            switch (language) {
            default:
            case BULGARIAN:
                return new BulgarianTextSynthesizer(options);
            case ENGLISH:
                return new EnglishTextSynthesizer(options);
            }
        }
        }
    }

}
