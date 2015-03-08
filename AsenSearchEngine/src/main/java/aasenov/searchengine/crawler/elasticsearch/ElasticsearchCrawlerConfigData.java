package aasenov.searchengine.crawler.elasticsearch;

import aasenov.searchengine.crawler.CrawlerConfigData;
import aasenov.searchengine.elasticsearch.ElasticserachManager;

/**
 * This class contain configuration options, that are used from {@link ElasticsearchCrawler} crawler.
 */
public class ElasticsearchCrawlerConfigData extends CrawlerConfigData {

    ElasticserachManager mManager;

    /**
     * Construct configuration data for {@link ElasticsearchCrawler} crawler.
     * 
     * @param siteToCrawl - URL of the site to be crawled.
     * @param manager - {@link ElasticserachManager} to use.
     */
    public ElasticsearchCrawlerConfigData(String siteToCrawl, ElasticserachManager manager) {
        super(siteToCrawl);
        mManager = manager;
    }

    /**
     * Retrieve manager, used to index files.
     * 
     * @return {@link ElasticserachManager} instance.
     */
    public ElasticserachManager getManager() {
        return mManager;
    }
}
