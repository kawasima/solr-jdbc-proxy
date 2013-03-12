package net.unit8.solr.proxy.server;

import net.unit8.solr.jdbc.SolrDriver;
import org.apache.commons.cli.*;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.seasar.util.sql.DriverManagerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.sql.DriverManager;

/**
 * The proxy server for solr update request.
 *
 * @author kawasima
 */
public class UpdateProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(UpdateProxyServer.class);

    public static Options createOptions() {
        Options options = new Options();
        options.addOption("u", "url", true, "The url of remote solr server");
        return options;
    }
    public static void main(String[] args) throws Exception {
        Options options = createOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine commandLine;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            logger.error("Can't parse arguments", e);
            return;
        }

        if (commandLine.hasOption("url")) {
            String url = commandLine.getOptionValue("url");
            SolrServerFactory.setSolrServer(new HttpSolrServer(url));
        } else {
            DriverManagerUtil.registerDriver(SolrDriver.class.getName());
            DriverManager.getConnection("jdbc:solr:s");
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer coreContainer = initializer.initialize();
            SolrServerFactory.setSolrServer(new EmbeddedSolrServer(coreContainer, coreContainer.getDefaultCoreName()));
        }

        Connector connector = new SelectChannelConnector();
        connector.setPort(6687);
        SolrServerFactory.init();
        Server server = new Server();
        server.setConnectors(new Connector[]{ connector });
        WebSocketHandler webSocketHandler = new WebSocketHandler() {
            @Override
            public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
                return new SolrUpdateSocket();
            }
        };
        server.setHandler(webSocketHandler);

        server.start();
    }
}
