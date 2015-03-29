import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.aasenov.parser.ContentMetadata;
import com.aasenov.parser.TikaStreamParser;

public class Test {

    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(Test.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        InputStream is = null;
        try {
            is = new FileInputStream("Lesson_1_BA_Basics_v1.1 2012.pptx");
            ContentMetadata metadata = new ContentMetadata();
            String result = TikaStreamParser.getInstance().parse(is, metadata);
            System.out.println(metadata.toString());

            System.out.println(result);
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
