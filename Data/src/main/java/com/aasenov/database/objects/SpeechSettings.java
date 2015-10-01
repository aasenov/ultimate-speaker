package com.aasenov.database.objects;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

/**
 * Settings applied for speech syntheses.
 */
@XmlRootElement(name = "SpeechSettings")
@XmlType(name = "SpeechSettings")
public class SpeechSettings implements Serializable {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_AMPLITUDE = 100;
    private static final int DEFAULT_WORD_GAP = 0;
    private static final int DEFAULT_CAPITALS = 3;
    private static final long DEFAULT_LINE_LENGTH = 0;
    private static final int DEFAULT_PITCH = 50;
    private static final int DEFAULT_SPEED = 150;
    private static final int DEFAULT_ENCODING = 1;
    private static final boolean DEFAULT_MARKUP = true;
    private static final boolean DEFAULT_NO_FINAL_PAUSE = false;
    private static final LanguageToUse DEFAULT_LANGUAGE = LanguageToUse.DETECT;

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SpeechSettings.class);

    private int mAmplitude;
    private int mWordGap;
    private int mCapitals;
    private long mLineLength;
    private int mPitch;
    private int mSpeed;
    private int mEncoding;
    private boolean mMarkup;
    private boolean mNoFinalPause;
    private LanguageToUse mLanguage;

    public SpeechSettings() {
        resetToDefaults();
    }

    /**
     * Getter for the {@link SpeechSettings#mAmplitude} field.
     *
     * @return the {@link SpeechSettings#mAmplitude} value.
     */
    @XmlElement(name = "Amplitude")
    public int getAmplitude() {
        return mAmplitude;
    }

    /**
     * Setter for the {@link SpeechSettings#mAmplitude} field.
     *
     * @param amplitude the {@link SpeechSettings#mAmplitude} to set
     */
    public void setAmplitude(int amplitude) {
        if (amplitude < 0 || amplitude > 200) {
            sLog.error(String.format("Amplitude value '%s' is not in range [0:200]. Set default value of '%s'.",
                    amplitude, DEFAULT_AMPLITUDE));
            mAmplitude = DEFAULT_AMPLITUDE;
        } else {
            mAmplitude = amplitude;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mWordGap} field.
     *
     * @return the {@link SpeechSettings#mWordGap} value.
     */
    @XmlElement(name = "WordGap")
    public int getWordGap() {
        return mWordGap;
    }

    /**
     * Setter for the {@link SpeechSettings#mWordGap} field.
     *
     * @param wordGap the {@link SpeechSettings#mWordGap} to set
     */
    public void setWordGap(int wordGap) {
        if (wordGap < 0) {
            sLog.error(String.format("Word Gap value '%s' is not greater than zero. Set default value of '%s'.",
                    wordGap, DEFAULT_WORD_GAP));
            mWordGap = DEFAULT_WORD_GAP;
        } else {
            mWordGap = wordGap;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mCapitals} field.
     *
     * @return the {@link SpeechSettings#mCapitals} value.
     */
    @XmlElement(name = "Capitals")
    public int getCapitals() {
        return mCapitals;
    }

    /**
     * Setter for the {@link SpeechSettings#mCapitals} field.
     *
     * @param capitals the {@link SpeechSettings#mCapitals} to set
     */
    public void setCapitals(int capitals) {
        if (capitals < 0) {
            sLog.error(String.format("Capitals value '%s' is not greater than zero. Set default value of '%s'.",
                    capitals, DEFAULT_CAPITALS));
            mCapitals = DEFAULT_CAPITALS;
        } else {
            mCapitals = capitals;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mLineLength} field.
     *
     * @return the {@link SpeechSettings#mLineLength} value.
     */
    @XmlElement(name = "LineLength")
    public long getLineLength() {
        return mLineLength;
    }

    /**
     * Setter for the {@link SpeechSettings#mLineLength} field.
     *
     * @param lineLength the {@link SpeechSettings#mLineLength} to set
     */
    public void setLineLength(long lineLength) {
        if (lineLength < 0) {
            sLog.error(String.format("Line length value '%s' is not greater than zero. Set default value of '%s'.",
                    lineLength, DEFAULT_LINE_LENGTH));
            mLineLength = DEFAULT_LINE_LENGTH;
        } else {
            mLineLength = lineLength;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mPitch} field.
     *
     * @return the {@link SpeechSettings#mPitch} value.
     */
    @XmlElement(name = "Pitch")
    public int getPitch() {
        return mPitch;
    }

    /**
     * Setter for the {@link SpeechSettings#mPitch} field.
     *
     * @param pitch the {@link SpeechSettings#mPitch} to set
     */
    public void setPitch(int pitch) {
        if (pitch < 0 || pitch > 99) {
            sLog.error(String.format("Pitch value '%s' is not in range [0:99]. Set default value of '%s'.", pitch,
                    DEFAULT_PITCH));
            mPitch = DEFAULT_PITCH;
        } else {
            mPitch = pitch;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mSpeed} field.
     *
     * @return the {@link SpeechSettings#mSpeed} value.
     */
    @XmlElement(name = "Speed")
    public int getSpeed() {
        return mSpeed;
    }

    /**
     * Setter for the {@link SpeechSettings#mSpeed} field.
     *
     * @param speed the {@link SpeechSettings#mSpeed} to set
     */
    public void setSpeed(int speed) {
        if (speed < 80) {
            sLog.error(String.format("Speed value '%s' is not greater than 80. Set default value of '%s'.", speed,
                    DEFAULT_SPEED));
            mSpeed = DEFAULT_SPEED;
        } else {
            mSpeed = speed;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mEncoding} field.
     *
     * @return the {@link SpeechSettings#mEncoding} value.
     */
    @XmlElement(name = "Encoding")
    public int getEncoding() {
        return mEncoding;
    }

    /**
     * Setter for the {@link SpeechSettings#mEncoding} field.
     *
     * @param encoding the {@link SpeechSettings#mEncoding} to set
     */
    public void setEncoding(int encoding) {
        if (encoding != 1 && encoding != 2 && encoding != 4) {
            sLog.error(String.format("Encoding value '%s' is not 1, 2 or 4. Set default value of '%s'.", encoding,
                    DEFAULT_ENCODING));
            mEncoding = DEFAULT_ENCODING;
        } else {
            mEncoding = encoding;
        }
    }

    /**
     * Getter for the {@link SpeechSettings#mMarkup} field.
     *
     * @return the {@link SpeechSettings#mMarkup} value.
     */
    @XmlElement(name = "Markup")
    public boolean isMarkup() {
        return mMarkup;
    }

    /**
     * Setter for the {@link SpeechSettings#mMarkup} field.
     *
     * @param markup the {@link SpeechSettings#mMarkup} to set
     */
    public void setMarkup(boolean markup) {
        mMarkup = markup;
    }

    /**
     * Getter for the {@link SpeechSettings#mNoFinalPause} field.
     *
     * @return the {@link SpeechSettings#mNoFinalPause} value.
     */
    @XmlElement(name = "NoFinalPause")
    public boolean isNoFinalPause() {
        return mNoFinalPause;
    }

    /**
     * Setter for the {@link SpeechSettings#mNoFinalPause} field.
     *
     * @param noFinalPause the {@link SpeechSettings#mNoFinalPause} to set
     */
    public void setNoFinalPause(boolean noFinalPause) {
        mNoFinalPause = noFinalPause;
    }

    /**
     * Getter for the {@link SpeechSettings#mLanguage} field.
     *
     * @return the {@link SpeechSettings#mLanguage} value.
     */
    @XmlElement(name = "Language")
    public LanguageToUse getLanguage() {
        return mLanguage;
    }

    /**
     * Setter for the {@link SpeechSettings#mLanguage} field.
     *
     * @param language the {@link SpeechSettings#mLanguage} to set
     */
    public void setLanguage(LanguageToUse language) {
        mLanguage = language;
    }

    @Override
    public String toString() {
        return String.format("UserSettings [Amplitude=%s WordGap=%s Capitals=%s LineLength=%s Pitch=%s Speed=%s "
                + "Encoding=%s Markup=%s NoFInalPause=%s Language=%s", mAmplitude, mWordGap, mCapitals, mLineLength,
                mPitch, mSpeed, mEncoding, mMarkup, mNoFinalPause, mLanguage);
    }

    /**
     * Reset all setting to default values.
     */
    public void resetToDefaults() {
        mAmplitude = DEFAULT_AMPLITUDE;
        mWordGap = DEFAULT_WORD_GAP;
        mCapitals = DEFAULT_CAPITALS;
        mLineLength = DEFAULT_LINE_LENGTH;
        mPitch = DEFAULT_PITCH;
        mSpeed = DEFAULT_SPEED;
        mEncoding = DEFAULT_ENCODING;
        mMarkup = DEFAULT_MARKUP;
        mNoFinalPause = DEFAULT_NO_FINAL_PAUSE;
        mLanguage = DEFAULT_LANGUAGE;
    }

    /**
     * Which language to use from synthesizer.
     */
    public enum LanguageToUse {
        DETECT, BULGARIAN, ENGLISH;
    }
}
