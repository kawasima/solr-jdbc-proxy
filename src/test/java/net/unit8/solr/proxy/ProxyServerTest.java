package net.unit8.solr.proxy;

import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class ProxyServerTest {
    private static WebSocketClientFactory webSocketClientFactory;
    @BeforeClass
    public static void before() throws Exception {
    }
    @Test
    public void test() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:solr:proxy://localhost:6687/");

    }
}
