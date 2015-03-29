package com.aasenov.searchengine.elasticsearch.web;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;

import com.aasenov.searchengine.Utils;
import com.aasenov.searchengine.elasticsearch.ElasticserachManager;

public class SearchServlet extends HttpServlet {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SearchServlet.class);

    /**
     * Default starting from the beginning.
     */
    private static int DEFAULT_START_FROM = 0;

    /**
     * Default result size.
     */
    private static int DEFAULT_SIZE = 100;

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        sLog.info("Starting elastic Search");
        ElasticserachManager.getInstance(); // inint elasticsearch on startup to prevent first user to wait initialization.
    }

    @Override
    public void destroy() {
        super.destroy();
        sLog.info("Stopping elastic Search");
        ElasticserachManager.getInstance().close();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        sLog.info(String.format("Search Get received: %s : %s", req, resp));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // add support for Cyrilic chars
        resp.setContentType("text/html;charset=UTF-8");

        String action = req.getParameter("action");
        String searchQuery = req.getParameter("searchQuery");

        PrintWriter out = resp.getWriter();
        String response;

        if (action.equals("StartSearching")) {
            int startFrom = DEFAULT_START_FROM;
            try {
                startFrom = Integer.parseInt(req.getParameter("startFrom"));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve startFrom parameter from '%s'. Defaulting to %s", req.getParameter("startFrom"), DEFAULT_START_FROM));
            }
            int size = DEFAULT_SIZE;
            try {
                size = Integer.parseInt(req.getParameter("size"));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve size parameter from '%s'. Defaulting to %s", req.getParameter("size"), DEFAULT_SIZE));
            }

            sLog.info(String.format("Search Post received: action: %s, query:%s, from: %s, size:%s", action, searchQuery, startFrom, size));

            // check whether we have phrases
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(searchQuery);
            List<String> phrasesToMatch = new ArrayList<String>();
            while (matcher.find()) {
                phrasesToMatch.add(matcher.group(1));
            }
            if (phrasesToMatch.isEmpty()) {
                response = performNormalSearch(searchQuery, startFrom, size);
            } else {
                response = performPhraseSearch(searchQuery, phrasesToMatch, startFrom, size);
            }
        } else if (action.equals("suggest")) {
            sLog.info(String.format("Search Post received: action: %s, query:%s", action, searchQuery));

            response = performSuggestSearch(searchQuery);
        } else {
            response = Utils.constructErrorResponse("Unrecognized command");
        }

        sLog.info(String.format("%s request completed", action));
        out.println(response);
    }

    /**
     * Perform normal full text search query.
     * 
     * @param searchQuery - query to match.
     * @param startFrom - start index for the search.
     * @param size - end index.
     * @return JSON formatted result.
     */
    private static String performNormalSearch(String searchQuery, int startFrom, int size) {
        try {
            int i = 0;
            SearchResponse searchResponse = ElasticserachManager.getInstance().searchFreeText(searchQuery, startFrom, size);
            XContentBuilder builder = jsonBuilder().startObject().field("tookInMillis", searchResponse.getTookInMillis())
                    .field("hits", searchResponse.getHits().totalHits());
            for (SearchHit hit : searchResponse.getHits()) {
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
            return builder.endObject().string();
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return Utils.constructErrorResponse(ex.getMessage());
        }
    }

    /**
     * Perform normal full text search query.
     * 
     * @param searchQuery - query to match.
     * @param phrases - phrases to match.
     * @param startFrom - start index for the search.
     * @param size - end index.
     * @return JSON formatted result.
     */
    private static String performPhraseSearch(String searchQuery, List<String> phrases, int startFrom, int size) {
        sLog.info("Performing phrase search over: " + Arrays.toString(phrases.toArray()));
        try {
            int i = 0;
            SearchResponse searchResponse = ElasticserachManager.getInstance().searchFreeTextAndPhrase(searchQuery, phrases, startFrom, size);
            XContentBuilder builder = jsonBuilder().startObject().field("tookInMillis", searchResponse.getTookInMillis())
                    .field("hits", searchResponse.getHits().totalHits());
            for (SearchHit hit : searchResponse.getHits()) {
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
            return builder.endObject().string();
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            return Utils.constructErrorResponse(ex.getMessage());
        }
    }

    /**
     * Perform suggestion query.
     * 
     * @param searchQuery - query to execute.
     * @return JSON formatted result.
     */
    private static String performSuggestSearch(String searchQuery) {
        SearchResponse searchResponse = ElasticserachManager.getInstance().suggest(searchQuery);

        List<String> suggestion = new ArrayList<String>();
        for (SearchHit hit : searchResponse.getHits()) {
            try {
                for (Text highlightedText : hit.getHighlightFields().get(ElasticserachManager.PAGE_TITLE_PROPERTY).getFragments()) {
                    Pattern pattern = Pattern.compile("<strong>(.*?)</strong>( <strong>(.*?)</strong>)*");
                    Matcher matcher = pattern.matcher(highlightedText.toString());
                    while (matcher.find()) {
                        String term = matcher.group().replaceAll("<strong>", "").replaceAll("</strong>", "").toLowerCase();
                        if (!term.startsWith(searchQuery.toLowerCase().trim()) && !searchQuery.toLowerCase().contains(term)) {
                            // this is not a phrase match, append original query to this term
                            String baseQuery = searchQuery.substring(0, searchQuery.lastIndexOf(" ") > 0 ? searchQuery.lastIndexOf(" ") + 1 : 0);
                            term = baseQuery + term;
                        }
                        if (!suggestion.contains(term)) {
                            suggestion.add(term);
                        }
                    }
                }
            } catch (Exception ex) {
                sLog.error("Highlight field is empty for " + hit.getSource().get(ElasticserachManager.PAGE_URL_PROPERTY), ex);
            }
        }
        try {
            return jsonBuilder().startObject().field("hits", suggestion.size()).field("suggest", suggestion.isEmpty() ? "" : suggestion.toArray(new String[] {}))
                    .endObject().string();
        } catch (IOException ex) {
            sLog.error(ex.getMessage(), ex);
            return Utils.constructErrorResponse(ex.getMessage());
        }
    }

}
