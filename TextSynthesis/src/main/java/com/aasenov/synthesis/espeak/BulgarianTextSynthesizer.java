package com.aasenov.synthesis.espeak;

/**
 * {@link TextSynthesizerBase} instance for Bulgarian language.
 */
public class BulgarianTextSynthesizer extends TextSynthesizerBase {
    private static String[] mCommandOptions = new String[] { SyntheseSettings.Markup.getConfigOption(),
            SyntheseSettings.Speed.getConfigOption(), "70", SyntheseSettings.Pitch.getConfigOption(), "80" };

    public BulgarianTextSynthesizer(String... commandOptions) {
        super(SyntheseLanguage.BULGARIAN, commandOptions == null || commandOptions.length == 0 ? mCommandOptions
                : commandOptions);
    }
}
