package net.unit8.solr.proxy.client;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.util.NamedList;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.seasar.util.io.SerializeUtil;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 16:40
 * To change this template use File | Settings | File Templates.
 */
public class ProxySolrServer extends SolrServer {
    private static WebSocketClientFactory webSocketClientFactory;
    static {
        webSocketClientFactory = new WebSocketClientFactory();
        webSocketClientFactory.start();
    }

    private URI uri;
    private NamedList<Object> response;
    private WebSocket.Connection connection;

    @Override
    public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException {
        if (request instanceof UpdateRequest) {
            if (connection == null) {
                try {
                    connection = createConnection();
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SolrServerException(e);
                }
            }
            byte[] serializedRequest = SerializeUtil.fromObjectToBinary(request);
            connection.sendMessage(serializedRequest, 0, serializedRequest.length);
            synchronized (this) {

            }
            return response;
        }
        return null;
    }

    private WebSocket.Connection createConnection() throws IOException, ExecutionException, InterruptedException {
        WebSocketClient client = webSocketClientFactory.newWebSocketClient();
        Future<WebSocket.Connection> connectionFuture = client.open(uri, new WebSocket.OnBinaryMessage() {
            @Override
            public void onMessage(byte[] data, int offset, int length) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onOpen(Connection connection) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onClose(int closeCode, String message) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        return connectionFuture.get();
    }
    @Override
    public void shutdown() {
    }
}
