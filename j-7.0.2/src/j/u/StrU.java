//<editor-fold>
package j.u;

import java.util.*;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * String Util Class
 *
 * @author wengmj
 */
public class StrU {

    public static String makeSpace(int n) {

        char[] cs = new char[n];
        for (int i = 0; i < n; i++)
            cs[i] = ' ';
        return new String(cs);

    }

    //<editor-fold defaultstate="collapsed" desc="fastFormat 2 3 4">
    public static String fastFormat(String format, Object... args) {
        return fastFormat3(format, '{', '}', args);
    }

    public static String fastFormat2(String format, Object... args) {
        return fastFormat3(format, '[', ']', args);
    }

    public static String fastFormat3(String format, char s, char e, Object... args) {
        return fastFormat4(format, s, e, args);
    }

    public static String fastFormat3(String format, String s, String e, Object... args) {
        return fastFormat4(format, s, e, args);
    }

    public static String fastFormat4(String format, char s, char e, Object[] args) {
        StringBuilder sb = new StringBuilder();
        int i, j = -1;
        while ((i = format.indexOf(s, j + 1)) != -1) {
            sb.append(format.substring(j + 1, i));
            j = format.indexOf(e, i + 1);
            sb.append(args[fastParseInt(format.substring(i + 1, j))]);
        }
        sb.append(format.substring(j + 1));
        return sb.toString();
    }

    public static String fastFormat4(String format, String s, String e, Object[] args) {

        int sLen = s.length();
        int eLen = e.length();
        int i, j = -eLen;

        StringBuilder sb = new StringBuilder();

        while ((i = format.indexOf(s, j + eLen)) != -1) {
            sb.append(format.substring(j + eLen, i));
            j = format.indexOf(e, i + sLen);
            sb.append(args[fastParseInt(format.substring(i + sLen, j))]);
        }
        sb.append(format.substring(j + eLen));
        return sb.toString();
    }

    private static int fastParseInt(String n) {
        switch (n.hashCode()) {
            case 48:
                return 0;
            case 49:
                return 1;
            case 50:
                return 2;
            case 51:
                return 3;
            case 52:
                return 4;
            case 53:
                return 5;
            case 54:
                return 6;
            case 55:
                return 7;
            case 56:
                return 8;
            case 57:
                return 9;
            default:
                return Integer.parseInt(n);
        }
    }
    //</editor-fold>

    public static String replace(String s, String f, String r) {

        int i, j = 0, k = f.length();

        StringBuilder sb = new StringBuilder();
        while ((i = s.indexOf(f, j)) >= 0) {
            sb.append(s.substring(j, i)).append(r);
            j = i + k;
        }

        if (j < s.length())
            sb.append(s.substring(j));

        return sb.toString();

    }

    public static <T> String join(Iterator<T> i, String s, Function<T, String> m) {

        if (!i.hasNext())
            return "";

        StringBuilder sb = new StringBuilder(m.apply(i.next()));
        while (i.hasNext())
            sb.append(s).append(m.apply(i.next()));

        return sb.toString();

    }

    @SuppressWarnings("unchecked")
    public static String join(Iterator it, String separator) {
        return join(it, separator, ObjU::toString);
    }

    public static String join(Collection col, String separator) {
        return join(col.iterator(), separator);
    }

    public static <T> String join(T[] arr, String separator) {
        return join(Arrays.asList(arr), separator);
    }

    public static String[] split(String s, char d) {

        ArrayList<String> list = new ArrayList<>();
        int i = 0, j;
        while ((j = s.indexOf(d, i)) >= 0) {
            list.add(s.substring(i, j));
            i = j + 1;
        }
        if (i < s.length())
            list.add(s.substring(i));
        return list.toArray(new String[0]);

    }

    public static String bind(String format, Map m) {

        StringBuilder sb = new StringBuilder();
        int i = -1, j = -1;
        while ((i = format.indexOf('{', i + 1)) != -1) {
            sb.append(format.substring(j + 1, i));
            j = format.indexOf('}', j + 1);
            sb.append(m.get(format.substring(i + 1, j)));
        }
        sb.append(format.substring(j + 1));
        return sb.toString();

    }

    public static boolean isEmpty(String o) {

        if (null == o || "".equals(o))
            return true;
        char cs[] = o.toCharArray();
        int n = cs.length;
        for (int i = 0; i < n; i++)
            if (cs[i] > 32)
                return false;
        return true;

    }

    public static String ifEmpty(String v, String d) {
        return isEmpty(v) ? d : v;
    }

    private static final ConcurrentHashMap<String, ThreadLocal<MessageFormat>> MS_F = new ConcurrentHashMap<>();

    public static String format(final String format, Object... args) {

        if (!MS_F.contains(format))
            MS_F.put(format, new ThreadLocal<MessageFormat>() {
                @Override
                protected MessageFormat initialValue() {
                    return new MessageFormat(format);
                }
            });

        return MS_F.get(format).get().format(args);
    }

    /**
     * first letter fast toUpperCase annotation upper case retry setMethod to
     * field name get value from map by key retry refer from MI.M.constructor in
     * this file and XMap.getV in XMap.java!!
     *
     * @see also MI.firstCharLower
     * @param n the string first char to upper
     * @return result string first char uppercased
     */
    public static String firstCharUpper(String n) {

        char[] cs = n.toCharArray();

        // fixed 20161118 online bug , not lowercase unchanged
        if (cs[0] >= 'a' && cs[0] <= 'z')
            cs[0] -= 32;

        return new String(cs);
    }

    //concat part url with /
    public static String urlCat(String a, String b) {
        return (a.endsWith("/") ? a : a.concat("/")).concat(b.startsWith("/") ? b.substring(1) : b);
    }
}
//</editor-fold>
