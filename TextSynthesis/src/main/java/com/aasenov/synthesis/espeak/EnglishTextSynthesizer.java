package com.aasenov.synthesis.espeak;

public class EnglishTextSynthesizer extends TextSynthesizerBase {
    private static String[] mCommandOptions = new String[] { SyntheseSettings.Markup.getConfigOption(),
            SyntheseSettings.Speed.getConfigOption(), "70", SyntheseSettings.Pitch.getConfigOption(), "80" };

    public EnglishTextSynthesizer() {
        super(SyntheseLanguage.ENGLISH, mCommandOptions);
    }
}
