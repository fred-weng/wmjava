//<editor-fold>
/**
 * annotation current class is a web service
 *
 * @author mingjun
 */
package j.w;

import j.Env;
import j.Prop;
import j.m.XList;
import j.u.StrU;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WS {

    public String value() default "";

    public String desc() default "";
}

class WS_Inf {

    final String value;
    final String desc;
    final String path;
    final Class clazz;

    /**
     * WS_INFO
     *
     * @param w ws
     * @param c the class which annotationed @ws
     * @param p CTX_PATH
     */
    WS_Inf(WS w, Class c) {

        webServ = w;
        clazz = c;
        value = getWSValue(w, c);
        desc = webServ.desc();
        path = value + "/";
    }

    String html;

    void wsHTML() {

        html = HTML.build(
                () -> value, //title
                () -> fmt(TOPIC, "/", value),//for root topic:<a href="{0}">{1}</a>
                () -> desc, //class comments
                () -> {
                    StringBuilder s = new StringBuilder(WS_CLIENT)
                            .append(fmt(INTERFACE, clazz.getSimpleName()))
                            .append(" {");

                    list.forEach(v -> {
                        s.append("<br/><br/>")
                                .append(!v.value.equals(v.mS()) ? fmt(WM_CLIENT, v.value) : "")
                                .append(fmt(RETURN_TYPE, v.rTS()))
                                .append(fmt(v.canGet() ? LINK_GET : LINK_POST, path, v.value, v.mS(), v.pS()))
                                .append(!v.desc.isEmpty() ? fmt(COMMENT, v.desc) : "");
                    });
                    s.append("<br/>}");
                    return s.toString();
                }
        );
    }

    void add(Wm_Inf wmi) {
        //for WSDispatcher.loadWSI build on ws method html wsHTML must execute after add list
        list.add(wmi);
    }

    //<editor-fold defaultstate="collapsed" desc="private">
    //@WebService annotation
    private final WS webServ;

    private static String fmt(String format, Object... args) {
        return StrU.fastFormat(format, args);
    }

    private static final Prop WS_PROP = new Prop("META-INF/ws.properties");
    private static final String TOPIC = WS_PROP.getString("TOPIC");
    private static final String WS_CLIENT = WS_PROP.getString("WS_CLIENT");
    private static final String INTERFACE = WS_PROP.getString("INTERFACE");
    private static final String WM_CLIENT = WS_PROP.getString("WM_CLIENT");
    private static final String RETURN_TYPE = WS_PROP.getString("RETURN_TYPE");
    private static final String COMMENT = WS_PROP.getString("COMMENT");
    private static final String LINK_GET = WS_PROP.getString("LINK_GET");
    private static final String LINK_POST = WS_PROP.getString("LINK_POST");
    private final XList<Wm_Inf> list = new XList<>();
    //private final String ctxPath; // /hello-app

    private static String getWSValue(WS ws, Class z) {

        String v = ws.value();
        if (v.equals("") || v.equals("/"))
            return "/" + z.getName().replace('.', '/');
        if (!v.startsWith("/"))
            return "/" + v;
        return v;
    }
    //</editor-fold>
}

class HTML {

    private static final String INDEX = Env.getResourceAsString("META-INF/ws.html");

    static interface IHTML {

        String getHTML();
    }

    static String build(IHTML... ihtml) {
        int n = ihtml.length;
        String[] ss = new String[n];
        for (int i = 0; i < n; i++)
            ss[i] = ihtml[i].getHTML();
        return StrU.fastFormat4(INDEX, "<#", "#>", ss);
    }
}
//</editor-fold>
