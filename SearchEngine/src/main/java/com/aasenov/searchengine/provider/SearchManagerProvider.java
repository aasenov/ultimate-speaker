package com.aasenov.searchengine.provider;

import com.aasenov.searchengine.SearchManager;
import com.aasenov.searchengine.elasticsearch.ElasticserachManager;

/**
 * Provider used to retrieve required {@link SearchManager} instances.
 */
public class SearchManagerProvider {

    /**
     * Type of parser to use.
     */
    private static SearchManagerType sEngineType = SearchManagerType.Elasticsearch;

    /**
     * Getter for the {@link SearchManagerProvider#sEngineType} property.
     * 
     * @return the {@link SearchManagerProvider#sEngineType}
     */
    public static SearchManagerType getEngineType() {
        return sEngineType;
    }

    /**
     * Setter for the {@link SearchManagerProvider#sEngineType} property
     * 
     * @param sEngineType the {@link SearchManagerProvider#sEngineType} to set
     */
    public static void setEngineType(SearchManagerType sEngineType) {
        SearchManagerProvider.sEngineType = sEngineType;
    }

    /**
     * Retrieve default {@link SearchManager} instance.
     * 
     * @return Initialized search manager object.
     */
    public static SearchManager getDefaultSearchManager() {
        switch (sEngineType) {
        case Elasticsearch:
        default:
            return ElasticserachManager.getInstance();
        }
    }

    /**
     * Destroy static instance of all managers.
     */
    public static void destroyManagers() {
        ElasticserachManager.destroy();
    }
}
