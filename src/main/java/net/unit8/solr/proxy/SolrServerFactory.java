package net.unit8.solr.proxy;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 12:49
 * To change this template use File | Settings | File Templates.
 */
public class SolrServerFactory {
    private static SolrServer solrServer;

    protected SolrServerFactory() {
    }

    public static void init() {
        System.setProperty("solr.solr.home", "src/main/resources");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        solrServer = new EmbeddedSolrServer(coreContainer, coreContainer.getDefaultCoreName());
    }

    public static SolrServer getSolrServer() {
        return solrServer;
    }
}
