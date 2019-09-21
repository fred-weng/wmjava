//<editor-fold>
package j.u;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.net.HttpURLConnection;

/**
 * Http Util Class
 *
 * @author lufax Fred WengMj
 */
public class HttpU {

    public static String read(String url) {
        return new Http(url).read();
    }

    public static String post(String url, String d) {
        return new Http(url).post(d);
    }

    public static String read(String url, Map headers) {
        Http h = new Http(url);
        h.setProperties(headers);
        return h.read();
    }

    public static String post(String url, String d, Map headers) {
        Http h = new Http(url);
        h.setProperties(headers);
        return h.post(d);
    }

    public static String read(String url, int connTimeout, int readTimeout, Map headers, String charsetName) {
        Http h = new Http(url);
        h.setConnTimeout(connTimeout);
        h.setReadTimeout(readTimeout);
        h.setCharsetName(charsetName);
        h.setProperties(headers);
        return h.read();
    }

    public static String post(String url, String d, int connTimeout, int readTimeout, Map headers, String charsetName) {
        Http h = new Http(url);
        h.setConnTimeout(connTimeout);
        h.setReadTimeout(readTimeout);
        h.setCharsetName(charsetName);
        h.setProperties(headers);
        return h.post(d);
    }
}

//<editor-fold defaultstate="collapsed" desc="Http No Static Class">
class Http {

    private int connTimeout = -1;
    private int readTimeout = -1;
    private String url = null;
    private String charsetName = "utf-8";
    private Map props = null;

    public void setProperties(Map properties) {
        this.props = properties;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setCharsetName(String charsetName) {
        if(charsetName==null || charsetName.equals(""))
            this.charsetName = "utf-8";
        else
            this.charsetName = charsetName;
    }

    public Http(String url) {
        this.url = url;
    }

    public String read() {
        HttpURLConnection conn = getConnection();
        if (null == conn)
            return null;

        try {
            String s = FileU.readEnd(new InputStreamReader(conn.getInputStream(), charsetName));
            conn.disconnect();
            return s;
        } catch (IOException e) {
            LogU.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public String post(String d) {

        HttpURLConnection conn = getConnection();

        if (null == conn)
            return null;

        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charsetName);

        try {
            conn.setRequestMethod("POST");
            if (d != null && !d.isEmpty())
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), charsetName))) {
                    writer.write(d);
                }

            String s = FileU.readEnd(new InputStreamReader(conn.getInputStream(), charsetName));
            conn.disconnect();
            return s;

        } catch (FileNotFoundException e) {
            String s = StrU.fastFormat("HTTP Status 404! The requested resource '{0}' is not available.",e.getMessage());
            LogU.error(s);
            throw new RuntimeException(s);
        } catch (IOException e) {
            LogU.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            conn.disconnect();
        }
    }

    @SuppressWarnings("unchecked")
    private HttpURLConnection getConnection() {

        HttpURLConnection conn = HttpConnFty.getConnection(url);

        if (null == conn)
            return null;

        if (connTimeout > 0)
            conn.setConnectTimeout(connTimeout);

        if (readTimeout > 0)
            conn.setReadTimeout(readTimeout);

        conn.setDoOutput(true);

        if (null != props && !props.isEmpty())
            props.keySet().stream().forEach((k) -> {
                conn.setRequestProperty((String) k, (String) props.get(k));
            });

        return conn;
    }

}
//</editor-fold>

/**
 * HttpURLConnection Class Factory Prepare for get HttpConnection from Pool
 * future
 *
 * @author lufax
 */
class HttpConnFty {

    public static HttpURLConnection getConnection(String url) {
        try {
            return (HttpURLConnection) new URL(url).openConnection();

        } catch (IOException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }
}
//</editor-fold>
