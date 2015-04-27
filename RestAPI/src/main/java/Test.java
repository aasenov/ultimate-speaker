import org.apache.log4j.BasicConfigurator;

import com.aasenov.restapi.UltimateSpeakerComponent;


public class Test {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        new UltimateSpeakerComponent().start();
    }
}
