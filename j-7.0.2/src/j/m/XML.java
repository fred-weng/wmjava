//<editor-fold>

/**
 * XML Parser
 * 
 * @author Fred Weng
 */

package j.m;

import j.Env;
import j.u.ObjU;
import j.u.StrU;
import java.util.Map;
import java.util.List;

public class XML {

    //<editor-fold defaultstate="collapsed" desc="serialize">
    public static Object parse(String s) {
        if (s == null || "".equals(s))
            return null;

        String[] n;
        Lexer lex = new Lexer(s);
        if ((n = lex.next()) == null)
            return null;

        switch (n[0].hashCode()) {
            case 48:
                return n[2];
            case 49:
                return parseN(lex);
            case 50:
                return n[2];
            default:
                return null;
        }
    }

    /**
     * can serialize any object to xml
     *
     * @param o
     * @return
     */
    public static String toXML(Object o) {
        return to_XML(JSON.parseX(JSON.toJSON(o)));
    }

    /**
     * can serialize any object to pretty xml
     *
     * @param o
     * @return
     */
    public static String toPrettyXML(Object o) {
        return to_PrettyXML(JSON.parseX(JSON.toJSON(o)));
    }

    /**
     * only serialize XMap/XList to xml
     * 
     * @param o
     * @return
     */
    static String to_XML(IX o) {
        StringBuilder s = new StringBuilder(XML_HEADER);
        o2x(s, "xml", o, -100, false);
        return s.toString();
    }

    /**
     * only serialize XMap/XList pretty xml
     *
     * @param o
     * @return
     */
    static String to_PrettyXML(IX o) {
        StringBuilder s = new StringBuilder(XML_HEADER);
        o2x(s, "xml", o, 0, true);
        return s.toString();
    }
    //</editor-fold>

    //<editor-fold desc="parse" defaultstate="collapsed">
    @SuppressWarnings("unchecked")
    private static Object parseN(Lexer lex) {
        String[] n = lex.next();
        if ("row".equals(n[1])) {
            XList list = new XList();
            do
                switch (n[0].hashCode()) {
                    case 48://0
                        list.add(n[2]);
                        break;
                    case 49://1
                        list.add(parseN(lex));
                        break;
                    case 50://2
                        list.add(n[2]);
                        break;
                    case 1444://-1
                        return list;
                }
            while ((n = lex.next()) != null);
            return list;
        }
        XMap map = new XMap();
        do
            switch (n[0].hashCode()) {
                case 48:
                    map.put(n[1], n[2]);
                    break;
                case 49:
                    map.put(n[1], parseN(lex));
                    break;
                case 50:
                    map.put(n[1], n[2]);
                    break;
                case 1444:
                    return map;
            }
        while ((n = lex.next()) != null);
        return map;
    }
    //</editor-fold>

    //<editor-fold desc="private toXML" defaultstate="collapsed">
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>";

    private static void o2x(StringBuilder s, Object k, Object o, int n, boolean m) {
        if (o == null)
            return;
        ident(s, n, m);
        s.append('<').append(k).append('>');
        switch (o.getClass().getName()) {
            case N.XMAP:
            case N.HASHMAP:
                m2x(s, (Map) o, n, m);
                break;
            case N.XLIST:
            case N.ARRAYLIST:
                l2x(s, (List) o, n, m);
                break;
            case N.STRING:
                if (contains(o, '<', '&'))//20170208 varargs
                    s.append("<![CDATA[").append(o).append("]]>");
                else
                    s.append(o);
                break;
            case N.TIME_STAMP:
            case N.DATE:
            case N.SDATE:
                s.append(ObjU.formatDate(o));
                break;
            case N.O:
            default:
                s.append(o);
                break;
        }
        s.append("</").append(k).append('>');
    }

    @SuppressWarnings("unchecked")
    private static void m2x(StringBuilder s, Map o, int n, boolean m) {
        o.keySet().stream().forEach((k) -> {
            o2x(s, k, o.get(k), n + 1, m);
        });
        ident(s, n, m);
    }

    @SuppressWarnings("unchecked")
    private static void l2x(StringBuilder s, List o, int n, boolean m) {
        o.stream().forEach((i) -> {
            o2x(s, "row", i, n + 1, m);
        });
        ident(s, n, m);
    }

    //20170208 varargs
    private static boolean contains(Object o, char... cs) {
        if (o == null)
            return false;
        String s = o.toString();
        for (char c : cs)
            if (s.indexOf(c) != -1)
                return true;
        return false;
    }
    
    /**
     * print ident space also for XML
     *
     * @param s
     * @param h Hierarchy
     * @param b do print indent space?
     */
    private static StringBuilder ident(StringBuilder s, int h, boolean b) {
        if (b)
            s.append(Env.LINE_SEP_OS).append(StrU.makeSpace(h * 4));
        return s;
    }
    //</editor-fold>

    private static class Lexer {

        private String s = null;
        private int i = 0, j = 0, k = 0, m = 0;
        private final String[] r = new String[3];

        public Lexer(String s) {
            this.s = s;
        }

        public String[] next() {
            i = s.indexOf('<', j);
            if (i == -1)
                return null;
            switch (s.charAt(i + 1)) {
                case '?':
                    j = s.indexOf('>', i);
                    return next();
                case '/':
                    j = s.indexOf('>', i);
                    return get("-1", s.substring(i + 2, j), "");
                default:
                    break;
            }
            k = s.indexOf('<', i + 1);
            switch (s.charAt(k + 1)) {
                case '/':
                    j = s.indexOf('>', k);
                    m = s.indexOf('>', i);
                    return get("0", s.substring(i + 1, m), s.substring(m + 1, s.indexOf('<', i + 1)));
                case '!':
                    m = s.indexOf("]]>", k + 1);
                    j = s.indexOf('>', m + 3);
                    return get("2", s.substring(i + 1, s.indexOf('>', i)), s.substring(k + 9, m));
                default:
                    j = s.indexOf('>', i);
                    return get("1", s.substring(i + 1, j), "");
            }
        }

        private String[] get(String t, String n, String v) {
            r[0] = t;
            r[1] = n;
            r[2] = v;
            return this.r;
        }
    }
}
//</editor-fold>

