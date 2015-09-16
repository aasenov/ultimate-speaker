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
import org.restlet.resource.Put;

import com.aasenov.restapi.managers.FileManager;
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
    protected static int DEFAULT_COUNT = 100;

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
     * Name of field, where we store the file ID.
     */
    public static String FIELD_FILE_ID = SearchManager.FILE_ID_PROPERTY;

    /**
     * Name of field, where we store the document ID.
     */
    public static String FIELD_DOCUMENT_ID = SearchManager.DOCUMENT_ID_PROPERTY;

    /**
     * Name of field, where we store the document name.
     */
    public static String FIELD_DOCUMENT_TITLE = SearchManager.DOCUMENT_TITLE_PROPERTY;

    /**
     * Name of field, where we store the highlighted text.
     */
    public static String FIELD_HIGHLIGHT = "highlight";

    /**
     * Name of field, where we store the summary text.
     */
    public static String FIELD_SUMMARY = "summary";

    /**
     * Name of field, where we store number of hits.
     */
    public static String FIELD_HITS = "hits";

    /**
     * Name of field, where we store search score for current hit.
     */
    public static String FIELD_SCORE = "score";

    /**
     * Name of field, where we store rating for current hit.
     */
    public static String FIELD_RATING = "rating";

    /**
     * Name of field, where we store number of milliseconds taken for search to complete.
     */
    public static String FIELD_TOOK_TIME = "tookInMillis";

    /**
     * Name of field, where we store result of suggest search.
     */
    public static String FIELD_SUGGEST = "suggest";

    /**
     * Perform search over files of logged user.<br/>
     * Options for searching:<br/>
     * <ul>
     * <li><b>action</b> - type of search to perform. One of {@link SearchType} constants.</li>
     * <li><b>query</b> - query to execute</li>
     * <li><b>start</b> - start point for searching</li>
     * <li><b>count</b> - number of search results to return. Default is DEFAULT_COUNT</li>
     * </ul>
     * 
     * @return HTML formatted result from searching.
     */
    @Put("form:html")
    public Representation search(Representation entity) {
        sLog.info("Request for searching received!");
        final Form form = new Form(entity);
        String action = form.getFirstValue(PARAM_ACTION);
        String query = form.getFirstValue(PARAM_QUERY);
        String userID = getRequest().getChallengeResponse().getIdentifier();

        if (action == null || action.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No action specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        if (query == null || query.isEmpty()) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            String message = "No query specified";
            sLog.error(message);
            return new StringRepresentation(message, MediaType.TEXT_PLAIN);
        }

        String response;
        if (action.equalsIgnoreCase(SearchType.SEARCH.toString())) {
            int start = DEFAULT_START_FROM;
            String startStr = form.getFirstValue(PARAM_START);
            try {
                start = Integer.parseInt(startStr);
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve start parameter from '%s'. Defaulting to %s", startStr,
                        DEFAULT_START_FROM));
            }

            int count = DEFAULT_COUNT;
            String countStr = form.getFirstValue(PARAM_COUNT);
            try {
                count = Integer.parseInt(countStr);
            } catch (Exception ex) {
                sLog.info(String.format("Unable to retrieve count parameter from '%s'. Defaulting to %s", countStr,
                        DEFAULT_COUNT));
            }

            sLog.info(String.format("Performing search query - action: %s, query:%s, from: %s, count:%s", action,
                    query, start, count));

            // check whether we have phrases
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(query);
            List<String> phrasesToMatch = new ArrayList<String>();
            while (matcher.find()) {
                phrasesToMatch.add(matcher.group(1));
            }
            try {
                if (phrasesToMatch.isEmpty()) {
                    response = performNormalSearch(query, userID, start, count);
                } else {
                    response = performPhraseSearch(query, phrasesToMatch, userID, start, count);
                }
            } catch (IOException e) {
                setStatus(Status.SERVER_ERROR_INTERNAL);
                String message = "Error during searching: " + e.getMessage();
                sLog.error(message, e);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
            }
        } else if (action.equalsIgnoreCase(SearchType.SUGGEST.toString())) {
            sLog.info(String.format("Performing suggest query -  action: %s, query:%s", action, query));
            try {
                response = performSuggestSearch(query, userID);
            } catch (IOException e) {
                setStatus(Status.SERVER_ERROR_INTERNAL);
                String message = "Error during searching: " + e.getMessage();
                sLog.error(message, e);
                return new StringRepresentation(message, MediaType.TEXT_PLAIN);
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
     * @param count - number of results to return.
     * @return JSON formatted result.
     * @throws IOException - in case of error.
     */
    private static String performNormalSearch(String searchQuery, String userID, int startFrom, int count)
            throws IOException {
        int i = 0;
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeText(searchQuery,
                userID, startFrom, count);
        XContentBuilder builder = jsonBuilder().startObject().field(FIELD_TOOK_TIME, searchResponse.getTookInMillis())
                .field(FIELD_HITS, searchResponse.getHits().totalHits());
        for (SearchHit hit : searchResponse.getHits()) {
            i++;
            builder.startObject("hit" + i);
            builder.field(FIELD_SCORE, hit.getScore());

            String documentID = (String) hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY);
            builder.field(FIELD_DOCUMENT_ID, documentID);

            String fileID = (String) hit.getSource().get(SearchManager.FILE_ID_PROPERTY);
            builder.field(FIELD_FILE_ID, fileID);
            builder.field(FIELD_RATING, FileManager.getInstance().getRatingForFile(fileID));

            String documentTitle = (String) hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY);
            builder.field(FIELD_DOCUMENT_TITLE, documentTitle);

            StringBuilder highlights = new StringBuilder();
            try {
                for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_CONTENT_PROPERTY)
                        .getFragments()) {
                    highlights.append(highlightedText.toString());
                    highlights.append("...");
                }
            } catch (Exception ex) {
                sLog.error(String.format("Problem constructing highlights for file '%s' with id '%s'", documentTitle,
                        documentID), ex);
            }
            builder.field(FIELD_HIGHLIGHT,
                    highlights.toString().isEmpty() ? hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY)
                            : highlights.toString()); // set summary as highlight if none.
            builder.field(FIELD_SUMMARY, hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY));
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
     * @param count - number of results to return.
     * @return JSON formatted result.
     * @throws IOException - in case of error.
     */
    private static String performPhraseSearch(String searchQuery, List<String> phrases, String userID, int startFrom,
            int count) throws IOException {
        sLog.info("Performing phrase search over: " + Arrays.toString(phrases.toArray()));

        int i = 0;
        SearchResponse searchResponse = SearchManagerProvider.getDefaultSearchManager().searchFreeTextAndPhrase(
                searchQuery, phrases, userID, startFrom, count);
        XContentBuilder builder = jsonBuilder().startObject().field(FIELD_TOOK_TIME, searchResponse.getTookInMillis())
                .field(FIELD_HITS, searchResponse.getHits().totalHits());
        for (SearchHit hit : searchResponse.getHits()) {
            i++;
            builder.startObject("hit" + i);
            builder.field(FIELD_SCORE, hit.getScore());
            String documentID = (String) hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY);
            builder.field(FIELD_DOCUMENT_ID, documentID);
            String fileID = (String) hit.getSource().get(SearchManager.FILE_ID_PROPERTY);
            builder.field(FIELD_FILE_ID, fileID);
            builder.field(FIELD_RATING, FileManager.getInstance().getRatingForFile(fileID));
            String documentTitle = (String) hit.getSource().get(SearchManager.DOCUMENT_TITLE_PROPERTY);
            builder.field(FIELD_DOCUMENT_TITLE, documentTitle);
            StringBuilder highlights = new StringBuilder();
            try {
                for (Text highlightedText : hit.getHighlightFields().get(SearchManager.DOCUMENT_CONTENT_PROPERTY)
                        .getFragments()) {
                    highlights.append(highlightedText.toString());
                    highlights.append("...");
                }
            } catch (Exception ex) {
                sLog.error(String.format("Problem constructing highlights for file '%s' with id '%s'", documentTitle,
                        documentID), ex);
            }
            builder.field(FIELD_HIGHLIGHT, highlights.toString());
            builder.field(FIELD_SUMMARY, hit.getSource().get(SearchManager.DOCUMENT_SUMMARY_PROPERTY));
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
                sLog.error(
                        String.format("Problem constructing suggestions for file with id '%s'",
                                hit.getSource().get(SearchManager.DOCUMENT_ID_PROPERTY)), ex);
            }
        }

        return jsonBuilder().startObject().field(FIELD_HITS, suggestion.size())
                .field(FIELD_SUGGEST, suggestion.isEmpty() ? "" : suggestion.toArray(new String[] {})).endObject()
                .string();
    }
}
