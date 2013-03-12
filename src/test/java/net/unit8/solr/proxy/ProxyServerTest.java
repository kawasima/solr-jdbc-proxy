package net.unit8.solr.proxy;

import net.unit8.solr.jdbc.ConnectionTypeDetector;
import net.unit8.solr.jdbc.SolrDriver;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.*;
import org.seasar.util.sql.StatementUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Test for UpdateProxyServer.
 *
 * @author kawasima
 */
public class ProxyServerTest {
    private Connection conn;
    private static final String REMOTE_SOLR_URL = "jdbc:solr:proxy:http://localhost:8983/solr";
    private static final String LOCAL_SOLR_URL  = "jdbc:solr:s";

    @BeforeClass
    public static void before() throws Exception {
        Class.forName(SolrDriver.class.getName());
        ConnectionTypeDetector.getInstance().add(SolrProxyConnection.class);
        SolrProxyConnection.setProxy("ws://localhost:6687/");
    }

    @Before
    public void setup() throws Exception {
        conn = DriverManager.getConnection(LOCAL_SOLR_URL);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("CREATE TABLE users (id integer, name varchar)");
            stmt.execute();
        } finally {
            StatementUtil.close(stmt);
        }
    }

    @Test
    public void test() throws Exception {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("INSERT INTO users (id, name) values (?, ?)");
            stmt.setInt(1, RandomUtils.nextInt(Integer.MAX_VALUE));
            stmt.setString(2, RandomStringUtils.randomAlphanumeric(50));
            stmt.execute();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            StatementUtil.close(stmt);
        }

        // Assertion
        try {
            stmt = conn.prepareStatement("SELECT count(*) AS cnt FROM users");
            ResultSet rs = stmt.executeQuery();
            Assert.assertTrue("Has results", rs.next());
            int cnt = rs.getInt("cnt");
            Assert.assertEquals("count = 1", 1, cnt);
        } finally {
            StatementUtil.close(stmt);
        }

    }

    @After
    public void tearDown() throws SQLException {
        if (conn != null) {
            PreparedStatement stmt = null;
            try {
                stmt = conn.prepareStatement("DROP TABLE users");
                stmt.execute();
            } finally {
                StatementUtil.close(stmt);
            }

            conn.close();
        }
    }

    public void testRemoteMultiThread() throws Exception {
        Callable<Integer> task = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Connection conn = DriverManager.getConnection("jdbc:solr:proxy:http://localhost:8983/solr");
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement("INSERT INTO users (id, name) values (?,?)");
                    for (int i=0; i < 10; i++) {
                        stmt.setInt(1, RandomUtils.nextInt(Integer.MAX_VALUE));
                        stmt.setString(2, RandomStringUtils.randomAlphanumeric(50));
                        stmt.execute();
                        long t1 = System.currentTimeMillis();
                        conn.commit();
                        long t2 = System.currentTimeMillis();
                        System.err.println(t2-t1);
                    }
                } finally {
                    StatementUtil.close(stmt);
                    if (conn != null)
                        conn.close();
                }
                return 1;
            }
        };
        int threadCnt = 60;
        List<Callable<Integer>> tasks = Collections.nCopies(threadCnt, task);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);
        List<Future<Integer>> futures = executorService.invokeAll(tasks);
        List<Integer> resultList = new ArrayList<Integer>(futures.size());
        for (Future<Integer> future : futures) {
            resultList.add(future.get());
        }
        Assert.assertEquals(futures.size(), threadCnt);
    }
}
