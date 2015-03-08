package aasenov.searchengine.crawler.test;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;

import aasenov.searchengine.Utils;
import aasenov.searchengine.crawler.CrawlManager;
import aasenov.searchengine.crawler.CrawlerConfigData;
import aasenov.searchengine.crawler.elasticsearch.ElasticsearchCrawler;
import aasenov.searchengine.crawler.elasticsearch.ElasticsearchCrawlerConfigData;
import aasenov.searchengine.elasticsearch.ElasticserachManager;

public class TestCrawler {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(TestCrawler.class);
    private static final String NEW_LINE = System.getenv("line.separator");

    public static void main(String[] args) throws Exception {
//        ElasticserachManager.getInstance().recreateIndex();
//        try {
//            CrawlManager manager = new CrawlManager("crawl/root");
//            AtomicReference<CrawlerConfigData> data = new AtomicReference<CrawlerConfigData>(new ElasticsearchCrawlerConfigData("http://sportal.bg",
//                    ElasticserachManager.getInstance()));
//            manager.startCrawling(ElasticsearchCrawler.class, data, 10);
//            Thread.sleep(60000);
//            manager.stopCrawling();
//            Thread.sleep(1000);
//        } finally {
//        }
        try {
            int i = 0;
            SearchResponse searchResponse = ElasticserachManager.getInstance().searchFreeText("Статистика", 0, 10);
            XContentBuilder builder = jsonBuilder().startObject().field("tookInMillis", searchResponse.getTookInMillis())
                    .field("hits", searchResponse.getHits().totalHits());
            for (SearchHit hit : searchResponse.getHits()) {
                // ElasticserachManager.getInstance().generateSummary(hit.getId(), (String) hit.getSource().get(ElasticserachManager.PAGE_CONTENT_PROPERTY));

                i++;
                builder.startObject("hit" + i);
                builder.field("score", hit.getScore());
                builder.field("pageURL", hit.getSource().get(ElasticserachManager.PAGE_URL_PROPERTY));
                builder.field("pageTitle", hit.getSource().get(ElasticserachManager.PAGE_TITLE_PROPERTY));
                StringBuilder highlights = new StringBuilder();
                try {
                    for (Text highlightedText : hit.getHighlightFields().get(ElasticserachManager.PAGE_CONTENT_PROPERTY).getFragments()) {
                        highlights.append(highlightedText.toString());
                        highlights.append("...");
                    }
                } catch (Exception ex) {
                    sLog.error("Highlight field is empty for " + hit.getSource().get(ElasticserachManager.PAGE_URL_PROPERTY), ex);
                }
                builder.field("highlight", highlights.toString());
                builder.field("summary", hit.getSource().get(ElasticserachManager.PAGE_SUMMARY_PROPERTY));
                builder.endObject();
            }
            System.out.println(builder.endObject().prettyPrint().string());
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
        }
        // manager.testAnalyzer("Асен караше иванов скачаше");
        // // index some pages
        // for (int i = 1; i <= 3; i++) {
        // FileReader reader = null;
        // BufferedReader br = null;
        // try {
        // reader = new FileReader(String.format("page%s.html", i));
        // br = new BufferedReader(reader);
        // StringBuilder result = new StringBuilder();
        // String line = br.readLine();
        // while (line != null) {
        // result.append(line);
        // result.append(NEW_LINE);
        // line = br.readLine();
        // }
        // manager.indexDocument(result.toString(), String.format("page%s.html", i));
        // } finally {
        // if (br != null) {
        // br.close();
        // }
        // if (reader != null) {
        // reader.close();
        // }
        // }
        // }
        // ElasticserachManager.getInstance();
        // Thread.sleep(3000);
        // ElasticserachManager.getInstance().search("лудогорец");
        // ElasticserachManager.getInstance().close();
        // on shutdown

    }
}
