package com.aasenov.parser.apache.tika.ppt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.DrawingParagraph;
import org.apache.poi.xslf.usermodel.DrawingTextBody;
import org.apache.poi.xslf.usermodel.DrawingTextPlaceholder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFCommentAuthors;
import org.apache.poi.xslf.usermodel.XSLFComments;
import org.apache.poi.xslf.usermodel.XSLFCommonSlideData;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTComment;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommentAuthor;

public class CustomXSLFPowerPointExtractor extends POIXMLTextExtractor {
    public static final XSLFRelation[] SUPPORTED_TYPES = new XSLFRelation[] { XSLFRelation.MAIN, XSLFRelation.MACRO,
            XSLFRelation.MACRO_TEMPLATE, XSLFRelation.PRESENTATIONML, XSLFRelation.PRESENTATIONML_TEMPLATE,
            XSLFRelation.PRESENTATION_MACRO };

    private XMLSlideShow slideshow;
    private boolean slidesByDefault = true;
    private boolean notesByDefault = false;
    private boolean masterByDefault = false;

    public CustomXSLFPowerPointExtractor(XMLSlideShow slideshow) {
        super(slideshow);
        this.slideshow = slideshow;
    }

    public CustomXSLFPowerPointExtractor(XSLFSlideShow slideshow) throws XmlException, IOException {
        this(new XMLSlideShow(slideshow.getPackage()));
    }

    public CustomXSLFPowerPointExtractor(OPCPackage container) throws XmlException, OpenXML4JException, IOException {
        this(new XSLFSlideShow(container));
    }

    /**
     * Should a call to getText() return slide text? Default is yes
     */
    public void setSlidesByDefault(boolean slidesByDefault) {
        this.slidesByDefault = slidesByDefault;
    }

    /**
     * Should a call to getText() return notes text? Default is no
     */
    public void setNotesByDefault(boolean notesByDefault) {
        this.notesByDefault = notesByDefault;
    }

    /**
     * Should a call to getText() return text from master? Default is no
     */
    public void setMasterByDefault(boolean masterByDefault) {
        this.masterByDefault = masterByDefault;
    }

    /**
     * Gets the slide text, but not the notes text
     */
    public String getText() {
        return Arrays.toString(getText(slidesByDefault, notesByDefault).toArray());
    }

    /**
     * Gets the requested text from the file
     * 
     * @param slideText Should we retrieve text from slides?
     * @param notesText Should we retrieve text from notes?
     */
    public List<String> getText(boolean slideText, boolean notesText) {
        return getText(slideText, notesText, masterByDefault);
    }

    /**
     * Gets the requested text from the file
     * 
     * @param slideText Should we retrieve text from slides?
     * @param notesText Should we retrieve text from notes?
     * @param masterText Should we retrieve text from master slides?
     */
    @SuppressWarnings("deprecation")
    public List<String> getText(boolean slideText, boolean notesText, boolean masterText) {
        XSLFSlide[] slides = slideshow.getSlides();
        List<String> result = new ArrayList<String>(slides.length);

        XSLFCommentAuthors commentAuthors = slideshow.getCommentAuthors();

        for (XSLFSlide slide : slides) {
            StringBuffer text = new StringBuffer();
            try {
                XSLFNotes notes = slide.getNotes();
                XSLFComments comments = slide.getComments();
                XSLFSlideLayout layout = slide.getSlideLayout();
                XSLFSlideMaster master = layout.getSlideMaster();

                // Do the slide's text if requested
                if (slideText) {
                    extractText(slide.getCommonSlideData(), false, text);

                    // If requested, get text from the master and it's layout
                    if (masterText) {
                        if (layout != null) {
                            extractText(layout.getCommonSlideData(), true, text);
                        }
                        if (master != null) {
                            extractText(master.getCommonSlideData(), true, text);
                        }
                    }

                    // If the slide has comments, do those too
                    if (comments != null) {
                        for (CTComment comment : comments.getCTCommentsList().getCmArray()) {
                            // Do the author if we can
                            if (commentAuthors != null) {
                                CTCommentAuthor author = commentAuthors.getAuthorById(comment.getAuthorId());
                                if (author != null) {
                                    text.append(author.getName() + ": ");
                                }
                            }

                            // Then the comment text, with a new line afterwards
                            text.append(comment.getText());
                            text.append("\n");
                        }
                    }
                }

                // Do the notes if requested
                if (notesText && notes != null) {
                    extractText(notes.getCommonSlideData(), false, text);
                }

                result.add(text.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    private void extractText(XSLFCommonSlideData data, boolean skipPlaceholders, StringBuffer text) {
        for (DrawingTextBody textBody : data.getDrawingText()) {
            if (skipPlaceholders && textBody instanceof DrawingTextPlaceholder) {
                DrawingTextPlaceholder ph = (DrawingTextPlaceholder) textBody;
                if (!ph.isPlaceholderCustom()) {
                    // Skip non-customised placeholder text
                    continue;
                }
            }

            for (DrawingParagraph p : textBody.getParagraphs()) {
                text.append(p.getText());
                text.append("\n");
            }
        }
    }
}
