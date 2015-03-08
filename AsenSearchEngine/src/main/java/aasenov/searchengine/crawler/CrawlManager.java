package aasenov.searchengine.crawler;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * Manager to operate with the crawling process.
 */
public class CrawlManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(CrawlManager.class);

    /**
     * Folder to store temporary files.
     */
    private String mCrawlWorkspaceFolder;

    /**
     * Controller used to start/stop crawling.
     */
    private CrawlController mController;

    /**
     * Constructor to set needed fields before crawling.
     * 
     * @param crawlWorkspaceFolder - place where crawler will store temp files.
     */
    public CrawlManager(String crawlWorkspaceFolder) {
        mCrawlWorkspaceFolder = crawlWorkspaceFolder;
    }

    /**
     * Start crawling.
     * 
     * @param crawlerToStart - crawler to be started.
     * @param data - data to pass to crawler.
     * @param numberOfCrawlers - how many crawlers to start.
     * 
     * @throws Exception - in case of error during crawler starting.
     */
    public void startCrawling(Class<? extends WebCrawler> crawlerToStart, AtomicReference<CrawlerConfigData> data, int numberOfCrawlers) throws Exception {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(mCrawlWorkspaceFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        mController = new CrawlController(config, pageFetcher, robotstxtServer);

        // Add custom data to make it available to the crawler.
        mController.setCustomData(data);

        // For each crawl, you need to add some seed urls. These are the first URLs that are fetched and then the crawler starts following links which are found in
        // these
        // pages
        mController.addSeed(data.get().getSiteToCrawl());

        // Start the crawl nonblocking, to be able to stop it whenever we want.
        if (sLog.isDebugEnabled()) {
            sLog.debug(String.format("Starting %s concurrent %s crawlers at %s", numberOfCrawlers, crawlerToStart.getName(), new Date()));
        }
        mController.startNonBlocking(crawlerToStart, numberOfCrawlers);
    }

    /**
     * Stop crawling.
     */
    public void stopCrawling() {
        if (mController != null) {
            if (sLog.isDebugEnabled()) {
                sLog.debug(String.format("Stopping crawling process at %s", new Date()));
            }
            mController.shutdown();
        } else {
            sLog.info("No crawling started!");
        }
    }

    /**
     * Checks whether crawling is finished.
     * 
     * @return <b>True</b> if current crawling is completed, <b>False</b> otherwise.
     */
    public boolean isReady() {
        if (mController != null) {
            return mController.isFinished();
        }
        return true;
    }
}
