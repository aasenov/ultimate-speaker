package com.aasenov.restapi.objects;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.objects.FileItem;

/**
 * Use this class for serialization, in order to send additional information about given file.
 */
@XmlRootElement(name = "FileItemWeb")
@XmlType(name = "FileItemWeb")
public class FileItemWeb extends FileItem {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private double rating;

    /**
     * Constructor used for serialization.
     */
    @Deprecated
    protected FileItemWeb() {
        super();
    }

    /**
     * Constructor for this object.
     * 
     * @param file - original file to wrap.
     * @param rating - rating computed for this file.
     */
    public FileItemWeb(FileItem file, double rating) {
        super(file.getName(), file.getHash(), file.getLocation(), file.getSpeechLocation(), file
                .getSpeechBySlidesLocation(), file.getParsedLocation());
        this.rating = rating;
    }

    /**
     * Getter for the {@link FileItemWeb#rating} property.
     * 
     * @return the {@link FileItemWeb#rating}
     */
    @XmlElement(name = "Rating")
    public double getRating() {
        return rating;
    }

    /**
     * Setter for the {@link FileItemWeb#rating} property
     * 
     * @param rating the {@link FileItemWeb#rating} to set
     */
    public void setRating(double rating) {
        this.rating = rating;
    }
}
