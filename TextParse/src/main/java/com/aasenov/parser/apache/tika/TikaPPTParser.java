package com.aasenov.parser.apache.tika;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import com.aasenov.parser.PPTParseResult;
import com.aasenov.parser.PPTParser;
import com.aasenov.parser.apache.tika.ppt.CustomPowerPointExtractor;
import com.aasenov.parser.apache.tika.ppt.CustomXSLFPowerPointExtractor;
import com.sun.syndication.io.impl.Base64;

/**
 * {@link PPTParser} implementation using Apache Tika library.
 * 
 */
public class TikaPPTParser implements PPTParser {
    /**
     * Logger instance of this class.
     */
    private static Logger sLog = Logger.getLogger(TikaPPTParser.class);

    /**
     * Static instance of this class.
     */
    private static TikaPPTParser sInstance;

    /**
     * Private constructor to prevent instnacing.
     */
    private TikaPPTParser() {
    }

    /**
     * Retrieve static instance of this parser.
     * 
     * @return Initialized {@link TikaPPTParser} instance.
     */
    public static synchronized TikaPPTParser getInstance() {
        if (sInstance == null) {
            sInstance = new TikaPPTParser();
        }
        return sInstance;
    }

    @Override
    public PPTParseResult parse(InputStream in, String contentType) {
        PPTParseResult result = new PPTParseResult();

        if (sPPTXContentTypes.contains(contentType)) {
            XMLSlideShow ppt = null;
            try {
                ppt = new XMLSlideShow(OPCPackage.open(in));
            } catch (Exception e1) {
                sLog.error(e1.getMessage(), e1);
                return null;
            }

            // extract text
            CustomXSLFPowerPointExtractor extractor = null;
            try {
                extractor = new CustomXSLFPowerPointExtractor(ppt);
                result.setSlidesText(extractor.getText(true, true));
            } finally {
                try {
                    extractor.close();
                } catch (IOException e) {
                }
            }

            // extract slides as Base64 encoded images
            XSLFSlide[] slides = ppt.getSlides();
            List<String> slidesAsImages = new ArrayList<String>(slides.length);
            for (int i = 0; i < slides.length; i++) {
                final XSLFSlide slide = slides[i];
                slidesAsImages.add(getEncodedImage(ppt.getPageSize(), new DrawableSlide() {
                    @Override
                    public void draw(Graphics2D graphics) {
                        try {
                            slide.draw(graphics);
                        } catch (Exception ex) {
                            sLog.error(ex.getMessage(), ex);
                        }

                    }
                }));
            }
            result.setSlidesImagesBase64Encoded(slidesAsImages);
        } else if (sPPTContentTypes.contains(contentType)) {
            HSLFSlideShow hslfppt = null;
            SlideShow ppt = null;
            try {
                hslfppt = new HSLFSlideShow(in);
                ppt = new SlideShow(hslfppt);
            } catch (Exception e1) {
                sLog.error(e1.getMessage(), e1);
                return null;
            }

            // extract text
            CustomPowerPointExtractor extractor = null;
            try {
                extractor = new CustomPowerPointExtractor(hslfppt);
                result.setSlidesText(extractor.getText(true, true));
            } finally {
                try {
                    extractor.close();
                } catch (IOException e) {
                }
            }

            // extract slides as Base64 encoded images
            Slide[] slides = ppt.getSlides();
            List<String> slidesAsImages = new ArrayList<String>(slides.length);
            for (int i = 0; i < slides.length; i++) {
                final Slide slide = slides[i];
                slidesAsImages.add(getEncodedImage(ppt.getPageSize(), new DrawableSlide() {
                    @Override
                    public void draw(Graphics2D graphics) {
                        try {
                            slide.draw(graphics);
                        } catch (Exception ex) {
                            sLog.error(ex.getMessage(), ex);
                        }

                    }
                }));
            }
            result.setSlidesImagesBase64Encoded(slidesAsImages);
        } else {
            sLog.error("Unrecognized content type:" + contentType);
        }

        return result;
    }

    /**
     * Draw given slide as and image and encode its byte in Base64.
     * 
     * @param pgsize - dimensions of the slide.
     * @param slide - slede to draw.
     * @return Base64 encoded string, representing slide as image.
     */
    private String getEncodedImage(Dimension pgsize, DrawableSlide slide) {
        BufferedImage img = new BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();

        // default rendering options
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

        // draw stuff
        slide.draw(graphics);

        // save the result
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            ImageIO.write(img, "png", out);
        } catch (IOException e) {
            sLog.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }

        return new String(Base64.encode(out.toByteArray()));
    }
}
