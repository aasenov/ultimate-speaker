package com.aasenov.restapi.objects;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aasenov.database.objects.FileItem;

/**
 * Use this class for serialization, in order to send additional information about listed files.
 */
@XmlRootElement(name = "FileItemsObject")
@XmlType(name = "FileItemsObject")
public class FileItemsList {

    private long mTotalCount;
    private List<FileItem> mFiles;

    /**
     * Constructor used for serialization.
     */
    @Deprecated
    protected FileItemsList() {
    }

    /**
     * Constructor for this object.
     * 
     * @param totalCount - total count of objects in database.
     * @param files - list of files to send to client.
     */
    public FileItemsList(long totalCount, List<FileItem> files) {
        mTotalCount = totalCount;
        mFiles = files;
    }

    /**
     * Getter for the {@link FileItemsList#mTotalCount} property.
     * 
     * @return the {@link FileItemsList#mTotalCount}
     */
    @XmlElement(name = "TotalCount")
    public long getTotalCount() {
        return mTotalCount;
    }

    /**
     * Setter for the {@link FileItemsList#mTotalCount} property
     * 
     * @param mTotalCount the {@link FileItemsList#mTotalCount} to set
     */
    public void setTotalCount(long mTotalCount) {
        this.mTotalCount = mTotalCount;
    }

    /**
     * Getter for the {@link FileItemsList#mFiles} property.
     * 
     * @return the {@link FileItemsList#mFiles}
     */
    @XmlElementWrapper(name = "Files")
    @XmlElement(name = "FileItem")
    public List<FileItem> getFiles() {
        return mFiles;
    }

    /**
     * Setter for the {@link FileItemsList#mFiles} property
     * 
     * @param mFiles the {@link FileItemsList#mFiles} to set
     */
    public void setFiles(List<FileItem> mFiles) {
        this.mFiles = mFiles;
    }

}
