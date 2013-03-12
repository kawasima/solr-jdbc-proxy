package net.unit8.solr.proxy.server;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.util.NamedList;
import org.eclipse.jetty.websocket.WebSocket;
import org.seasar.util.io.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kawasima
 */
public class SolrUpdateSocket implements WebSocket.OnBinaryMessage {
    private static final Logger logger = LoggerFactory.getLogger(SolrUpdateSocket.class);
    private List<UpdateRequest> updateRequestList = new ArrayList<UpdateRequest>();
    private Connection connection;

    @Override
    public void onOpen(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onClose(int closeCode, String message) {
        this.connection = null;
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        SolrServer solrServer = SolrServerFactory.getSolrServer();
        byte[] buf = new byte[length];
        System.arraycopy(data, offset, buf, 0, length);
        UpdateRequest request = (UpdateRequest) SerializeUtil.fromBinaryToObject(buf);
        updateRequestList.add(request);

        if (request.getAction() == UpdateRequest.ACTION.COMMIT) {
            try {
                NamedList<Object> response = null;
                synchronized (SolrUpdateSocket.class) {
                    long t1 = System.currentTimeMillis();
                    for (UpdateRequest req : updateRequestList) {
                        response = solrServer.request(req);
                    }
                    logger.info("Batch updated {} documents {}ms", updateRequestList.size()-1, System.currentTimeMillis() - t1);
                }
                updateRequestList.clear();
                byte[] res = SerializeUtil.fromObjectToBinary(response);
                connection.sendMessage(res, 0, res.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
