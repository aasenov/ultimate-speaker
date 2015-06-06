package com.aasenov.parser;

/**
 * Class that contain metadata for file that is parsed.
 */
public class ContentMetadata {

    /**
     * Keep parsed content type.
     */
    private String mContentType;

    /**
     * Keep parsed document title.
     */
    private String mTitle;

    /**
     * Keep parsed number of pages in document.
     */
    private int mNumPages;

    /**
     * Keep parsed document author.
     */
    private String mAuthor;

    /**
     * Keep parsed document author.
     */
    private int mWordCount;

    /**
     * Keep parsed document language.
     */
    private LanguageDetected mLanguage;

    /**
     * Getter for the {@link ContentMetadata#mContentType} field.
     * 
     * @return the {@link ContentMetadata#mContentType} value.
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Setter for the {@link ContentMetadata#mContentType} field.
     * 
     * @param value the {@link ContentMetadata#mContentType} to set
     */
    public void setContentType(String value) {
        mContentType = value;
    }

    /**
     * Getter for the {@link ContentMetadata#mTitle} field.
     * 
     * @return the {@link ContentMetadata#mTitle} value.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Setter for the {@link ContentMetadata#mTitle} field.
     * 
     * @param mTitle the {@link ContentMetadata#mTitle} to set
     */
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    /**
     * Getter for the {@link ContentMetadata#mNumPages} field.
     * 
     * @return the {@link ContentMetadata#mNumPages} value.
     */
    public int getNumPages() {
        return mNumPages;
    }

    /**
     * Setter for the {@link ContentMetadata#mNumPages} field.
     * 
     * @param mNumPages the {@link ContentMetadata#mNumPages} to set
     */
    public void setNumPages(int mNumPages) {
        this.mNumPages = mNumPages;
    }

    /**
     * Getter for the {@link ContentMetadata#mAuthor} field.
     * 
     * @return the {@link ContentMetadata#mAuthor} value.
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * Setter for the {@link ContentMetadata#mAuthor} field.
     * 
     * @param mAuthor the {@link ContentMetadata#mAuthor} to set
     */
    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    /**
     * Getter for the {@link ContentMetadata#mWordCount} field.
     * 
     * @return the {@link ContentMetadata#mWordCount} value.
     */
    public int getWordCount() {
        return mWordCount;
    }

    /**
     * Setter for the {@link ContentMetadata#mWordCount} field.
     * 
     * @param mWordCount the {@link ContentMetadata#mWordCount} to set
     */
    public void setWordCount(int mWordCount) {
        this.mWordCount = mWordCount;
    }

    /**
     * Getter for the {@link ContentMetadata#mLanguage} field.
     * 
     * @return the {@link ContentMetadata#mLanguage} value.
     */
    public LanguageDetected getLanguage() {
        return mLanguage;
    }

    /**
     * Setter for the {@link ContentMetadata#mLanguage} field.
     * 
     * @param mLanguage the {@link ContentMetadata#mLanguage} to set
     */
    public void setLanguage(LanguageDetected mLanguage) {
        this.mLanguage = mLanguage;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("ContentMetadata[");
        result.append("ContentType=");
        result.append(getContentType());
        result.append(" Title=");
        result.append(getTitle());
        result.append(" NumPages=");
        result.append(getNumPages());
        result.append(" Author=");
        result.append(getAuthor());
        result.append(" WordCount=");
        result.append(getWordCount());
        result.append(" Language=");
        result.append(getLanguage());
        result.append("]");
        return result.toString();
    }

}
