import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class Test {

	public static void main(String[] args) {
		InputStream is = null;
		try {
			is = new FileInputStream("Diplomna.pdf");
			ContentHandler contenthandler = new BodyContentHandler();
			Metadata metadata = new Metadata();
			PDFParser pdfparser = new PDFParser();
			pdfparser.parse(is, contenthandler, metadata, new ParseContext());
			System.out.println(contenthandler.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
