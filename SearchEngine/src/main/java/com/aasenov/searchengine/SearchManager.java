package com.aasenov.searchengine;

import java.util.List;

import org.elasticsearch.action.search.SearchResponse;

public interface SearchManager {

    /**
     * Name of field, where we store the content.
     */
    public static String DOCUMENT_CONTENT_PROPERTY = "documentContent";

    /**
     * Name of field, where we store the document ID.
     */
    public static String DOCUMENT_ID_PROPERTY = "documentID";

    /**
     * Name of field, where we store the document name.
     */
    public static String DOCUMENT_TITLE_PROPERTY = "documentTitle";

    /**
     * Name of field, where we store the document summary.
     */
    public static String DOCUMENT_SUMMARY_PROPERTY = "documentSummary";

    /**
     * Initialize needed resources.
     */
    public void initialize();

    /**
     * Delete old index, if exists, and create new one.
     */
    public void recreateIndex();

    /**
     * Index given content.
     * 
     * @param content - content of document to be indexed.
     * @param documentID - ID of indexed document.
     * @param title - title of the page.
     */
    public void indexDocument(String content, String documentID, String title);

    /**
     * Delete index for document with given ID.
     * 
     * @param documentID - ID of document to delete.
     */
    public void deleteIndexedDocument(String documentID);

    /**
     * Generates Summary based on term frequencies.
     * 
     * @param docID - ID of document to generate summary of.
     * @param originalContent - content from where we will retrieve sentences for evaluation.
     * @return Generated summary.
     */
    public String generateSummary(String docID, String originalContent);

    /**
     * Perform free text match for given query string.
     * 
     * @param query - query to match.
     * @param from - start index.
     * @param size - number of results to return.
     * @return Response from the executed query.
     */
    public SearchResponse searchFreeText(String query, int from, int size);

    /**
     * Perform phrase match. Phrases must match and freeTextQuery is used to boost score.
     * 
     * @param freeTextQuery - query to match.
     * @param phrasesQuery - phrases to match.
     * @param from - start index.
     * @param size - number of results to return.
     * @return Reponse from the executed query.
     */
    public SearchResponse searchFreeTextAndPhrase(String freeTextQuery, List<String> phrasesQuery, int from, int size);

    /**
     * Perform query over n-gram indexed field.
     * 
     * @param query - query to execute.
     * @return Response from the executed query.
     */
    public SearchResponse suggest(String query);

    /**
     * Close and clean all opened resources.
     */
    public void close();

    /**
     * Cleanup storage folders.
     */
    public void deleteStorageFolders();
}
