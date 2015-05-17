import org.apache.log4j.BasicConfigurator;

import com.aasenov.synthesis.provider.SynthesizerLanguage;
import com.aasenov.synthesis.provider.TextSynthesizerProvider;


public class Test {
    public static void main(String[] args) throws Exception {
    	BasicConfigurator.configure();
        // Synthesizer syn = new Synthesizer();
        // StringBuilder command = new StringBuilder("espeak ");
        // command.append("-m ");// Interpret SSML markup, and ignore other < >
        // // tags
        // command.append("-s 70 ");// Speed in words per minute, default is 160
        // command.append("-p 90 ");// Pitch adjustment, 0 to 99, default is 50
        // command.append("-v ");// Use voice file of this name from
        // // espeak-data/voices
        // syn.cmd = command.toString();
        // syn.speak("bg", "Много си красива");

        // syn.synthesize("test");
        TextSynthesizerProvider.getDefaultSynthesizer(SynthesizerLanguage.BULGARIAN).synthesizeFromFile("speech.txt");
        // syn.synthesizeToFile("Много си красива", "asen.wav");
    }
}
