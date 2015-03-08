package aasenov.searchengine.crawler.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import aasenov.searchengine.Utils;
import aasenov.searchengine.crawler.CrawlerConfigData;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Crawler that saves retrieved pages to files.
 */
public class SaveToFileCrawler extends WebCrawler {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SaveToFileCrawler.class);

    private AtomicReference<CrawlerConfigData> mConfigData;

    @Override
    public void init(int id, CrawlController crawlController) {
        super.init(id, crawlController);
        mConfigData = (AtomicReference<CrawlerConfigData>) crawlController.getCustomData();

        // clean destination directory, as it may contain old files.
        File filesLocation = new File(((SaveToFileCrawlerConfigData) mConfigData.get()).getFilesDestinationDir());
        if (filesLocation.exists()) {
            Utils.rmdir(filesLocation);
        }
        filesLocation.mkdirs();
    }

    /**
     * Checks whether the given url should be crawled or not.
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !SaveToFileCrawlerConfigData.FILTERS.matcher(href).matches() && href.startsWith(mConfigData.get().getSiteToCrawl());
    }

    /**
     * Define what to do with page that was visited.
     */
    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

            mConfigData.get().incrementPagesCount();
            long localPageNumber = ((SaveToFileCrawlerConfigData) mConfigData.get()).getNextPageNumber();
            File htmlFileToSave = new File(((SaveToFileCrawlerConfigData) mConfigData.get()).getFilesDestinationDir(), String.format("page%s.html", localPageNumber));
            FileWriter htmlOut = null;
            try {
                htmlOut = new FileWriter(htmlFileToSave);
                // save text, without html markup
                htmlOut.write(htmlParseData.getHtml());
            } catch (Exception ex) {
                sLog.error(ex.getMessage(), ex);
            } finally {
                if (htmlOut != null) {
                    try {
                        htmlOut.close();
                    } catch (IOException e) {
                        // skip
                    }
                }
            }
        }
    }

}
