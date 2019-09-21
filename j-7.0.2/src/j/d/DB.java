//<editor-fold desc="DB">

package j.d;

import j.m.*;
import j.u.*;
import j.Conf;
import java.sql.*;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DB {

    //<editor-fold defaultstate="collapsed" desc="open-close">
    protected Connection conn = null;
    protected PreparedStatement stmt = null;

    protected abstract Connection getConnection();

    public void open() {
        if (conn == null || isClosed())
            conn = getConnection();
    }

    public void close() {
        if (null == conn || isClosed())
            return;
        try {
            conn.close();
        } catch (SQLException e) {
            LogU.error(e.getMessage());
        }
    }

    private boolean isClosed() {
        try {
            return conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="prepare-statement">
    public void prepareStatement(String sql) {
        if (conn == null)
            return;

        try {
            stmt = conn.prepareStatement(sql);
        } catch (SQLException e) {
            LogU.error(e.getMessage());
        }
    }

    public void prepareGKStatement(String sql) {
        if (conn == null)
            return;
        try {
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException e) {
            LogU.error(e.getMessage());
        }
    }

    public void prepareIDsStatement(String sql) {
        s = sql;
        c = 0;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="set-parameter">
    public int setArray(Object[] ids) {
        return setArray(1, ids);
    }

    public int setArray(int j, Object[] ids) {
        int n = ids.length;
        if (n != c) {
            StringBuilder sb = new StringBuilder("(?");
            for (int i = 1; i < n; i++)
                sb.append(",?");
            sb.append(")");
            prepareStatement(StrU.fastFormat(s, sb.toString()));
            c = n;
        }
        return setObjects(j, ids);
    }

    public int setParameters(int i, Object... p) {
        return setObjects(i, p);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="query-execute">
    @SuppressWarnings("unchecked")
    public XList<XMap> queryRows() {

        if (stmt == null)
            return null;

        XList<XMap> list = new XList<>();
        try {
            ResultSet r = stmt.executeQuery();
            ResultSetMetaData m = r.getMetaData();

            int j = m.getColumnCount();
            String[] k = new String[j];
            for (int i = 0; i < j; i++)
                k[i] = m.getColumnLabel(i + 1);

            while (r.next()) {

                XMap row = new XMap();
                for (int i = 0; i < j; i++)
                    row.put(k[i], r.getObject(i + 1));

                list.add(row);
            }
        } catch (SQLException e) {
            LogU.error(e.getMessage());
            return null;
        }

        return list;
    }

    @SuppressWarnings("unchecked")
    public XMap querySingleRow() {

        if (stmt == null)
            return null;

        XMap map = new XMap();
        try {
            ResultSet r = stmt.executeQuery();

            if (r.next()) {
                ResultSetMetaData m = r.getMetaData();
                int j = m.getColumnCount();
                for (int i = 0; i < j; i++)
                    map.put(m.getColumnLabel(i + 1), r.getObject(i + 1));
            }
        } catch (SQLException e) {
            LogU.error(e.getMessage());
            return null;
        }

        return map;
    }

    public Object queryScalar() {
        if (stmt == null)
            return null;

        try {
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getObject(1);

            return new Object();
        } catch (SQLException e) {
            return null;
        }
    }

    public int quietExecute() {
        if (stmt == null)
            return -1;

        try {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LogU.error(e.getMessage());
            return -e.getErrorCode();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="transcation">
    public void beginTrans() throws SQLException {
        if (conn != null)
            conn.setAutoCommit(false);
        LogU.info("begin transcation!");
    }

    public int execute() throws SQLException {
        if (stmt == null)
            return -1;
        return stmt.executeUpdate();
    }

    public void commit() throws SQLException {
        if (conn != null) {
            conn.commit();
            conn.setAutoCommit(true);
        }
        LogU.info("transcation commit!");
    }

    public void rollback() {
        if (conn == null)
            return;

        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            LogU.error(e.getMessage());
        }
        LogU.warning("transcation rollback!");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="simple-query-execute">
    public XList<XMap> queryRows(String sql, Object... paras) {
        open();
        prepareStatement(sql);
        setObjects(1, paras);
        XList<XMap> list = queryRows();
        close();
        return list;
    }

    public XMap querySingleRow(String sql, Object... paras) {
        open();
        prepareStatement(sql);
        setObjects(1, paras);
        XMap row = querySingleRow();
        close();
        return row;
    }

    public Object queryScalar(String sql, Object... paras) {
        open();
        prepareStatement(sql);
        setObjects(1, paras);
        Object ret = queryScalar();
        close();
        return ret;
    }

    public int quietExecute(String sql, Object... paras) {
        open();
        prepareStatement(sql);
        setObjects(1, paras);
        int ret = quietExecute();
        close();
        return ret;
    }

    public XList<XMap> queryRowsInIDs(String sql, Object[] ids) {
        open();
        prepareIDsStatement(sql);
        setArray(ids);
        XList<XMap> list = queryRows();
        close();
        return list;
    }

    public XList<XMap> queryRowsInIDs(String sql, Object[] ids, Object... paras) {
        open();
        prepareIDsStatement(sql);
        setParameters(setArray(ids), paras);
        XList<XMap> list = queryRows();
        close();
        return list;
    }

    public int quietExecuteInIDs(String sql, Object[] ids) {
        open();
        prepareIDsStatement(sql);
        setArray(ids);
        int ret = quietExecute();
        close();
        return ret;
    }

    public int quietExecuteInIDs(String sql, Object[] ids, Object... paras) {
        open();
        prepareIDsStatement(sql);
        setParameters(setArray(ids), paras);
        int ret = quietExecute();
        close();
        return ret;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private">
    private String s = null;
    private int c = 0;

    protected int setObjects(int i, Object[] ps) {
        if (stmt == null)
            return i;

        try {
            for (Object p : ps)
                stmt.setObject(i++, p);
        } catch (SQLException e) {
            LogU.error(e.getMessage());
        }
        return i;
    }
    //</editor-fold>

    protected static class CP {
       /**
        * Connection Pool Manager
        */
        private static final ConcurrentHashMap<String, DataSource> DS_MAP = new ConcurrentHashMap<String, DataSource>();
        
        public static Connection getRawConnection(String name) {
            if (DS_CONF == null)
                return null;
            try {
                XMap p = DS_CONF.getXMap(name).getXMap("properties");
                Class.forName(p.getString("driverClassName"));
                return DriverManager.getConnection(p.getString("url"), p.getString("username"), p.getString("password"));
            } catch (ClassNotFoundException | SQLException e) {
                LogU.error(e);
                return null;
            }
        }

        public static Connection getPoolConnection(String name) {
            DataSource ds = getDataSource(name);

            if (ds == null)
                return null;

            try {
                return ds.getConnection();
            } catch (SQLException e) {
                LogU.error(e);
                return null;
            }
        }

        private static DataSource getDataSource(String name) {

            if (DS_MAP.containsKey(name))
                return DS_MAP.get(name);

            DataSource ds;

            try {
                ds = newDataSource(name);
            } catch (Exception e) {
                LogU.error(e.getMessage());
                return null;
            }

            if (ds != null)
                DS_MAP.put(name, ds);
            
            return ds;
        }

        private static final XMap DS_CONF = Conf.getDefault().pathXMap("{d}{dbcps}");

        private static DataSource newDataSource(String name) throws Exception {

            XMap c = DS_CONF.getXMap(name);
            XMap p = c.getXMap("properties");
            Class clazz = Class.forName(c.getString("class"));
            
            @SuppressWarnings("unchecked")
            DataSource ds = (DataSource) clazz.getDeclaredConstructor().newInstance();// clazz.newInstance() is deprecated since jdk9
            
            String g;
            Method[] ms = clazz.getMethods();
            for (Object k : p.keySet()) {
                g = "set".concat(StrU.firstCharUpper((String) k));
                for (Method m : ms)
                    if (m.getName().equals(g)) {
                        dsSet(ds, m, p.get(k));
                        break;
                    }
            }
            LogU.info(StrU.fastFormat("datasource {0} instance created.", name));
            return ds;
        }

        /**
         * datasorce config set
         *
         * @param d dataSource instance
         * @param m set method
         * @param v config value
         * @return void
         * @throws ReflectiveOperationException
         */
        private static Object dsSet(DataSource d, Method m, Object v) throws ReflectiveOperationException {
            switch (m.getParameterTypes()[0].getName()) {
                case "int":
                    return ObjU.cal(d, m, ObjU.toInt(v));
                case "long":
                    return ObjU.cal(d, m, ObjU.toLong(v));
                case "boolean":
                    return ObjU.cal(d, m, ObjU.toBool(v));
                default:
                    return ObjU.cal(d, m, ObjU.toString(v));
            }
        }

    }

}
//</editor-fold>
