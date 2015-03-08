package aasenov.searchengine.crawler;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * This class contain configuration options, that are used from all crawlers.
 */
public class CrawlerConfigData {

    /**
     * Filter containing extensions that shouldn't be followed
     */
    public final static Pattern FILTERS = Pattern
            .compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private String mSiteToCrawl;

    private AtomicLong mPagesCrawled;

    /**
     * Initialize the crawler config data.
     * 
     * @param siteToCrawl - URL of the site to be crawled.
     */
    public CrawlerConfigData(String siteToCrawl) {
        mSiteToCrawl = siteToCrawl;
        mPagesCrawled = new AtomicLong();
    }

    /**
     * Retrieve URL of site to crawl.
     * 
     * @return URL of site to start crawling.
     */
    public String getSiteToCrawl() {
        return mSiteToCrawl;
    }

    /**
     * Increment pages counter.
     */
    public void incrementPagesCount() {
        mPagesCrawled.incrementAndGet();
    }

    /**
     * Checks how many pages we've crawled.
     * 
     * @return Number of pages currently crawled.
     */
    public long getNumPagesCrawled() {
        return mPagesCrawled.get();
    }
}
