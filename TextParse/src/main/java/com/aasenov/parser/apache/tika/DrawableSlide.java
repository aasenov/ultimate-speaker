package com.aasenov.parser.apache.tika;

import java.awt.Graphics2D;

/**
 * This interface is common for slide, that can be drawn as images.
 */
public interface DrawableSlide {
    /**
     * Draw the underlying slide to given graphics object.
     * 
     * @param graphics - object to draw to.
     */
    public void draw(Graphics2D graphics);
}
