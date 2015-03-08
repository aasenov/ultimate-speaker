package aasenov.searchengine.elasticsearch.web;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import aasenov.searchengine.Utils;
import aasenov.searchengine.crawler.CrawlManager;
import aasenov.searchengine.crawler.CrawlerConfigData;
import aasenov.searchengine.crawler.elasticsearch.ElasticsearchCrawler;
import aasenov.searchengine.crawler.elasticsearch.ElasticsearchCrawlerConfigData;
import aasenov.searchengine.elasticsearch.ElasticserachManager;

public class CrawlServlet extends HttpServlet {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(CrawlServlet.class);
    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Manager used for site crawling.
     */
    private static volatile CrawlManager mCrawlManager;

    /**
     * Configuration data used.
     */
    private static AtomicReference<CrawlerConfigData> mData = new AtomicReference<CrawlerConfigData>();

    /**
     * Directory to store temporary crawling files.
     */
    private static String mCrawlerWorkingDir;

    @Override
    public void init() throws ServletException {
        super.init();
        // relative to WebApp root directory.
        mCrawlerWorkingDir = getServletContext().getRealPath(File.separator) + "/crawl/root";
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter out = resp.getWriter();
        String response;
        if (mCrawlManager != null) {
            if (mCrawlManager.isReady()) {
                mCrawlManager.stopCrawling();
                mCrawlManager = null;
                response = jsonBuilder().startObject().field("status", "finished").field("numProcessed", mData.get().getNumPagesCrawled()).endObject().string();
            } else {
                response = jsonBuilder().startObject().field("status", "crawling").field("numProcessed", mData.get().getNumPagesCrawled()).endObject().string();
            }
        } else {
            response = Utils.constructErrorResponse("No active crawling!!!");
        }

        out.println(response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String siteURL = req.getParameter("siteURL");
        sLog.info(String.format("Crawl Post received. Action '%s', siteURL '%s'", action, siteURL));

        PrintWriter out = resp.getWriter();
        String response;
        if (action.equals("StartCrawling")) {
            if (mCrawlManager != null) {
                response = Utils.constructErrorResponse("Crawling awready rinning. Please stop it, before starting new one!");
                sLog.info(response);
            } else {
                // create new index for the site
                ElasticserachManager.getInstance().recreateIndex();

                mCrawlManager = new CrawlManager(mCrawlerWorkingDir);
                mData.set(new ElasticsearchCrawlerConfigData(siteURL, ElasticserachManager.getInstance()));
                try {
                    mCrawlManager.startCrawling(ElasticsearchCrawler.class, mData, 10);
                    response = jsonBuilder().startObject().field("status", "crawling").field("numProcessed", mData.get().getNumPagesCrawled()).endObject().string();
                } catch (Exception e) {
                    sLog.error(e.getMessage(), e);
                    response = Utils.constructErrorResponse(e.getMessage());
                }
            }
        } else if (action.equals("StopCrawling")) {
            if (mCrawlManager != null) {
                mCrawlManager.stopCrawling();
                mCrawlManager = null;
            }
            response = jsonBuilder().startObject().field("status", "finished").field("numProcessed", mData.get().getNumPagesCrawled()).endObject().string();
        } else {
            response = Utils.constructErrorResponse("Unrecognized command");
        }
        out.println(response);
    }

}
