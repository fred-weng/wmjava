//<editor-fold>
/**
 *
 * @author mingjun
 *
 * Client proxy interface does not support method overloading
 * but you can change the name of the method
 * eg:
 * ws server:
 *
 * @WS
 * public class Math {
 * @WM int add(int x,int y){ return x + y; }
 *
 * @WM("add-get") int add(XMap p){ return p.getInt("x") + p.getInt("y"); } } ws
 * client:
 *
 * @WsClient("http://localhost:8080/hello-app/services/NewClass/") interface
 * Math {
 * @WmClient("add") int addx(int p0, int p1); //change method name here
 *
 * @WmClient("add-get") int add(XMap p0); }
 */
package j.w;

import j.Conf;
import j.c.Cache;
import j.m.JSON;
import j.m.XMap;
import j.u.HttpU;
import java.lang.reflect.*;
import java.util.concurrent.ConcurrentHashMap;

public class WsProxy {

    //load client proxy interface class to call remote server side restful WebService
    @SuppressWarnings("unchecked")
    public static <T> T load(Class<T> z) {
        return (T) Cache.lazy(
                z.getName(),
                M,
                () -> Proxy.newProxyInstance(
                        z.getClassLoader(),
                        new Class[]{z},
                        new InvocationHandler() {
                    WsC_Inf s = new WsC_Inf(z, (WsClient) z.getAnnotation(WsClient.class));

                    @Override
                    public Object invoke(Object o, Method m, Object[] args) throws Throwable {
                        return WsCFty.wsClient(s.server).wsMethod(s.wsPath(m.getName())).cal(s.mReType(m.getName()), args);
                    }
                }
                )
        );
    }
    //multithread write read, must use ConcurrentHashMap
    private static final ConcurrentHashMap<String, Object> M = new ConcurrentHashMap<>();
}

//<editor-fold defaultstate="collapsed" desc="WsCFty">
class WsCFty {

    private static final XMap S_MAP = Conf.getDefault().pathXMap("{w}{server}");

    static WSClient wsClient(String name) {
        return new WSClient(S_MAP.getXMap(name));
    }

    static class WSClient {

        private String root = null;
        private XMap props = null;

        WSClient(XMap props) {
            this.props = props;
            String url = props.getString("url");
            if (url != null)
                this.root = url.endsWith("/") ? url : url.concat("/");
            else
                this.root = "";
        }

        WSMethod wsMethod(String method) {
            WSMethod w = new WSMethod(this.root.concat(method.startsWith("/") ? method.substring(1) : method));
            w.setConnTimeout(props.getInt("connTimeout"));
            w.setReadTimeout(props.getInt("readTimeout"));
            w.setCharsetName(props.getString("charsetName"));
            w.setHeaders(props.getXMap("headers"));
            return w;
        }

        static class WSMethod {

            private String url = null;
            private int connTimeout = -1;
            private int readTimeout = -1;
            private XMap headers = null;
            private String charsetName = null;

            WSMethod(String url) {
                this.url = url;
            }

            void setConnTimeout(int timeout) {
                this.connTimeout = timeout;
            }

            void setReadTimeout(int timeout) {
                this.readTimeout = timeout;
            }

            void setHeaders(XMap headers) {
                this.headers = headers;
            }

            void setCharsetName(String charsetName) {
                this.charsetName = charsetName;
            }

            String get() {
                return HttpU.read(url, connTimeout, readTimeout, headers, charsetName);
            }

            String post(String s) {
                return HttpU.post(url, s, connTimeout, readTimeout, headers, charsetName);
            }

            Object cal(Type returnType, Object[] args) {
                //client args use Array 
                //server args use XList
                //for Array.toJSON == XList.toJSON
                String s = post(JSON.toJSON(args));

                if (s.startsWith("WsException:"))
                    throw new WsException(s.substring(12));
                /*
                    XResult<List<Integer>> result = JSON.parseObject("{'errorCode':'000000','errorMessage':'','data':[1,2,3,4,5]}", 
                    PType.compose(XResult.class, PType.compose(List.class, Integer.class)));
                 */
                return JSON.parseObject(s, returnType);
            }
        }
    }
}
//</editor-fold>

//</editor-fold>
