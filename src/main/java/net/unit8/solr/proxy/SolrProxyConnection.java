package net.unit8.solr.proxy;

import net.unit8.solr.jdbc.impl.SolrConnection;
import net.unit8.solr.proxy.client.ProxySolrServer;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class SolrProxyConnection extends SolrConnection {
    public SolrProxyConnection(String serverUrl) {
        super(serverUrl);
        ProxySolrServer solrServer = new ProxySolrServer();
        setSolrServer(solrServer);
    }
    @Override
    public void setQueryTimeout(int second) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getQueryTimeout() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
