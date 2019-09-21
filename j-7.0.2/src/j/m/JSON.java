//<editor-fold>
/**
 * JSON Parser and Serializer
 *
 * @author Fred Weng
 */
package j.m;

import j.*;
import j.u.*;
import j.i.*;
import java.util.*;
import java.lang.reflect.*;

public class JSON {

    //MAX_DEPTH default value is 5 and max set value is 7
    // 20180818: bug fixed recursion depth limit , avoid stack overflow
    // o2j invocation increase recursion level and invocaton of ej2 judge deepth
    private static final int DEPTH = Conf.getDefault().pathInt("{m}{json}{maxDepth}", 5);
    private static final int MAX_DEPTH = DEPTH > 7 ? 7 : DEPTH;

    //<editor-fold defaultstate="collapsed" desc="core function">
    /**
     * Here can only be parse json string into rawString or XMap or XList
     *
     * @param json string
     * @return XMap or XList Object If you want to parse the JSON into a pojo
     * use T parseObject(String json, Type t) please.
     */
    public static Object parse(String json) {

        char c;
        //20180804 fixed bug: parse("hello a\'b")
        if (null == json
                || "".equals(json)
                || ((c = json.charAt(0)) != '[' && c != '{')) //avoiding pure string run into lexical parser
            //2018-07-23: json is primitive string eg: "123","hello" return raw string

            return json;

        Lexer lex = new Lexer(json);

        if (!lex.next())
            return json;

        return parseO(lex);
    }

