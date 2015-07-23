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
import org.restlet.data.Status;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import com.aasenov.searchengine.SearchManager;
import com.aasenov.searchengine.provider.SearchManagerProvider;

public class SearchResource extends WadlServerResource {

    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(SearchResource.class);

    /**
     * Default starting from the beginning.
     */
    protected static int DEFAULT_START_FROM = 0;

    /**
     * Default result size.
     */
    protected static int DEFAULT_SIZE = 100;

    /**
     * Parameter containing type of search to perform.
     */
    protected static final String PARAM_ACTION = "action";
    /**
     * Parameter containing search query to execute.
     */
    protected static final String PARAM_QUERY = "query";
    /**
     * Parameter containing start point for listing.
     */
    protected static final String PARAM_START = "start";

    /**
     * Parameter containing number of files to list.
     */
    protected static final String PARAM_COUNT = "count";

    /**
     * Perform search over files of logged user.<br/>
     * Options for searching:<br/>
     * <ul>
     * <li><b>action</b> - type of search to perform. One of {@link SearchType} constants.</li>
     * <li><b>query</b> - query to execute</li>
     * <li><b>startFrom</b> - start point for searching</li>
     * <li><b>size</b> - number of search results to return. Default is DEFAULT_SIZE</li>
     * </ul>
     * 
     * @return HTML formatted result from searching.
     */
    @Post("form:html")
    public Representation search(Representation entity) {
        final Form form = new Form(entity);
        String action = form.getFirstValue(PARAM_ACTION);
        String query = form.getFirstValue(PARAM_QUERY);
        String userID = getRequest().getChallengeResponse().getIdentifier();

        if (action == null || action.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            new StringRepresentation("No action specified", MediaType.TEXT_PLAIN);
        }

        if (query == null || query.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new StringRepresentation("No query specified", MediaType.TEXT_PLAIN);
        }

        String response;
        if (action.equalsIgnoreCase(SearchType.SEARCH.toString())) {
            int start = DEFAULT_START_FROM;
            try {
                start = Integer.parseInt(form.getFirstValue(PARAM_START));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve start parameter from '%s'. Defaulting to %s",
                        form.getFirstValue("startFrom"), DEFAULT_START_FROM));
            }
            int size = DEFAULT_SIZE;
            try {
                size = Integer.parseInt(form.getFirstValue("size"));
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve size parameter from '%s'. Defaulting to %s",
                        form.getFirstValue("size"), DEFAULT_SIZE));
            }

            sLog.info(String.format("Search Post received: action: %s, query:%s, from: %s, size:%s", action, query,
                    start, size));

            // check whether we have phrases
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(query);
            List<String> phrasesToMatch = new ArrayList<String>();
            while (matcher.find()) {
                phrasesToMatch.add(matcher.group(1));
            }
            try {
                if (phrasesToMatch.isEmpty()) {
                    response = performNormalSearch(query, userID, start, size);
                } else {
                    response = performPhraseSearch(query, phrasesToMatch, userID, start, size);
                }
            } catch (IOException e) {
                sLog.error(e.getMessage(), e);
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return new StringRepresentation("Error during searching: " + e.getMessage(), MediaType.TEXT_PLAIN);
            }
        } else if (action.equalsIgnoreCase(SearchType.SUGGEST.toString())) {
            sLog.info(String.format("Search Post received: action: %s, query:%s", action, query));
            try {
                response = performSuggestSearch(query, userID);
            } catch (IOException e) {
                sLog.error(e.getMessage(), e);
                setStatus(Status.SERVER_ERROR_INTERNAL);
                return new StringRepresentation("Error during searching: " + e.getMessage(), MediaType.TEXT_PLAIN);
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String errorMsg = String.format("'%s' action is unrecognized.", action);
            sLog.error(errorMsg);
            return new StringRepresentation(errorMsg, MediaType.TEXT_PLAIN);
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
     * @param userID - id of user to search for.
     * @param startFrom - start index for the search.
     * @param size - end index.
     * @return JSON formatted result.
     * @throws IOException - in case of error.
     */
    private static String performNormalSearch(String searchQuery, String userID, int startFrom, int size)
            throws IOException {
        int i = 0;
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeText(searchQuery,
                userID, startFrom, size);
        XContentBuilder builder = jsonBuilder().startObject().field("tookInMillis", searchResponse.getTookInMillis())
                .field("hits", searchResponse.getHits().totalHits());
        for (SearchHit hit : searchResponse.getHits()) {
            i++;
            builder.startObject("hit" + i);
            builder.field("score", hit.getScore());
            builder.field("documentID", hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY));
            builder.field("fileID", hit.getSource().get(SearchManager.FILE_ID_PROPERTY));
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
    }

    /**
     * Perform normal full text search query.
     * 
     * @param searchQuery - query to match.
     * @param phrases - phrases to match.
     * @param userID - id of user to search for.
     * @param startFrom - start index for the search.
     * @param size - end index.
     * @return JSON formatted result.
     * @throws IOException - in case of error.
     */
    private static String performPhraseSearch(String searchQuery, List<String> phrases, String userID, int startFrom,
            int size) throws IOException {
        sLog.info("Performing phrase search over: " + Arrays.toString(phrases.toArray()));

        int i = 0;
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeTextAndPhrase(
                searchQuery, phrases, userID, startFrom, size);
        XContentBuilder builder = jsonBuilder().startObject().field("tookInMillis", searchResponse.getTookInMillis())
                .field("hits", searchResponse.getHits().totalHits());
        for (SearchHit hit : searchResponse.getHits()) {
            i++;
            builder.startObject("hit" + i);
            builder.field("score", hit.getScore());
            builder.field("documentID", hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY));
            builder.field("fileID", hit.getSource().get(SearchManager.FILE_ID_PROPERTY));
            builder.field("documentTitle", hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY));
            StringBuilder highlights = new StringBuilder();
            try {
                for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_CONTENT_PROPERTY)
                        .getFragments()) {
                    highlights.append(highlightedText.toString());
                    highlights.append("...");
                }
            } catch (Exception ex) {
                sLog.error("Highlight field is empty for " + hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY),
                        ex);
            }
            builder.field("highlight", highlights.toString());
            builder.field("summary", hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY));
            builder.endObject();
        }
        return builder.endObject().string();
    }

    /**
     * Perform suggestion query.
     * 
     * @param searchQuery - query to execute
     * @param userID - id of user to search for..
     * @return JSON formatted result.
     * @throws IOException - in case of error.
     */
    private static String performSuggestSearch(String searchQuery, String userID) throws IOException {
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().suggest(searchQuery, userID);

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
        return jsonBuilder().startObject().field("hits", suggestion.size())
                .field("suggest", suggestion.isEmpty() ? "" : suggestion.toArray(new String[] {})).endObject().string();
    }
}
