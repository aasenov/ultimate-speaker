package aasenov.searchengine.crawler.file;

import java.util.concurrent.atomic.AtomicLong;

import aasenov.searchengine.crawler.CrawlerConfigData;

/**
 * This class contain configuration options, that are used from {@link SaveToFileCrawler} crawler.
 */
public class SaveToFileCrawlerConfigData extends CrawlerConfigData {

    private String mFilesDestinationDir;

    private AtomicLong mPageNumber = new AtomicLong(0);

    /**
     * Construct configuration data for {@link SaveToFileCrawler} crawler.
     * 
     * @param siteToCrawl - URL of the site to be crawled.
     * @param filesDestinationDir - path to directory, where crawled pages should be stored.
     */
    public SaveToFileCrawlerConfigData(String siteToCrawl, String filesDestinationDir) {
        super(siteToCrawl);
        mFilesDestinationDir = filesDestinationDir;
        mPageNumber = new AtomicLong();
    }

    /**
     * Retrieve path to directory, where crawled pages should be stored.
     * 
     * @return Absolute path to directory.
     */
    public String getFilesDestinationDir() {
        return mFilesDestinationDir;
    }

    /**
     * Retrieve unique page number, used to create unique names of pages.
     * 
     * @return Next number from sequence to use.
     */
    public long getNextPageNumber() {
        return mPageNumber.incrementAndGet();
    }

}
