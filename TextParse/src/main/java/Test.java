import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.aasenov.parser.ContentMetadata;
import com.aasenov.parser.provider.ParserProvider;

public class Test {

    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(Test.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        InputStream is = null;
        OutputStream out = null;
        try {
            is = new FileInputStream("Diplomna.pdf");
            out = new FileOutputStream("Diplomna.txt");
            ContentMetadata metadata = new ContentMetadata();
            ParserProvider.getDefaultParser().parse(is, metadata, out);
            System.out.println(metadata.toString());
        } catch (Exception e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
