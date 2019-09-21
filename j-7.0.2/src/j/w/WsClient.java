//<editor-fold>
package j.w;

import j.u.StrU;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WsClient {

    //http://localhost:8080/hello-app
    public String server() default "";

    // /Example-WS 
    public String value();
}

//<editor-fold defaultstate="collapsed" desc="WsC_Inf">
class WsC_Inf {

    //webservice client information
    final String server;

    /**
     * @param i client interface class
     * @param an client interface class WSClient annotation setting
     */
    WsC_Inf(Class i, WsClient an) {
        if (an == null)
            throw new IllegalArgumentException(
                    StrU.fastFormat("interface: \"{0}\" must be set up annotation \"@WSC\" ", i.getName())
            );
        server = an.server();
        initMap(i, an);
    }

    String wsPath(String name) {
        return MP_MAP.get(name);
    }

    Type mReType(String name) {
        //use method fullname eg:public int add(int,int) if support method overload
        return RT_MAP.get(name);
    }

    /**
     * method name to ws path mapping single thread write and multi thread
     * read,HashMap is ok
     */
    private final HashMap<String, String> MP_MAP = new HashMap<>();

    //method name to method return type mapping
    private final HashMap<String, Type> RT_MAP = new HashMap<>();

    private void initMap(Class i, WsClient an) {

        String v = wscValue(i, an);

        /*
        method name to wsMethod path and method return type mapping.
        use method.toString get full name if support method overload
        both foo(List<String> arg> and foo(List<Integer> arg> method have same erasure
         */
        for (Method m : i.getDeclaredMethods()) {
            MP_MAP.put(m.getName(), StrU.urlCat(v, wmcValue(m.getAnnotation(WmClient.class), m)));
            RT_MAP.put(m.getName(), m.getGenericReturnType());
        }
    }

    private static String wscValue(Class i, WsClient an) {
        String v = "";
        if (an == null || (v = an.value()) == null || v.equals(""))
            return i.getSimpleName();
        return v;
    }

    private static String wmcValue(WmClient w, Method m) {
        String v = "";
        if (w == null || (v = w.value()) == null || v.equals("") || v.endsWith("/"))
            return m.getName();
        return v;
    }
}
//</editor-fold>

//</editor-fold>
