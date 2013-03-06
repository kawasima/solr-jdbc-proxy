package net.unit8.solr.proxy;

import org.apache.solr.client.solrj.request.UpdateRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 17:29
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRequestBatch {
    private List<UpdateRequest> updateRequestList = new ArrayList<UpdateRequest>();
    public void add(UpdateRequest updateRequest) {
        updateRequestList.add(updateRequest);
    }
}
