import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.aasenov.restapi.UltimateSpeakerComponent;


public class Test {

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger("com.aasenov").setLevel(Level.DEBUG);
        Logger.getLogger("org.sqlite.core").setLevel(Level.DEBUG);
        final UltimateSpeakerComponent component = new UltimateSpeakerComponent();
        component.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    System.out.println("test");
                    component.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if (true || Boolean.parseBoolean(System.getenv("RUNNING_IN_ECLIPSE")) == true) {
            System.out.println("You're using Eclipse; click in this console and     "
                    + "press ENTER to call System.exit() and run the shutdown routine.");
            try {
                System.in.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
}
