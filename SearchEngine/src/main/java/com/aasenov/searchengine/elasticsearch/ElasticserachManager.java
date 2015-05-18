package com.aasenov.searchengine.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.TermsEnum;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvector.TermVectorResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;

import com.aasenov.searchengine.SearchManager;

/**
 * Use this manager to perform various operations over elasticsearch cluster/node
 */
public class ElasticserachManager implements SearchManager {
    /**
     * Logger instance.
     */
    private static Logger sLog = Logger.getLogger(ElasticserachManager.class);

    /**
     * Name of index to use.
     */
    private static String INDEX_NAME = "ultimatespeaker";

    /**
     * Type of documents, that will be indexed.
     */
    private static String TYPE_NAME = "ultimatespeaker_type";

    /**
     * Name of analyzer, used for content and title fields.
     */
    private static String CUSTOM_ANALYZER_NAME = "ultimatespeakerAnalyzer";

    /**
     * Name of field, where we store the content.
     */
    private static String AUTOCOMPLETE_ANALYZER_NAME = "autocompleteAnalyzer";

    /**
     * Maximum number of characters that sentence may contain.
     */
    private static int MAX_SENTENCE_LENGTH = 250;

    /**
     * Node object, used to open/close resouces.
     */
    private Node mNode;

    /**
     * Client object, used to comunicate with the node.
     */
    private Client mClient;

    /**
     * Static instance of this class.
     */
    private static ElasticserachManager sInstance;

    /**
     * Initialize this manager. During initialization needed indexes are created and the manager is ready to index
     * documents.
     */
    private ElasticserachManager() {
        mNode = new NodeBuilder().node();
        mClient = mNode.client();

        // check whether cluster started
        try {
            if (isIndexExists()) {
                sLog.info(String.format("Index %s already created. Reuse!", INDEX_NAME));
            } else {
                createIndex();
            }
        } catch (Exception ex) {
            sLog.error(ex.getMessage(), ex);
            stopNode();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieve static instance of this class.
     * 
     * @return Initialize {@link ElasticserachManager} instance.
     */
    public synchronized static ElasticserachManager getInstance() {
        if (sInstance == null) {
            sInstance = new ElasticserachManager();
        }
        return sInstance;
    }

    /**
     * Stops node instance.
     */
    private void stopNode() {
        if (mNode != null) {
            mNode.close();
        }
    }

    @Override
    public void recreateIndex() {
        if (isIndexExists()) {
            deleteIndex();
            createIndex();
        } else {
            createIndex();
        }
    }

    /**
     * Checks whether index exists in current cluster.
     * 
     * @return<b>True</b> if index exists, <b>False</b> otherwise.
     */
    private boolean isIndexExists() {
        boolean clusterStarted = false;
        int numTries = 0;
        while (!clusterStarted) {
            try {
                ActionFuture<IndicesStatsResponse> future = mClient.admin().indices().stats(new IndicesStatsRequest());
                IndicesStatsResponse response = future.actionGet();
                Map<String, IndexStats> result = response.getIndices();
                if (result == null || result.isEmpty()) {
                    if (numTries < 5) {
                        numTries++;
                        sLog.info("No indexes found. Retry:" + numTries);
                        // wait for cluster starting
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            sLog.error(e.getMessage(), e);
                        }
                        continue;
                    }
                    sLog.info("No indexes found");
                    return false;
                }
                for (String index : result.keySet()) {
                    sLog.info(String.format("Index: %s, numShards: %s", index, result.get(index).getIndexShards()
                            .size()));
                }
                return true;
            } catch (ClusterBlockException ex) {
                sLog.info("Cluster not started. Wait initialization!");
                // wait for cluster starting
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    sLog.error(e.getMessage(), e);
                }
            } catch (Exception ex) {
                clusterStarted = true;
                sLog.error(ex.getMessage(), ex);
                new RuntimeException(ex);
            }
        }

        return false;
    }

    /**
     * Delete index from the cluster.
     */
    private void deleteIndex() {
        DeleteIndexResponse delete = mClient.admin().indices().delete(new DeleteIndexRequest(INDEX_NAME)).actionGet();
        if (!delete.isAcknowledged()) {
            sLog.error("Index wasn't deleted");
        } else {
            sLog.info(String.format("Index with name %s deleted.", INDEX_NAME));
        }
    }

