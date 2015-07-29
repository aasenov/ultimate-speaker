package com.aasenov.parser.apache.tika.ppt;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Comment;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.Table;
import org.apache.poi.hslf.model.TableCell;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public final class CustomPowerPointExtractor extends POIOLE2TextExtractor {
    private HSLFSlideShow _hslfshow;
    private SlideShow _show;
    private Slide[] _slides;

    private boolean _slidesByDefault = true;
    private boolean _notesByDefault = false;
    private boolean _commentsByDefault = false;
    private boolean _masterByDefault = false;

    /**
     * Creates a CustomPowerPointExtractor, from a file
     * 
     * @param fileName The name of the file to extract from
     */
    public CustomPowerPointExtractor(String fileName) throws IOException {
        this(new FileInputStream(fileName));
    }

    /**
     * Creates a CustomPowerPointExtractor, from an Input Stream
     * 
     * @param iStream The input stream containing the PowerPoint document
     */
    public CustomPowerPointExtractor(InputStream iStream) throws IOException {
        this(new POIFSFileSystem(iStream));
    }

    /**
     * Creates a CustomPowerPointExtractor, from an open POIFSFileSystem
     * 
     * @param fs the POIFSFileSystem containing the PowerPoint document
     */
    public CustomPowerPointExtractor(POIFSFileSystem fs) throws IOException {
        this(fs.getRoot());
    }

    /**
     * Creates a CustomPowerPointExtractor, from an open NPOIFSFileSystem
     * 
     * @param fs the NPOIFSFileSystem containing the PowerPoint document
     */
    public CustomPowerPointExtractor(NPOIFSFileSystem fs) throws IOException {
        this(fs.getRoot());
    }

    /**
     * Creates a CustomPowerPointExtractor, from a specific place inside an open NPOIFSFileSystem
     * 
     * @param dir the POIFS Directory containing the PowerPoint document
     */
    public CustomPowerPointExtractor(DirectoryNode dir) throws IOException {
        this(new HSLFSlideShow(dir));
    }

    /**
     * @deprecated Use {@link #CustomPowerPointExtractor(DirectoryNode)} instead
     */
    @Deprecated
    public CustomPowerPointExtractor(DirectoryNode dir, POIFSFileSystem fs) throws IOException {
        this(new HSLFSlideShow(dir, fs));
    }

    /**
     * Creates a CustomPowerPointExtractor, from a HSLFSlideShow
     * 
     * @param ss the HSLFSlideShow to extract text from
     */
    public CustomPowerPointExtractor(HSLFSlideShow ss) {
        super(ss);
        _hslfshow = ss;
        _show = new SlideShow(_hslfshow);
        _slides = _show.getSlides();
    }

    /**
     * Should a call to getText() return slide text? Default is yes
     */
    public void setSlidesByDefault(boolean slidesByDefault) {
        this._slidesByDefault = slidesByDefault;
    }

    /**
     * Should a call to getText() return notes text? Default is no
     */
    public void setNotesByDefault(boolean notesByDefault) {
        this._notesByDefault = notesByDefault;
    }

    /**
     * Should a call to getText() return comments text? Default is no
     */
    public void setCommentsByDefault(boolean commentsByDefault) {
        this._commentsByDefault = commentsByDefault;
    }

    /**
     * Should a call to getText() return text from master? Default is no
     */
    public void setMasterByDefault(boolean masterByDefault) {
        this._masterByDefault = masterByDefault;
    }

    /**
     * Fetches all the slide text from the slideshow, but not the notes, unless you've called setSlidesByDefault() and
     * setNotesByDefault() to change this
     */
    public String getText() {
        return Arrays.toString(getText(_slidesByDefault, _notesByDefault, _commentsByDefault, _masterByDefault)
                .toArray());
    }

    /**
     * Fetches all the notes text from the slideshow, but not the slide text
     */
    public String getNotes() {
        return Arrays.toString(getText(false, true).toArray());
    }

    public List<OLEShape> getOLEShapes() {
        List<OLEShape> list = new ArrayList<OLEShape>();

        for (int i = 0; i < _slides.length; i++) {
            Slide slide = _slides[i];

            Shape[] shapes = slide.getShapes();
            for (int j = 0; j < shapes.length; j++) {
                if (shapes[j] instanceof OLEShape) {
                    list.add((OLEShape) shapes[j]);
                }
            }
        }

        return list;
    }

    /**
     * Fetches text from the slideshow, be it slide text or note text. Because the final block of text in a TextRun
     * normally have their last \n stripped, we add it back
     * 
     * @param getSlideText fetch slide text
     * @param getNoteText fetch note text
     */
    public List<String> getText(boolean getSlideText, boolean getNoteText) {
        return getText(getSlideText, getNoteText, _commentsByDefault, _masterByDefault);
    }

    public List<String> getText(boolean getSlideText, boolean getNoteText, boolean getCommentText, boolean getMasterText) {
        List<String> result = new ArrayList<String>(_slides.length);
        if (getSlideText) {
            for (int i = 0; i < _slides.length; i++) {
                StringBuffer ret = new StringBuffer();
                Slide slide = _slides[i];

                // Slide header, if set
                HeadersFooters hf = slide.getHeadersFooters();
                if (hf != null && hf.isHeaderVisible() && hf.getHeaderText() != null) {
                    ret.append(hf.getHeaderText() + "\n");
                }

                // Slide text
                textRunsToText(ret, slide.getTextRuns());

                // Table text
                for (Shape shape : slide.getShapes()) {
                    if (shape instanceof Table) {
                        extractTableText(ret, (Table) shape);
                    }
                }
                // Slide footer, if set
                if (hf != null && hf.isFooterVisible() && hf.getFooterText() != null) {
                    ret.append(hf.getFooterText() + "\n");
                }

                // Comments, if requested and present
                if (getCommentText) {
                    Comment[] comments = slide.getComments();
                    for (int j = 0; j < comments.length; j++) {
                        ret.append(comments[j].getAuthor() + " - " + comments[j].getText() + "\n");
                    }
                }
                result.add(ret.toString());
            }
        }
        return result;
    }

    private void extractTableText(StringBuffer ret, Table table) {
        for (int row = 0; row < table.getNumberOfRows(); row++) {
            for (int col = 0; col < table.getNumberOfColumns(); col++) {
                TableCell cell = table.getCell(row, col);
                // defensive null checks; don't know if they're necessary
                if (cell != null) {
                    String txt = cell.getText();
                    txt = (txt == null) ? "" : txt;
                    ret.append(txt);
                    if (col < table.getNumberOfColumns() - 1) {
                        ret.append("\t");
                    }
                }
            }
            ret.append('\n');
        }
    }

    private void textRunsToText(StringBuffer ret, TextRun[] runs) {
        if (runs == null) {
            return;
        }

        for (int j = 0; j < runs.length; j++) {
            TextRun run = runs[j];
            if (run != null) {
                String text = run.getText();
                ret.append(text);
                if (!text.endsWith("\n")) {
                    ret.append("\n");
                }
            }
        }
    }
}
