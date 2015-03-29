package com.aasenov.espeak;

public enum SyntheseSettings {
    /**
     * Text file to speak
     */
    ReadFromFile("-f"),
    /**
     * Amplitude, 0 to 200, default is 100
     */
    Amplitude("-a"),
    /**
     * Pause between words, units of 10mS at the default speed
     */
    WordGap("-g"),
    /**
     * Indicate capital letters with: 1=sound, 2=the word "capitals", higher
     * values indicate a pitch increase (try -k20).
     */
    Capitals("-k"),
    /**
     * Line length. If not zero (which is the default), consider lines less than
     * this length as end-of-clause
     */
    LineLength("-l"),
    /**
     * Pitch adjustment, 0 to 99, default is 50
     */
    Pitch("-p"),
    /**
     * Speed in words per minute, 80 to 450, default is 175
     */
    Speed("-s"),
    /**
     * Use voice file of this name from espeak-data/voices
     */
    Voice("-v"),
    /**
     * Write speech to this WAV file, rather than speaking it directly
     */
    WAVFile("-w"),
    /**
     * Input text encoding, 1=UTF8, 2=8 bit, 4=16 bit
     */
    Encoding("-b"),
    /**
     * Interpret SSML markup, and ignore other < > tags
     */
    Markup("-m"),
    /**
     * No final sentence pause at the end of the text.
     */
    NoFinalPouse("-z"),
    /**
     * Starts a new WAV file every <minutes>. Used with -w
     */
    Split("--split");

    private String mConfigOption;

    private SyntheseSettings(String configOption) {
        mConfigOption = configOption;
    }

    public String getConfigOption() {
        return mConfigOption;
    }
}
