package net.unit8.solr.proxy;

import net.unit8.solr.jdbc.ConnectionTypeDetector;
import net.unit8.solr.jdbc.SolrDriver;
import org.junit.BeforeClass;
import org.junit.Test;
import org.seasar.util.sql.StatementUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: UU034251
 * Date: 13/03/05
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class ProxyServerTest {
    @BeforeClass
    public static void before() throws Exception {
        Class.forName(SolrDriver.class.getName());
        ConnectionTypeDetector.getInstance().add(SolrProxyConnection.class);
        SolrProxyConnection.setProxy("ws://localhost:6687/");
    }
    @Test
    public void test() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:solr:proxy:s;SOLR_HOME=src/test/resources");
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("CREATE TABLE users (id integer, name varchar)");
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            StatementUtil.close(stmt);
        }
    }
}
