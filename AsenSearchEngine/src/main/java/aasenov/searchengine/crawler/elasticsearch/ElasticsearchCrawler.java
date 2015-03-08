package aasenov.searchengine.crawler.elasticsearch;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import aasenov.searchengine.crawler.CrawlerConfigData;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Crawler that send crawled pages for indexing in Elasticsearch.
 */
public class ElasticsearchCrawler extends WebCrawler {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(ElasticsearchCrawler.class);

    private AtomicReference<CrawlerConfigData> mConfigData;

    @Override
    public void init(int id, CrawlController crawlController) {
        super.init(id, crawlController);
        mConfigData = (AtomicReference<CrawlerConfigData>) crawlController.getCustomData();
    }

    /**
     * Checks whether the given url should be crawled or not.
     */
    @Override
    public boolean shouldVisit(WebURL url) {
        String href = url.getURL().toLowerCase();
        return !CrawlerConfigData.FILTERS.matcher(href).matches() && href.startsWith(mConfigData.get().getSiteToCrawl());
    }

    /**
     * Define what to do with page that was visited.
     */
    @Override
    public void visit(Page page) {
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            mConfigData.get().incrementPagesCount();
            ((ElasticsearchCrawlerConfigData) mConfigData.get()).getManager().indexDocument(htmlParseData.getText(), page.getWebURL().getURL(), htmlParseData.getTitle());
        }
    }

}
