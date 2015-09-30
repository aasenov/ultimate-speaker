package com.aasenov.synthesis.espeak;

/**
 * {@link TextSynthesizerBase} instance for English language.
 */
public class EnglishTextSynthesizer extends TextSynthesizerBase {
    private static String[] mCommandOptions = new String[] { SyntheseSettings.Markup.getConfigOption(),
            SyntheseSettings.Speed.getConfigOption(), "70", SyntheseSettings.Pitch.getConfigOption(), "80" };

    public EnglishTextSynthesizer(String... commandOptions) {
        super(SyntheseLanguage.ENGLISH, commandOptions == null || commandOptions.length == 0 ? mCommandOptions
                : commandOptions);
    }
}