    /**
     * A very powerful method. it can convert json string to any complex type
     * including custom and nested generics type object.
     *
     * @param <T> return type
     * @param json
     * @param t Integer.class or
     * PType.compose(List.class,PType.compose(List.class,Integer.class));
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseObject(String json, Type t) {
        return (T) XMap.o2t(null, parse(json), t);
    }

    public static IX parseX(String json) {
        Object o = parse(json);
        if (o instanceof IX)
            return (IX) o;
        return null;
    }

    public static String toJSON(Object o) {
        return toJSON(o, MAX_DEPTH);
    }

    public static String toPrettyJSON(Object o) {
        return toPrettyJSON(o, MAX_DEPTH);
    }

    public static String toJSON(Object o, int deepth) {
        StringBuilder s = new StringBuilder();
        o2j(s, o, 1, false, deepth);
        return s.toString();
    }

    public static String toPrettyJSON(Object o, int deepth) {
        StringBuilder s = new StringBuilder();
        o2j(s, o, 1, true, deepth);
        return s.toString();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="parse">
    private static Object parseO(Lexer lex) {

        String v = lex.Value;

        if (lex.S)
            return v;

        switch (v) {
            case "{":
                return parseM(lex);
            case "[":
                return parseL(lex);
            case "null":
                return null;
            case "true":
                return Boolean.TRUE;
            case "false":
                return Boolean.FALSE;
            default:
                return new O(v);
        }
    }

    @SuppressWarnings("unchecked")
    private static XMap parseM(Lexer lex) {
        XMap map = new XMap();
        String k;
        while (lex.next()) {
            if ("#".equals(lex.Value))
                break;

            k = lex.Value;
            lex.next();
            lex.next();
            map.put(k, parseO(lex));
            lex.next();

            if ("#".equals(lex.Value))
                break;
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static XList parseL(Lexer lex) {
        XList list = new XList();
        while (lex.next()) {
            if ("#".equals(lex.Value))
                break;

            list.add(parseO(lex));
            lex.next();

            if ("#".equals(lex.Value))
                break;
        }
        return list;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="x2j">
    private static void o2j(StringBuilder s, Object o, int n, boolean b, int d) {

        if (o == null) {
            s.append("null");
            return;
        }

        Class z = o.getClass();
        String name = z.getName();
        switch (name) {
            case N.STRING:
                if (n == 1)//recursion deepth: the first call, differentiating if single String object to JSON

                    s.append(esc((String) o));
                else
                    s.append('"').append(esc((String) o)).append('"');
                break;

            case N.O:
            case N.INTEGER:
            case N.BOOLEAN:
            case N.LONG:
            case N.FLOAT:
            case N.DOUBLE:
            case N.BYTE:
            case N.SHORT:
            case N.BIG_INTEGER://20160822
            case N.BIG_DECIMAL:
            case N.LOCALE:
                s.append(o);
                break;

            case N.XMAP:
            case N.HASHMAP:
            case N.HASHTABLE:
            case N.PROPERTIES:
                m2j(s, (Map) o, n, b, d);
                break;

            case N.XLIST:
            case N.ARRAYLIST:
            case N.ARRAYS_ARRAYLIST:
            case N.HASHSET: //set instance is often hashset,XMap added a function l2s
            case N.LINKED_LIST:
                l2j(s, (Iterable) o, n, b, d);
                break;

            //three new function structure
            case N.FLIST:
                f2j(s, (FList<?>) o, n, b, d);
                break;
            case N.COUPLE:
                c2j(s, (Couple) o, n, b, d);
                break;
            case N.TRIAD:
                t2j(s, (Triad) o, n, b, d);
                break;

            case N.DATE:
            case N.SDATE:
            case N.XDATE://2017-12-11
            case N.TIME_STAMP:
                if (n == 1)
                    s.append(ObjU.formatDate(o));
                else
                    s.append('"').append(ObjU.formatDate(o)).append('"');
                break;

            case N.CHARACTER:
            case N.TIME:
            case N.INSTANT://2017-12-11
                if (n == 1)
                    s.append(o);
                else
                    s.append('"').append(o).append('"');
                break;

            default:
                if (name.charAt(0) == '[')//Array

                    l2j(s, aL(o, name), n, b, d);
                else
                    e2j(s, o, z, name, n, b, d);
                break;
        }
    }

    private static void e2j(StringBuilder s, Object o, Class z, String name, int n, boolean b, int d) {

        // 20180818: bug fixed recursion depth limit , avoid stack overflow
        // o2j invocation increase recursion level and invocaton of ej2 judge deepth
        if (n >= d) {
            s.append('"').append(esc(o.toString())).append('"');
            return;
        }

        MI mi = MI.getMI(z, name);
        if (mi.getM.isEmpty()) { //20180818 fixed bug : not bean object
            s.append('"').append(esc(o.toString())).append('"');
            return;
        }

        s.append('{');
        ident(s, n, b);

        boolean f = false;
        for (MI.M m : mi.getM) {
            if (f) {
                s.append(',');
                ident(s, n, b);
            }
            f = true;
            s.append('"').append(m.name).append('"').append(':');
            o2j(s, ObjU.cal(o, m.method), n + 1, b, d);
        }

        ident(s, n - 1, b);
        s.append('}');

    }

    /**
     * Object as List see also XMap.l2a 2017-12-13 Fred Weng code of the theory
     * that man is an integral part of nature
     *
     * @param o Ready to convert to a list of objects
     * @return List
     */
    private static List aL(Object o, String n) {
        //can not cast o to Object[] if o is primitive type array
        //but primitive array's self is a Object , it's array can cast to Object[]
        switch (n.charAt(1)) {
            case 'L'://object array
            case '['://multidimensional primitive type array. eg:int[][]
                return Arrays.asList((Object[]) o);
            default://1 dimensional primitive type array
                Object[] os = new Object[Array.getLength(o)];
                Arrays.setAll(os, i -> Array.get(o, i));
                return Arrays.asList(os);
        }
    }

    private static void m2j(StringBuilder s, Map o, int n, boolean m, int d) {
        s.append('{');
        ident(s, n, m);
        boolean f = false;
        for (Object k : o.keySet()) {
            if (f) {
                s.append(',');
                ident(s, n, m);
            }
            f = true;
            s.append('"').append(k).append('"').append(':');
            o2j(s, o.get(k), n + 1, m, d);
        }
        ident(s, n - 1, m);
        s.append('}');

    }

    private static void l2j(StringBuilder s, Iterable o, int n, boolean m, int d) {
        s.append('[');
        ident(s, n, m);
        boolean f = false;
        for (Object k : o) {
            if (f) {
                s.append(',');
                ident(s, n, m);
            }
            f = true;
            o2j(s, k, n + 1, m, d);
        }
        ident(s, n - 1, m);
        s.append(']');
    }

    //<editor-fold defaultstate="collapsed" desc="three new function structure">
    private static void f2j(StringBuilder s, FList<?> o, int n, boolean m, int d) {
        s.append('[');
        ident(s, n, m);
        //Deception compiler : the external variable referenced by the lambda expression must be final.
        final WrapBool wb = new WrapBool();
        o.foldRight(s,
                (x, y) -> {
                    if (wb.value) {
                        x.append(',');
                        ident(x, n, m);
                    }
                    wb.value = true;
                    o2j(x, y, n + 1, m, d);
                    return x;
                }
        );
        ident(s, n - 1, m);
        s.append(']');
    }

