package net.unit8.solr.proxy;

import net.unit8.solr.jdbc.ConnectionTypeDetector;
import net.unit8.solr.jdbc.impl.SolrConnection;
import net.unit8.solr.proxy.client.ProxiedSolrServer;
import org.apache.commons.lang.StringUtils;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 16:36
 * To change this template use File | Settings | File Templates.
 */
public class SolrProxyConnection extends SolrConnection {
    private static String proxyUrl;
    public SolrProxyConnection(String serverUrl) {
        super(proxyUrl);
        serverUrl = serverUrl.replaceFirst("^proxy:", "");
        try {
            SolrConnection connection = ConnectionTypeDetector.getInstance().find(serverUrl);
            ProxiedSolrServer proxyServer = new ProxiedSolrServer(proxyUrl);
            proxyServer.setBypassConnection(connection);
            setSolrServer(proxyServer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean accept(String url) {
        return StringUtils.startsWith(url, "proxy:");
    }


    public static void setProxy(String proxyUrl) {

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
        getSolrServer().shutdown();
    }
}
