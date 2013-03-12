package net.unit8.solr.proxy.server;

import org.apache.solr.client.solrj.SolrServer;

/**
 * @author kawasima
 */
public class SolrServerFactory {
    private static SolrServer solrServer;

    protected SolrServerFactory() {
    }

    public static void init() {
    }

    public static SolrServer getSolrServer() {
        return solrServer;
    }

    public static void setSolrServer(SolrServer solrServer) {
        SolrServerFactory.solrServer = solrServer;
    }
}
