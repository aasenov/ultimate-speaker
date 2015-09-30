package com.aasenov.synthesis.espeak;

public enum SyntheseSettings {

    /**
     * Text file to speak
     */
    ReadFromFile("-f"),

    /**
     * Amplitude (volume) in a range of 0 to 200. The default is 100.
     */
    Amplitude("-a"),

    /**
     * Word gap. This option inserts a pause between words. The value is the length of the pause, in units of 10 mS (at
     * the default speed of 170 wpm).
     */
    WordGap("-g"),

    /**
     * Indicate words which begin with capital letters.
     * 
     * 1 eSpeak uses a click sound to indicate when a word starts with a capital letter, or double click if word is all
     * capitals.
     * 
     * 2 eSpeak speaks the word "capital" before a word which begins with a capital letter.
     * 
     * Other values: eSpeak increases the pitch for words which begin with a capital letter. The greater the value, the
     * greater the increase in pitch. Try -k20.
     */
    Capitals("-k"),

    /**
     * Line-break length, default value 0. If set, then lines which are shorter than this are treated as separate
     * clauses and spoken separately with a break between them. This can be useful for some text files, but bad for
     * others.
     */
    LineLength("-l"),

    /**
     * Pitch in a range of 0 to 99. The default is 50.
     */
    Pitch("-p"),
    /**
     * The speed in words-per-minute (approximate values for the default English voice, others may differ slightly). The
     * default value is 175. I generally use a faster speed of 260. The lower limit is 80. There is no upper limit, but
     * about 500 is probably a practical maximum.
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
     * Input text character format.
     * 
     * Value=1 UTF-8. This is the default.
     * 
     * Value=2 The 8-bit character set which corresponds to the language (eg. Latin-2 for Polish).
     * 
     * Value=4 16 bit Unicode.
     * 
     * Without this option, eSpeak assumes text is UTF-8, but will automatically switch to the 8-bit character set if it
     * finds an illegal UTF-8 sequence.
     */
    Encoding("-b"),

    /**
     * Interpret SSML markup, and ignore other < > tags
     */
    Markup("-m"),

    /**
     * No final sentence pause at the end of the text.
     */
    NoFinalPause("-z"),

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
