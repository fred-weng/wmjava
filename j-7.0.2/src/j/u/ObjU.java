//<editor-fold>
/**
 * All inputs are Object so the tool class is called Obj(ect)U(til)
 */
package j.u;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.time.Instant;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

public class ObjU {

    //<editor-fold defaultstate="collapsed" desc="common type converter">
    private static String int2s(Object o) {
        if (null == o)
            return null;

        String s = o.toString().trim();

        if ("".equals(s))
            return null;

        int index = s.indexOf('.');
        //.123 format exception
        if (index == 0)
            return null;
        //12.345
        if (index > 0)
            s = s.substring(0, index);

        return s;
    }

    public static Short toOShort(Object o) {
        String s = int2s(o);

        if (s == null)
            return null;

        try {
            return Short.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static short toShort(Object o) {
        String s = int2s(o);

        if (s == null)
            return 0;

        try {
            return Short.parseShort(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0;
        }
    }

    public static Byte toOByte(Object o) {
        String s = int2s(o);

        if (s == null)
            return null;

        try {
            return Byte.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static byte toByte(Object o) {
        String s = int2s(o);

        if (s == null)
            return 0;

        try {
            return Byte.parseByte(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0;
        }
    }

    public static Integer toOInt(Object o) {
        String s = int2s(o);

        if (s == null)
            return null;

        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static int toInt(Object o) {
        String s = int2s(o);

        if (s == null)
            return 0;

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0;
        }

    }

    public static Long toOLong(Object o) {
        String s = int2s(o);

        if (s == null)
            return null;

        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static long toLong(Object o) {

        String s = int2s(o);

        if (s == null)
            return 0L;

        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0L;
        }
    }

    public static Boolean toOBool(Object o) {
        if (o == null)
            return null;

        switch (o.toString().trim().toLowerCase()) {
            case "true":
                return Boolean.TRUE;
            case "false":
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    public static boolean toBool(Object o) {
        return o != null && o.toString().trim().equalsIgnoreCase("true");
    }

    public static Character toOChar(Object o) {
        if (null == o)
            return null;

        String s = o.toString().trim();

        if ("".equals(s))
            return null;

        return s.charAt(0);
    }

    public static char toChar(Object o) {
        if (null == o)
            return ' ';

        String s = o.toString().trim();

        if ("".equals(s))
            return ' ';

        return s.charAt(0);
    }

    public static Float toOFloat(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();

        if ("".equals(s))
            return null;

        try {
            return Float.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static float toFloat(Object o) {
        if (o == null)
            return 0F;

        String s = o.toString().trim();
        if ("".equals(s))
            return 0F;

        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0F;
        }
    }

    public static Double toODouble(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();

        if ("".equals(s))
            return null;

        try {
            return Double.valueOf(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static double toDouble(Object o) {
        if (o == null)
            return 0D;

        String s = o.toString().trim();
        if ("".equals(s))
            return 0D;

        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            LogU.error(e.getMessage());
            return 0D;
        }
    }

    public static String toString(Object o) {
        if (null == o)
            return null;

        return o.toString();
    }

    public static BigInteger toBigInteger(Object o) {
        String s = int2s(o);

        if (s == null)
            return null;

        try {
            return new BigInteger(s);
        } catch (Exception e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static BigDecimal toBigDecimal(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static java.sql.Date toDate(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        java.util.Date tmp = parseDate(s);
        if (tmp == null)
            return null;

        return new java.sql.Date(tmp.getTime());
    }

    public static XDate toXDate(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        java.util.Date tmp = parseDate(s);
        if (tmp == null)
            return null;

        return new XDate(tmp.getTime());
    }

    public static Timestamp toTimestamp(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        try {
            return Timestamp.valueOf(s);
        } catch (Exception e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static java.time.Instant toInstant(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        try {
            return Instant.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static Time toTime(Object o) {
        if (o == null)
            return null;

        String s = o.toString().trim();
        if ("".equals(s))
            return null;

        try {
            return Time.valueOf(s);
        } catch (Exception e) {
            LogU.error(e.getMessage());
            return null;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="thread safe formater">
    /**
     * Thread safe date and number formatter bugfix:multiple threads share
     * SimpleDateFormat object conversion errors,20141027,Fred Weng
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_F = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static String formatDate(Object o) {
        return DATE_F.get().format(o);
    }

    private static final ThreadLocal<SimpleDateFormat> DATE_S_F = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    private static final Pattern DATE_FORMAT = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})(\\s\\d{2}:\\d{2}:\\d{2})?");

    public static java.util.Date parseDate(String s) {

        Matcher m = DATE_FORMAT.matcher(s);
        if (m.find())
            if (m.group(2) == null)
                try {
                    return DATE_S_F.get().parse(s);
                } catch (ParseException e) {
                    LogU.error(e.getMessage());
                    return null;
                }
            else
                try {
                    return DATE_F.get().parse(s);
                } catch (ParseException e) {
                    LogU.error(e.getMessage());
                    return null;
                }
        else
            return null;
    }

    private static final ConcurrentHashMap<String, ThreadLocal<DecimalFormat>> DECI_F = new ConcurrentHashMap<>();

    public static String formatNumber(Object number, final String format) {
        if (!DECI_F.contains(format))
            DECI_F.put(format, new ThreadLocal<DecimalFormat>() {
                @Override
                protected DecimalFormat initialValue() {
                    return new DecimalFormat(format);
                }
            });
        try {
            return DECI_F.get(format).get().format(number);
        } catch (IllegalArgumentException e) {
            String num = ObjU.toString(number);
            LogU.error(e.getMessage().concat(" of ObjectU formatNumber:").concat(num));
            return num;
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Object reflect invocation">
    /**
     * reflect invocation method call XMap cal to set Field value for object
     * deserialization JSON cal to get Field value for object serialization see
     * also JSON.e2j
     *
     * @param o
     * @param m
     * @param p
     * @return
     */
    private static final String METHOD_CAL_ERRMSG = "{0}.{1}() throw exception: {2}.";

    public static Object cal(Object o, Method m, Object... p) {
        try {
            return m.invoke(o, p);
        } catch (InvocationTargetException e) {
            LogU.warning(StrU.fastFormat(METHOD_CAL_ERRMSG,
                    o.getClass().getName(),
                    m.getName(),
                    e.getTargetException().getMessage()));

            return null;
        } catch (ReflectiveOperationException e) {
            LogU.warning(StrU.fastFormat(METHOD_CAL_ERRMSG,
                    o.getClass().getName(),
                    m.getName(),
                    e.getMessage()));

            return null;
        }
    }
    //</editor-fold>
}
//</editor-fold>
