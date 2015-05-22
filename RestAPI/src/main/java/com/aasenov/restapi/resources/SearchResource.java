package com.aasenov.restapi.resources;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.aasenov.restapi.util.Helper;
import com.aasenov.searchengine.SearchManager;
import com.aasenov.searchengine.Utils;
import com.aasenov.searchengine.provider.SearchManagerProvider;

public class SearchResource extends ServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SearchResource.class);

    /**
     * Default starting from the beginning.
     */
    private static int DEFAULT_START_FROM = 0;

    /**
     * Default result size.
     */
    private static int DEFAULT_SIZE = 100;

    /**
     * List all files from database.<br/>
     * Options for listing:<br/>
     * <ul>
     * <li><b>start</b> - start point for returned page of files</li>
     * <li><b>count</b> - number of files to return. Default is DEFAULT_PAGE_SIZE</li>
     * <li><b>out</b> - type of return result. One of {@link ResponseType} constants.</li>
     * </ul>
     * 
     * @return List of files, formatted based on passed criteria parameters.
     */
    @Post
    public Representation search(Representation entity) {
        Helper.enableCORS(getResponse());

        final Form form = new Form(entity);
        String action = form.getFirstValue("action");
        String searchQuery = form.getFirstValue("searchQuery");

        String response;

        if (action.equals("StartSearching")) {
            int startFrom = DEFAULT_START_FROM;
            try {
                startFrom = Integer.parseInt(form.getFirstValue("startFrom"));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve startFrom parameter from '%s'. Defaulting to %s",
                        form.getFirstValue("startFrom"), DEFAULT_START_FROM));
            }
            int size = DEFAULT_SIZE;
            try {
                size = Integer.parseInt(form.getFirstValue("size"));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve size parameter from '%s'. Defaulting to %s",
                        form.getFirstValue("size"), DEFAULT_SIZE));
            }

            sLog.info(String.format("Search Post received: action: %s, query:%s, from: %s, size:%s", action,
                    searchQuery, startFrom, size));

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

        StringRepresentation result = new StringRepresentation(response);
        result.setMediaType(MediaType.TEXT_HTML);
        result.setCharacterSet(CharacterSet.UTF_8);// add support for Cyrilic chars
        return result;
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
            SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeText(searchQuery,
                    startFrom, size);
            XContentBuilder builder = jsonBuilder().startObject()
                    .field("tookInMillis", searchResponse.getTookInMillis())
                    .field("hits", searchResponse.getHits().totalHits());
            for (SearchHit hit : searchResponse.getHits()) {
                i++;
                builder.startObject("hit" + i);
                builder.field("score", hit.getScore());
                builder.field("documentID", hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY));
                builder.field("documentTitle", hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY));
                StringBuilder highlights = new StringBuilder();
                try {
                    for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_CONTENT_PROPERTY)
                            .getFragments()) {
                        highlights.append(highlightedText.toString());
                        highlights.append("...");
                    }
                } catch (NullPointerException ex) {
                    sLog.error(String.format("Highlight field is empty for %s with id %s",
                            hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY),
                            hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY)));
                } catch (Exception ex) {
                    sLog.error(
                            String.format("Highlight field is empty for %s with id %s",
                                    hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY),
                                    hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY)), ex);
                }
                builder.field("highlight",
                        highlights.toString().isEmpty() ? hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY)
                                : highlights.toString()); // set summary as highlight if none.
                builder.field("summary", hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY));
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
            SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeTextAndPhrase(
                    searchQuery, phrases, startFrom, size);
            XContentBuilder builder = jsonBuilder().startObject()
                    .field("tookInMillis", searchResponse.getTookInMillis())
                    .field("hits", searchResponse.getHits().totalHits());
            for (SearchHit hit : searchResponse.getHits()) {
                i++;
                builder.startObject("hit" + i);
                builder.field("score", hit.getScore());
                builder.field("documentID", hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY));
                builder.field("documentTitle", hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY));
                StringBuilder highlights = new StringBuilder();
                try {
                    for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_CONTENT_PROPERTY)
                            .getFragments()) {
                        highlights.append(highlightedText.toString());
                        highlights.append("...");
                    }
                } catch (Exception ex) {
                    sLog.error("Highlight field is empty for "
                            + hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY), ex);
                }
                builder.field("highlight", highlights.toString());
                builder.field("summary", hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY));
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
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().suggest(searchQuery);

        List<String> suggestion = new ArrayList<String>();
        for (SearchHit hit : searchResponse.getHits()) {
            try {
                for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_TITLE_PROPERTY)
                        .getFragments()) {
                    Pattern pattern = Pattern.compile("<strong>(.*?)</strong>( <strong>(.*?)</strong>)*");
                    Matcher matcher = pattern.matcher(highlightedText.toString());
                    while (matcher.find()) {
                        String term = matcher.group().replaceAll("<strong>", "").replaceAll("</strong>", "")
                                .toLowerCase();
                        if (!term.startsWith(searchQuery.toLowerCase().trim())
                                && !searchQuery.toLowerCase().contains(term)) {
                            // this is not a phrase match, append original query to this term
                            String baseQuery = searchQuery.substring(0,
                                    searchQuery.lastIndexOf(" ") > 0 ? searchQuery.lastIndexOf(" ") + 1 : 0);
                            term = baseQuery + term;
                        }
                        if (!suggestion.contains(term)) {
                            suggestion.add(term);
                        }
                    }
                }
            } catch (Exception ex) {
                sLog.error("Highlight field is empty for " + hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY),
                        ex);
            }
        }
        try {
            return jsonBuilder().startObject().field("hits", suggestion.size())
                    .field("suggest", suggestion.isEmpty() ? "" : suggestion.toArray(new String[] {})).endObject()
                    .string();
        } catch (IOException ex) {
            sLog.error(ex.getMessage(), ex);
            return Utils.constructErrorResponse(ex.getMessage());
        }
    }
}
