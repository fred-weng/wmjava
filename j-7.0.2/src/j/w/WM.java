//<editor-fold>
/**
 * annotation current method is a web service method.
 *
 * @author mingjun
 */
package j.w;

import j.m.IX;
import j.m.JSON;
import j.m.PType;
import j.m.XMap;
import j.m.XList;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WM {

    public String value() default "";

    public String desc() default "";
}

class Wm_Inf {

    final String value;
    final String desc;
    final int n;

    private final Method m;
    private final Type[] ts; //method parameterTypes
    private final Type rt; //method return Type

    /*
	   no use m.getGenericParameterTypes for get parameters[i] name,
	   bug normal jdk lost parameter name , return arg+i
	   remove this.ps = m.getParameters(); 
     */
    Wm_Inf(WM w, Method m) {
        this.m = m;
        ts = m.getGenericParameterTypes();
        rt = m.getGenericReturnType();
        n = ts.length;
        //fault tolerant processing for Value Settings by Programmers
        value = getValue(w, m);
        desc = w.desc();
    }

    //return String of genericReturnType
    String rTS() {
        StringBuilder s = new StringBuilder();
        typeS(rt, s);
        return s.toString();
    }

    //method string
    String mS() {
        return m.getName();
    }

    //parameters string
    String pS() {
        if (n == 0)
            return "";
        StringBuilder s = new StringBuilder();
        paraS(ts, n, s);
        return s.toString();
    }

    //for WS_Inf.wsHTML()
    boolean canGet() {
        switch (n) {
            case 0: //LINK_GET
                return true;
            case 1: //RECOMMEND_LINK
                //queryString not support generic type
                if (ts[0].getTypeName().equals(XMap.TYPE_NAME))
                    return true;
            default://LINK_POST
                return false;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="private">
    private static void paraS(Type[] ts, int n, StringBuilder s) {
        s.append("<span class='type'>");
        typeS(ts[0], s);
        s.append("</span> ");

        s.append("p0");
        for (int i = 1; i < n; i++) {
            s.append(", <span class='type'>");
            typeS(ts[i], s);
            s.append("</span> ").append("p").append(i);

            /**
             * bug:normal jdk lost parameter name , return arg+i if off javac
             * -parameters parameters[i].getName = args+i
             */
        }
    }

    public static void typeS(Type t, StringBuilder s) {
        PType.typeS(t, s, "&lt;", "&gt;", false);
    }

    /**
     * fault tolerant processing for Value Settings by Programmers
     *
     * @param w
     * @param m
     * @return
     */
    private static String getValue(WM w, Method m) {

        String v = w.value();

        //method can not endswith '/' ; endsWith '/' indicate class 
        if (v.equals("") || v.endsWith("/"))
            return m.getName();

        //class path has ends with '/'
        if (v.startsWith("/"))
            return v.substring(1);

        return v;
    }
    //</editor-fold>
}

class MI {

    /**
     * Method Info for invocation
     *
     * @author wengmj
     */
    private final Method method;
    private final Object o;
    private final int n;
    private final Type[] pTypes;

    MI(Method m, Object o) {
        method = m;
        method.setAccessible(true);
        this.o = o;
        // getGenericParameterTypes and getParameters()[i].getParameterizedType()
        // if use getParameterTypes will lost parameter type List<String> -> List
        pTypes = m.getGenericParameterTypes();
        n = pTypes.length;

    }

    Object cal(String m, String d) throws ReflectiveOperationException, IOException {
    /**
     * Read the post string from the client side, convert it to the list of
     * parameter types of the web service method, call the method.
     *
     * @param request
     * @return return object
     * @throws ReflectiveOperationException
     */
        if (n == 0)
            return method.invoke(o);

        //can use the GET method to directly call only one XMap parameter with the browser.
        // 20180818: here method.invoke can note replace by ObjU.cal. it will can not throw InvocationTargetException 
        // and the InvocationTargetException contains targetException is ValidException
        if (n == 1 && "GET".equals(m) && pTypes[0].getTypeName().equals(XMap.TYPE_NAME))
            return method.invoke(o, get(d));
        else if ("POST".equals(m))
            return method.invoke(o, post(d));
        else
            throw new IllegalArgumentException("this url only support post and rpc invocation.");

    }

    //<editor-fold defaultstate="collapsed" desc="private">
    /**
     * for getting any function post data
     *
     * @param s
     * @return
     */
    private Object[] post(String s) {

        Object r = JSON.parse(s);
        XList list = (r instanceof XList) ? (XList) r : new XList(get((String) r).values());//for post man? name=aaa&age=123 -> [aaa,123]

        Object[] args = new Object[n];
        for (int i = 0; i < n; i++)
            args[i] = IX.toType(list.get(i), pTypes[i]);

        return args;
    }

    /**
     * only invocation when get method, and can not get null value
     *
     * @param request
     * @return
     */
    private static XMap get(String s) {
        XMap r = new XMap();
        if (s != null) {
            int i;
            for (String p : s.split("&")) {
                i = p.indexOf('=');
                if (i > 0)
                    r.put(p.substring(0, i), p.substring(i + 1));
            }
        }
        return r;
    }
    //</editor-fold>
}
//</editor-fold>
