package net.unit8.solr.proxy.client;

import net.unit8.solr.jdbc.impl.SolrConnection;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.util.NamedList;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.seasar.util.io.SerializeUtil;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author kawasima
 */
public class ProxiedSolrServer extends SolrServer {
    private SolrConnection bypassConnection;
    private static WebSocketClientFactory webSocketClientFactory;
    static {
        webSocketClientFactory = new WebSocketClientFactory();
        try {
            webSocketClientFactory.start();
        } catch (Exception e) {

        }
    }

    private URI uri;
    private NamedList<Object> response;
    private WebSocket.Connection connection;

    public ProxiedSolrServer(String proxyServerUrl) {
        uri = URI.create(proxyServerUrl);
    }

    public void setBypassConnection(SolrConnection bypassConnection) {
        this.bypassConnection = bypassConnection;
    }

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
            if (((UpdateRequest) request).getAction() == UpdateRequest.ACTION.COMMIT) {
                synchronized (this) {
                    try {
                        TimeUnit.SECONDS.timedWait(this, 30);
                    } catch (InterruptedException ignore) {
                    }
                    try {
                        if (response != null)
                            return response.clone();
                    } finally {
                        response = null;
                    }
                }
            }
            return new NamedList<Object>();
        } else {
            return bypassConnection.getSolrServer().request(request);
        }
    }

    private WebSocket.Connection createConnection() throws IOException, ExecutionException, InterruptedException {
        WebSocketClient client = webSocketClientFactory.newWebSocketClient();
        final ProxiedSolrServer parent = this;
        Future<WebSocket.Connection> connectionFuture = client.open(uri, new WebSocket.OnBinaryMessage() {
            @SuppressWarnings("unchecked")
            @Override
            public void onMessage(byte[] data, int offset, int length) {
                byte[] buf = new byte[length];
                System.arraycopy(data, offset, buf, 0, length);
                response = (NamedList<Object>)SerializeUtil.fromBinaryToObject(buf);

                synchronized (parent) {
                    parent.notify();
                }
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
        connection.close();
    }
}
