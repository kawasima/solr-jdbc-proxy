package net.unit8.solr.proxy.server;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.util.NamedList;
import org.eclipse.jetty.websocket.WebSocket;
import org.seasar.util.io.SerializeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 12:40
 * To change this template use File | Settings | File Templates.
 */
public class SolrUpdateSocket implements WebSocket.OnBinaryMessage {
    private List<UpdateRequest> updateRequestList = new ArrayList<UpdateRequest>();
    private Connection connection;

    @Override
    public void onOpen(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onClose(int closeCode, String message) {
        //To change body of implemented methods use File | Settings | File Templates.
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
                    for (UpdateRequest req : updateRequestList) {
                        response = solrServer.request(request);
                    }
                }
                byte[] res = SerializeUtil.fromObjectToBinary(response);
                connection.sendMessage(res, 0, res.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
