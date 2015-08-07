package com.aasenov.restapi.objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Use this class for serialization, in order to send slides for requested file.
 */
@XmlRootElement(name = "FileSlidesList")
@XmlType(name = "FileSlidesList")
public class FileSlidesList {

    private List<String> mImages;
    private List<String> mSpeeches;

    /**
     * Constructor used for serialization.
     */
    @Deprecated
    protected FileSlidesList() {
    }

    /**
     * Constructor for this object.
     * 
     * @param images - base64 encoded list of slides images.
     * @param speeches - base64 encoded list of slides speeches.
     */
    public FileSlidesList(List<String> images, List<String> speeches) {
        mImages = images;
        mSpeeches = speeches;
    }

    /**
     * Getter for the {@link FileSlidesList#mImages} property.
     * 
     * @return the {@link FileSlidesList#mImages}
     */
    @XmlElementWrapper(name = "Images")
    @XmlElement(name = "Image")
    public List<String> getImages() {
        return mImages;
    }

    /**
     * Setter for the {@link FileSlidesList#mImages} property
     * 
     * @param images the {@link FileSlidesList#mImages} to set
     */
    public void setImages(List<String> images) {
        mImages = images;
    }

    /**
     * Getter for the {@link FileSlidesList#mSpeeches} property.
     * 
     * @return the {@link FileSlidesList#mSpeeches}
     */
    @XmlElementWrapper(name = "Speeches")
    @XmlElement(name = "Speech")
    public List<String> getSpeeches() {
        return mSpeeches;
    }

    /**
     * Setter for the {@link FileSlidesList#mSpeeches} property
     * 
     * @param speeches the {@link FileSlidesList#mSpeeches} to set
     */
    public void setSpeeches(List<String> speeches) {
        mSpeeches = speeches;
    }
}