    /**
     * Create index to be used, defining setting and mappings for type used.
     */
    private void createIndex() {
        CreateIndexRequestBuilder createIndexBuilder = mClient.admin().indices().prepareCreate(INDEX_NAME);

        // create index with only 1 Shard and no replicas, as we will use
        ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
        settingsBuilder.put(IndexMetaData.SETTING_NUMBER_OF_SHARDS, 1);
        settingsBuilder.put(IndexMetaData.SETTING_NUMBER_OF_REPLICAS, 0);

        // custom analyzer
        try {
            String customAnalyzer = jsonBuilder().startObject().
                                        startObject("analysis").
                                            startObject("filter").
                                                startObject("autocomplete_filter").
                                                    field("type", "edge_ngram").
                                                    field("min_gram", 1).
                                                    field("max_gram", 20).
                                                endObject().
                                                startObject("bulgarian_stop").
                                                    field("type", "stop").
                                                    field("stopwords", "_bulgarian_").
                                                endObject().
                                                startObject("english_stop").
                                                    field("type", "stop").
                                                    field("stopwords", "_english_").
                                                endObject().
                                            endObject().
                                            startObject("analyzer").
                                                startObject(AUTOCOMPLETE_ANALYZER_NAME).
                                                    field("type", "custom").
                                                    field("tokenizer", "standard").
                                                    field("filter", new String[]{"lowercase","autocomplete_filter"}).
                                                endObject().
                                                startObject(CUSTOM_ANALYZER_NAME).
                                                    field("type", "custom").
                                                    field("tokenizer", "standard").
                                                    field("char_filter", "html_strip").
                                                    field("filter", new String[]{"lowercase","standard", "bulgarian_stop" , "english_stop"}).
                                                endObject().
                                            endObject().
                                        endObject().endObject().string();
            settingsBuilder.loadFromSource(customAnalyzer);
        } catch (IOException e) {
            sLog.error(e.getMessage(), e);
        }

        createIndexBuilder.setSettings(settingsBuilder.build());

        // add mappings
        XContentBuilder mappingBuilder;
        try {
            mappingBuilder = jsonBuilder().startObject().
                                        startObject(TYPE_NAME).
                                            startObject("_source").
                                                field("store", false).
                                            endObject().
                                            startObject("properties").
                                                startObject(DOCUMENT_CONTENT_PROPERTY).
                                                    field("type", "string").
                                                    field("analyzer", CUSTOM_ANALYZER_NAME).
                                                    field("term_vector", "with_positions_offsets_payloads").
                                                    field("store", true).
                                                endObject().
                                                startObject(DOCUMENT_ID_PROPERTY).
                                                    field("type", "string").
                                                    field("index", "not_analyzed").
                                                    field("store", true).
                                                endObject().
                                                startObject(DOCUMENT_TITLE_PROPERTY).
                                                    field("type", "string").
                                                    field("index_analyzer", AUTOCOMPLETE_ANALYZER_NAME).
                                                    field("search_analyzer", CUSTOM_ANALYZER_NAME).
                                                    field("store", true).
                                                endObject().
                                                startObject(DOCUMENT_SUMMARY_PROPERTY).
                                                    field("type", "string").
                                                    field("index", "not_analyzed").
                                                    field("store", true).
                                            endObject().
                                            endObject().
                                        endObject().endObject();
            createIndexBuilder.addMapping(TYPE_NAME, mappingBuilder);
        } catch (IOException e) {
            sLog.error(e.getMessage(), e);
        }

        CreateIndexResponse create = createIndexBuilder.execute().actionGet();
        if (!create.isAcknowledged()) {
            sLog.error("Index wasn't created");
        } else {
            sLog.info(String.format("Index with name %s created.", INDEX_NAME));
        }
    }

    @Override
    public void indexDocument(String content, String documentID, String title) {
        // index document
        Map<String, Object> json = new HashMap<String, Object>();
        json.put(DOCUMENT_CONTENT_PROPERTY, content);
        json.put(DOCUMENT_ID_PROPERTY, documentID);
        json.put(DOCUMENT_TITLE_PROPERTY, title);
        json.put(DOCUMENT_SUMMARY_PROPERTY, "temp summary");

        IndexResponse response = mClient.prepareIndex(INDEX_NAME, TYPE_NAME).setSource(json).execute().actionGet();
        String summary = generateSummary(response.getId(), content);

        json.clear();
        json.put(DOCUMENT_SUMMARY_PROPERTY, summary);
        UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, TYPE_NAME, response.getId()).doc(json);

