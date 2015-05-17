package com.aasenov.synthesis.espeak;

public class BulgarianTextSynthesizer extends TextSynthesizerBase {
    private static String[] mCommandOptions = new String[] { SyntheseSettings.Markup.getConfigOption(),
            SyntheseSettings.Speed.getConfigOption(), "70", SyntheseSettings.Pitch.getConfigOption(), "80" };

    public BulgarianTextSynthesizer() {
        super(SyntheseLanguage.BULGARIAN, mCommandOptions);
    }
}