    private static class WrapBool {

        public boolean value = false;
    }

    private static void c2j(StringBuilder s, Couple o, int n, boolean m, int d) {
        s.append('[');

        ident(s, n, m);
        o2j(s, o._1, n + 1, m, d);
        s.append(',');

        ident(s, n, m);
        o2j(s, o._2, n + 1, m, d);

        ident(s, n - 1, m);
        s.append(']');
    }

    private static void t2j(StringBuilder s, Triad o, int n, boolean m, int d) {
        s.append('[');

        ident(s, n, m);
        o2j(s, o._1, n + 1, m, d);
        s.append(',');

        ident(s, n, m);
        o2j(s, o._2, n + 1, m, d);
        s.append(',');

        ident(s, n, m);
        o2j(s, o._3, n + 1, m, d);

        ident(s, n - 1, m);
        s.append(']');
    }
    //</editor-fold>

    /**
     * see also JSON.Lexer.unesc
     *
     * @param s
     * @return
     */
    private static String esc(String s) {
        int n = s.length();

        boolean f = false;
        _L1:
        for (int i = 0; i < n; i++)
            switch (s.charAt(i)) {
                case '"':
                case '\n':
                case '\r':
                case '\t':
                case '\\':
                    f = true;
                    break _L1;
                default:
                    break;
            }

        if (!f)
            return s;

        char c;
        StringBuilder sb = new StringBuilder(n * 2);
        for (int i = 0; i < n; i++) {
            c = s.charAt(i);
            switch (c) {
                case 92:
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * print ident space also for XML
     *
     * @param s
     * @param n Hierarchy
     * @param b do print indent space?
     */
    static void ident(StringBuilder s, int n, boolean b) {
        if (b)
            s.append(Env.LINE_SEP_OS).append(StrU.makeSpace(n * 4));
    }
    //</editor-fold>

    private static class O {

        private final String o;

        public O(String o) {
            this.o = o;
        }

        @Override
        public String toString() {
            return this.o;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.o);
        }

        /**
         * Very clear logic
         *
         * @param b
         * @return
         */
        @Override
        public boolean equals(Object b) {
            if (null == b)
                return null == o;
            return b.toString().equals(o);
        }
    }

    private static class Lexer {

        private String s = null;
        private int i = 0, j = 0, k = 0, n = 0;
        public String Value = null;
        public boolean S = false;

        public Lexer(String s) {
            this.s = s;
            n = s == null ? 0 : s.length();
        }

        /**
         * see also JSON.esc
         *
         * @param endChar
         */
        private void unesc(final char endChar) {

            j = s.indexOf(endChar, i + 1);
            Value = s.substring(i + 1, j);

            //do not need unesc
            if (Value.indexOf(92) < 0) // '\' is esc char start flag

                return;
            StringBuilder sb = new StringBuilder();
            for (i++;; i++) {
                char c = s.charAt(i);
                switch (c) {
                    case 92:
                        switch (s.charAt(i + 1)) {
                            case 92:
                                sb.append('\\');
                                i++;
                                break;
                            case '"':
                                sb.append('"');
                                i++;
                                break;
                            case 'r':
                                sb.append('\r');
                                i++;
                                break;
                            case 'n':
                                sb.append('\n');
                                i++;
                                break;
                            case 't':
                                sb.append('\t');
                                i++;
                                break;
                            case '\'':
                                sb.append('\'');
                                i++;
                                break;
                            default:
                                sb.append('\\');
                                break;
                        }
                        break;
                    case '\'':
                    case '\"':
                        if (endChar == c) {
                            j = i;
                            Value = sb.toString();
                            return;
                        }
                        sb.append(c);
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }

        public boolean next() {

            if (i == n)
                return false;

            S = false;
            char c = s.charAt(i);
            switch (c) {
                case ',':
                case ':':
                    tokenize("@");
                    break;
                case ']':
                case '}':
                    tokenize("#");
                    break;
                case '[':
                    tokenize("[");
                    break;
                case '{':
                    tokenize("{");
                    break;
                case 34:
                case 39:
                    unesc(c);
                    i = j + 1;
                    S = true;
                    break;
                case 9:
                case 10:
                case 13:
                case 32:
                    i++;
                    return next();
                default:
                    i++;
                    k = 1;
                    return next();
            }

            return true;
        }

        private void tokenize(String v) {
            if (1 == k) {
                k = 0;
                Value = s.substring(j + 1, i);
            } else {
                Value = v;
                j = i;
                i++;
            }
        }
    }
}

//<editor-fold defaultstate="collapsed" desc="class XPath">
class XPath {

    //maybe return null;
    static Object path(IX o, String path) {
        if (path == null || o == null)
            return o;

        Object r = o;
        char[] ps = path.toCharArray();
        int i, j, n = ps.length;

        for (i = 0; i < n;)
            switch (ps[i]) {
                case '[':
                    j = path.indexOf(']', i + 1);
                    if (j < 0)
                        i++;
                    else {
                        if (r instanceof List)
                            r = ((List) r).get(ObjU.toInt(path.substring(i + 1, j)));
                        else
                            return r;

                        i = j + 1;
                    }
                    break;
                case '{':
                    j = path.indexOf('}', i + 1);
                    if (j < 0)
                        i++;
                    else {
                        if (r instanceof Map)
                            r = ((Map) r).get(path.substring(i + 1, j));
                        else
                            return r;
                        i = j + 1;
                    }
                    break;
                default:
                    i++;
                    break;
            }
        return r;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="class N">
class N {

    static final String STRING = "java.lang.String";
    static final String OBJECT = "java.lang.Object";
    static final String LONG = "java.lang.Long";
    static final String BIG_INTEGER = "java.math.BigInteger";
    static final String INTEGER = "java.lang.Integer";

    static final String MAP = "java.util.Map";
    static final String XMAP = "j.m.XMap";
    static final String HASHMAP = "java.util.HashMap";
    static final String HASHTABLE = "java.util.Hashtable";

    static final String SET = "java.util.Set";
    static final String HASHSET = "java.util.HashSet";

    static final String PROPERTIES = "java.util.Properties";
    static final String ARRAYLIST = "java.util.ArrayList";

    //just for Arrays.asList(T... o)
    static final String ARRAYS_ARRAYLIST = "java.util.Arrays$ArrayList";
    //for ArrayList.subList
    static final String SUB_LIST = "java.util.ArrayList$SubList";
    static final String LIST = "java.util.List";
    static final String XLIST = "j.m.XList";
    static final String LINKED_LIST = "java.util.LinkedList";
    static final String VECTOR = "java.util.Vector";

    static final String DATE = "java.util.Date";
    static final String SDATE = "java.sql.Date";
    static final String XDATE = "j.u.XDate";//2017-12-11
    static final String INSTANT = "java.time.Instant";//2017-12-11
    static final String TIME_STAMP = "java.sql.Timestamp";
    static final String DOUBLE = "java.lang.Double";
    static final String FLOAT = "java.lang.Float";
    static final String BOOLEAN = "java.lang.Boolean";
    static final String BYTE = "java.lang.Byte";
    static final String SHORT = "java.lang.Short";
    static final String CHARACTER = "java.lang.Character";
    static final String BIG_DECIMAL = "java.math.BigDecimal";
    static final String TIME = "java.sql.Time";

    static final String O = "j.m.JSON$O";

    //system const name
    static final String TRUE = "true";
    static final String FALSE = "false";
    static final String NULL = "null";

    //primitive type name
    static final String TYPE_CHAR = "char";
    static final String TYPE_BYTE = "byte";
    static final String TYPE_SHORT = "short";
    static final String TYPE_INT = "int";
    static final String TYPE_LONG = "long";
    static final String TYPE_FLOAT = "float";
    static final String TYPE_DOUBLE = "double";
    static final String TYPE_BOOLEAN = "boolean";

    //j.v annotation class name
    static final String ANNO_REQUIRE = "j.v.Require";
    static final String ANNO_REGEXP = "j.v.RegExp";
    static final String ANNO_RANGE = "j.v.Range";

    //j.i
    static final String FLIST = "j.i.FList";
    static final String COUPLE = "j.i.Couple";
    static final String TRIAD = "j.i.Triad";

    static final String LOCALE = "java.util.Locale";
}
//</editor-fold>

class ValidException extends RuntimeException {

    ValidException(String message) {
        super(message);
    }
}

//</editor-fold>
