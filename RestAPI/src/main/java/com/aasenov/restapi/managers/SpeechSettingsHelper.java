package com.aasenov.restapi.managers;

import java.util.ArrayList;
import java.util.List;

import com.aasenov.database.objects.SpeechSettings;
import com.aasenov.synthesis.espeak.SyntheseSettings;
import com.aasenov.synthesis.provider.SynthesizerType;

/**
 * Helper class used for speech settings conversion.
 */
public class SpeechSettingsHelper {

    /**
     * Retrieve synthesizer settings based on its type and given speech settings.
     * 
     * @param synthesizerType - type of synthesizer to retrieve settings for.
     * @param settings - settings to convert.
     * @return Constructed synthesizer settings.
     */
    public static String[] getSynthesizerSettings(SynthesizerType synthesizerType, SpeechSettings settings) {
        List<String> result = new ArrayList<String>();
        switch (synthesizerType) {
        case ESpeak:
            result.add(SyntheseSettings.Amplitude.getConfigOption());
            result.add(Integer.toString(settings.getAmplitude()));
            result.add(SyntheseSettings.Capitals.getConfigOption());
            result.add(Integer.toString(settings.getCapitals()));
            result.add(SyntheseSettings.Encoding.getConfigOption());
            result.add(Integer.toString(settings.getEncoding()));
            result.add(SyntheseSettings.LineLength.getConfigOption());
            result.add(Long.toString(settings.getLineLength()));
            result.add(SyntheseSettings.Pitch.getConfigOption());
            result.add(Integer.toString(settings.getPitch()));
            result.add(SyntheseSettings.Speed.getConfigOption());
            result.add(Integer.toString(settings.getSpeed()));
            result.add(SyntheseSettings.WordGap.getConfigOption());
            result.add(Integer.toString(settings.getWordGap()));
            if (settings.isMarkup()) {
                result.add(SyntheseSettings.Markup.getConfigOption());
            }
            if (settings.isNoFinalPause()) {
                result.add(SyntheseSettings.NoFinalPause.getConfigOption());
            }
            break;
        }
        return result.toArray(new String[result.size()]);
    }
}