        mClient.update(updateRequest);
        if (sLog.isDebugEnabled()) {
            sLog.info(String.format("index: %s, type: %s, id: %s, version: %s URL:%s ", response.getIndex(),
                    response.getType(), response.getId(), response.getVersion(), documentID));
        }
    }

    @Override
    public String generateSummary(String docID, String originalContent) {
        boolean ready = false;
        StringBuilder result = new StringBuilder();
        while (!ready) {
            // retrieve only payload and frequency
            TermVectorResponse termResponse = mClient.prepareTermVector(INDEX_NAME, TYPE_NAME, docID)
                    .setPositions(false).setOffsets(false).setPayloads(true).execute().actionGet();
            ready = termResponse.isExists();
            if (ready) {
                try {
                    Map<String, Long> termScores = new HashMap<String, Long>();

                    // extract terms frequences
                    TermsEnum terms = termResponse.getFields().terms(DOCUMENT_CONTENT_PROPERTY).iterator(null);
                    while (terms.next() != null) {
                        DocsAndPositionsEnum esDocsPosEnum = terms.docsAndPositions(null, null, 0);
                        termScores.put(terms.term().utf8ToString(), (long) esDocsPosEnum.freq());
                    }

                    // get sentences
                    Map<String, Double> sentences = new HashMap<String, Double>();
                    originalContent = originalContent.replaceAll("\\s+", " ");
                    StringTokenizer sentenceTokenizer = new StringTokenizer(originalContent, ".");
                    while (sentenceTokenizer.hasMoreTokens()) {
                        double sentenceScore = 0;
                        double numTokens = 0;

                        String sentence = sentenceTokenizer.nextToken();
                        StringTokenizer wordsTokenizer = new StringTokenizer(sentence, " ");
                        while (wordsTokenizer.hasMoreTokens()) {
                            String word = wordsTokenizer.nextToken().trim();
                            if (word.isEmpty()) {
                                continue;
                            }
                            Long score = termScores.get(word.toLowerCase());
                            numTokens++;
                            if (score != null) {
                                sentenceScore += score;
                            }
                        }
                        // normalize by sentence length
                        sentenceScore = sentenceScore / numTokens;
                        if (sentence.length() < MAX_SENTENCE_LENGTH && numTokens > 3) {
                            sentences.put(sentence.trim(), sentenceScore);
                        }
                    }
                    // get bes score 3 sentences
                    sentences = sortByValues(sentences);
                    Iterator<String> sentencesIterator = sentences.keySet().iterator();
                    int summaryLength = 3;// num sentences

                    while (sentencesIterator.hasNext() && summaryLength > 0) {
                        summaryLength--;
                        String sentence = sentencesIterator.next();
                        result.append(sentence);
                        result.append(". ");
                        if (sLog.isDebugEnabled()) {
                            sLog.debug(String.format("Sentence: %s, Score: %s", sentence, sentences.get(sentence)));
                        }
                    }
                } catch (IOException e) {
                    sLog.error(e.getMessage(), e);
                    result.append(e.getMessage());
                }
            } else {
                // wait before executing to make sure the file is indexed
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    sLog.error(e.getMessage(), e);
                }
                if (sLog.isDebugEnabled()) {
                    sLog.debug(String.format("Wait indexing document with id: %s", docID));
                }
            }
        }
        return result.toString();

    }

    /**
     * Sort map by its values.
     * 
     * @param map - map to sort.
     * @return Sorted map.
     */
    private static HashMap<String, Double> sortByValues(Map<String, Double> map) {
        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o1.getValue().compareTo(o2.getValue()) * -1;// top values first
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap<String, Double> sortedHashMap = new LinkedHashMap<String, Double>();
        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    /**
     * Analyze given text using custom analyzer.
     * 
     * @param textToAnalyze - text to analyze.
     */
    @SuppressWarnings("unused")
    private void testAnalyzer(String textToAnalyze) {
        sLog.info("Analyzing: " + textToAnalyze);
        AnalyzeResponse response = mClient.admin().indices().prepareAnalyze(INDEX_NAME, textToAnalyze)
                .setAnalyzer(CUSTOM_ANALYZER_NAME).get();
        for (AnalyzeToken token : response.getTokens()) {
            sLog.info(String.format("token: %s, type: %s, position: %s, start:%s, end: %s.", token.getTerm(),
                    token.getType(), token.getPosition(), token.getStartOffset(), token.getEndOffset()));
        }
    }

    @Override
    public SearchResponse searchFreeText(String query, int from, int size) {
        SearchResponse response = mClient.prepareSearch(INDEX_NAME).setTypes(TYPE_NAME)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(DOCUMENT_CONTENT_PROPERTY, query)) // must match the body
                                .should(QueryBuilders.matchQuery(DOCUMENT_TITLE_PROPERTY, query).boost(3))// boost if matching in title
                                .minimumNumberShouldMatch(1)) 
                .setFrom(from).setSize(size) // set size as default is 10
                .addHighlightedField(DOCUMENT_CONTENT_PROPERTY, 100, 100) // highlight only content, as title will be displayed anyway.
                .setHighlighterPreTags("<strong>").setHighlighterPostTags("</strong>").execute().actionGet();

        if (sLog.isDebugEnabled()) {
            sLog.debug(String.format("Search take %s milliseconds. Total hits %s", response.getTookInMillis(), response
                    .getHits().getTotalHits()));
            for (SearchHit hit : response.getHits()) {
                sLog.debug(String.format("Matched: id:%s score:%s File:%s", hit.getId(), hit.getScore(), hit
                        .getSource().get(DOCUMENT_ID_PROPERTY)));
            }
        }
        return response;
    }

    @Override
    public SearchResponse searchFreeTextAndPhrase(String freeTextQuery, List<String> phrasesQuery, int from, int size) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        for (String phrase : phrasesQuery) {
            builder.must(QueryBuilders.matchPhraseQuery(DOCUMENT_CONTENT_PROPERTY, phrase));// must match phrase
        }
        builder.should(QueryBuilders.matchQuery(DOCUMENT_TITLE_PROPERTY, freeTextQuery)); // boost if matching free text
        builder.should(QueryBuilders.matchQuery(DOCUMENT_CONTENT_PROPERTY, freeTextQuery)); // boost if matching free text
        SearchResponse response = mClient.prepareSearch(INDEX_NAME).setTypes(TYPE_NAME)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(builder).setFrom(from).setSize(size)// set size as default is 10
                .addHighlightedField(DOCUMENT_CONTENT_PROPERTY, 100, 100) // highlight only content, as title will be  displayed anyway.
                .setHighlighterPreTags("<strong>").setHighlighterPostTags("</strong>").execute().actionGet();

        if (sLog.isDebugEnabled()) {
            sLog.debug(String.format("Search take %s milliseconds. Total hits %s", response.getTookInMillis(), response
                    .getHits().getTotalHits()));
            for (SearchHit hit : response.getHits()) {
                sLog.debug(String.format("Matched: id:%s score:%s File:%s", hit.getId(), hit.getScore(), hit
                        .getSource().get(DOCUMENT_ID_PROPERTY)));
            }
        }

        return response;
    }

    @Override
    public SearchResponse suggest(String query) {
        SearchResponse response = mClient
                .prepareSearch(INDEX_NAME)
                .setTypes(TYPE_NAME)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(
                        QueryBuilders.boolQuery()
                                .should(QueryBuilders.matchPhrasePrefixQuery(DOCUMENT_TITLE_PROPERTY, query)) // should  phrase match the Title
                                .should(QueryBuilders.matchQuery(DOCUMENT_TITLE_PROPERTY, query))) // should match in title
                .addHighlightedField(DOCUMENT_TITLE_PROPERTY) // highlight
                .setHighlighterPreTags("<strong>").setHighlighterPostTags("</strong>").execute().actionGet();
        if (sLog.isDebugEnabled()) {
            sLog.debug(String.format("suggest take %s milliseconds. Total hits %s", response.getTookInMillis(),
                    response.getHits().getTotalHits()));
            for (SearchHit hit : response.getHits()) {
                sLog.debug(String.format("Matched: id:%s score:%s Suggest:%s", hit.getId(), hit.getScore(), hit
                        .getSource().get(DOCUMENT_TITLE_PROPERTY)));
            }
        }
        return response;
    }

    @Override
    public void close() {
        stopNode();
    }

    @Override
    public void initialize() {
        // do nothing. The manager is initialized during creation.
    }
}
