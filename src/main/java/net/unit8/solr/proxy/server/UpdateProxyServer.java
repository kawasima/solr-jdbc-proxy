package net.unit8.solr.proxy.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
public class UpdateProxyServer {
    public static void main(String[] args) throws Exception {
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
