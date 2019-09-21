//<editor-fold>
/**
 * extend list think in datatable
 *
 * @author Fred Weng
 */
package j.m;

import j.Env;
import j.u.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.math.BigDecimal;

public class XList<T> extends ArrayList<T> implements IX {

    //<editor-fold defaultstate="collapsed" desc="ctor">
    public XList() {
        super();
    }

    public XList(Collection<T> values) {
        super(values);
    }

    @SuppressWarnings("unchecked")
    public XList(T... params) {
        super(Arrays.asList(params));
    }

    public final XList<T> append(T item) {
        super.add(item);
        return this;
    }

    @SuppressWarnings("unchecked")
    public final XList<T> append(T... params) {
        super.addAll(Arrays.asList(params));
        return this;
    }

    public final XList<T> appends(T[] params) {
        return append(params);
    }

    public final XList<T> merge(List<T> list) {
        this.addAll(list);
        return this;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="fromXXX">
    @SuppressWarnings("unchecked")
    public static final <U> XList<U> fromJSON(String json) {
        Object o = JSON.parse(json);
        if (o instanceof XList)
            return (XList<U>) o;
        return new XList<>();
    }

    @SuppressWarnings("unchecked")
    public static final <U> XList<U> fromXML(String xml) {
        Object o = XML.parse(xml);
        if (o instanceof XList)
            return (XList<U>) o;
        return new XList<>();
    }

    @SuppressWarnings("unchecked")
    public static final <U> XList<U> of(U... params) {
        return new XList<>(params);
    }

    public static final <U> XList<U> fromArray(U[] params) {
        return new XList<>(params);
    }

    public static final <U> XList<U> fromStream(Stream<U> stream) {
        return stream.parallel().collect(XList::new, XList::add, XList::addAll);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="toXXX">
    /**
     * convert XList(T) to XList(U)
     *
     * @param <U> to Object class Type
     * @param clazz Object class
     * @return class Type object list
     */
    @SuppressWarnings("unchecked")
    public <U> XList<U> toObjectList(Class<U> clazz) {
        return this.parallelStream().collect(XList::new,
                (x, y) -> x.add((U) XMap.o2t(null, y, clazz)),
                XList::addAll);
    }

    /**
     * map function can only convert Stream(A) to Stream(B)
     *
     * @param <U> to Object class Type
     * @param clazz Object class
     * @return class Type object stream
     */
    @SuppressWarnings("unchecked")
    public <U> Stream<U> toObjectStream(Class<U> clazz) {
        return this
                .parallelStream()
                .map(x -> (U) XMap.o2t(null, x, clazz));
    }

    @SuppressWarnings("unchecked")
    public XMap toXMap(Object key) {
        return ((XList<XMap>) this)
                .parallelStream()
                .collect(XMap::new,
                        (x, y) -> x.put(y.getString(key), y),
                        XMap::putAll);
    }

    /**
     * Since 4.0 for Strongly typed Entity List XList.fromJSON(this.toJSON())
     * for Earse type for Develop and Debug model just for developer debug
     * system out
     */
    @SuppressWarnings("unchecked")
    public void tableFormat() {

        final String tab = "\t\t";
        final String empty = "";
        final String line = "--------------------------------";
        java.util.function.BiConsumer<Integer, StringBuilder> biConsumer = (x, y) -> {
            IntStream.range(0, x).forEach(i -> y.append(line));
            y.append(Env.LINE_SEP_OS);
        };

        int z = this.size();
        if (z == 0)
            return;

        XList<XMap> d = this.get(0) instanceof XMap ? (XList<XMap>) this : XList.<XMap>fromJSON(this.toJSON());
        Set n = d.parallelStream().collect(HashSet::new, (x, y) -> x.addAll(y.keySet()), Set::addAll);

        int w = n.size();
        if (w == 0)
            return;

        StringBuilder s = new StringBuilder();
        Object[] ns = n.toArray();

        biConsumer.accept(w, s);
        n.forEach(k -> s.append(k).append(tab));
        IntStream.range(0, z).forEach(i -> {
            s.append(Env.LINE_SEP_OS);
            biConsumer.accept(w, s);
            XMap m = d.getXMap(i);
            IntStream.range(0, w).forEach(x -> s.append(m.get(ns[x])).append(tab));
        });
        s.append(Env.LINE_SEP_OS);
        IntStream.range(0, w).forEach(x -> s.append(line));

        System.out.println(s.toString());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="path-get-from">
    @Override
    public T get(int index) {
        if (index < 0 || index >= this.size())
            return null;
        return super.get(index);
    }

    public Character getOChar(int index) {
        return ObjU.toOChar(get(index));
    }

    public Short getOShort(int index) {
        return ObjU.toOShort(get(index));
    }

    public Byte getOByte(int index) {
        return ObjU.toOByte(get(index));
    }

    public Integer getOInt(int index) {
        return ObjU.toOInt(get(index));
    }

    public Long getOLong(int index) {
        return ObjU.toOLong(get(index));
    }

    public Boolean getOBoolean(int index) {
        return ObjU.toOBool(get(index));
    }

    public Float getOFloat(int index) {
        return ObjU.toOFloat(get(index));
    }

    public Double getODouble(int index) {
        return ObjU.toODouble(get(index));
    }

    public char getChar(int index) {
        return ObjU.toChar(get(index));
    }

    public short getShort(int index) {
        return ObjU.toShort(get(index));
    }

    public byte getByte(int index) {
        return ObjU.toByte(get(index));
    }

    public int getInt(int index) {
        return ObjU.toInt(get(index));
    }

    public long getLong(int index) {
        return ObjU.toLong(get(index));
    }

    public boolean getBoolean(int index) {
        return ObjU.toBool(get(index));
    }

    public float getFloat(int index) {
        return ObjU.toFloat(get(index));
    }

    public double getDouble(int index) {
        return ObjU.toDouble(get(index));
    }

    /////////////////////////////////
    public String getString(int index) {
        return ObjU.toString(get(index));
    }

    public BigDecimal getBigDecimal(int index) {
        return ObjU.toBigDecimal(get(index));
    }

    public Date getDate(int index) {

        return ObjU.toDate(get(index));
    }

    public XMap getXMap(int index) {
        Object o = get(index);

        if (o instanceof XMap)
            return (XMap) o;
        else if (o instanceof Map)
            return new XMap((Map) o);
        else
            return new XMap();
    }

    /**
     *
     * @param <R> this XList's item(XList)'s item type
     * @param index index
     * @return XList
     */
    @SuppressWarnings("unchecked")
    public <R> XList<R> getXList(int index) {
        Object o = get(index);

        if (o instanceof XList)
            return (XList<R>) o;
        else if (o instanceof List)
            return new XList<>((List) o);
        else
            return new XList<>();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="sql-operation">
    @SuppressWarnings("unchecked")
    public XList<XMap> select(String sqls) {
        int z = this.size();
        if (z < 1)
            return (XList<XMap>) this;
        SQL sql = new SQL(sqls);
        if (sql.group != null)
            return group(sql, z);
        else if (sql.order != null)
            return order(sql, z);
        return filter(sql, z);
    }

    public XMap first(String sqls) {
        int z = this.size();
        if (z < 1)
            return new XMap();
        SQL sql = new SQL(sqls);
        if (sql.group != null)
            return group(sql, z).getXMap(0);
        else if (sql.order != null)
            return order(sql, z).getXMap(0);
        return filterFirst(sql, z);
    }

    public XList<XMap> select(String sqls, Object... params) {
        return select(StrU.fastFormat(sqls, params));
    }

    public XMap first(String sqls, Object... params) {
        return first(StrU.fastFormat(sqls, params));
    }

    //<editor-fold desc="sql group">
    @SuppressWarnings("unchecked")
    private XList<XMap> group(SQL sql, int z) {

        XMap map = new XMap();
        for (int i = 0; i < z; i++) {
            XMap row = this.getXMap(i);
            if (sql.group.get(0) != null && !sql.filter(row))
                continue;
            XList group_key = new XList();
            sql.group.get(1).stream().forEach(f -> group_key.add(row.get(f)));

            if (!map.containsKey(group_key))
                map.put(group_key, new XMap());

            sql.group.get(1).stream().forEach(f -> map.getXMap(group_key).put(f, row.get(f)));

            List<String> group_tokens = sql.group.get(2);
            int group_tokens_size = group_tokens.size();
            for (int j = 0; j < group_tokens_size; j++) {
                XMap m = map.getXMap(group_key);
                switch (group_tokens.get(j).hashCode()) {
                    case 94851343:
                        m.put("count", m.getInt("count") + 1);
                        break;
                    case 114251:
                        sum(m, group_tokens.get(++j), row);
                        break;
                    case 107876:
                        max(m, group_tokens.get(++j), row);
                        break;
                    case 108114:
                        min(m, group_tokens.get(++j), row);
                    default:
                        break;
                }
            }
        }
        if (sql.group.get(3) != null) {
            TreeSet tree = new TreeSet(new XCmp(sql.group.get(3)));
            tree.addAll(map.values());
            return new XList<>(tree);
        }

        return new XList<>(map.values());
    }

    private static class XCmp implements Comparator<XMap> {

        private final List<String> order;

        public XCmp(List<String> order) {
            this.order = order;
        }

        @Override
        public int compare(XMap x, XMap y) {
            int r;
            for (String o : order)
                switch (o.charAt(0)) {
                    case '-':
                        if ((r = comp(y, x, o.substring(1))) != 0)
                            return r;
                        break;
                    case '+':
                        if ((r = comp(x, y, o.substring(1))) != 0)
                            return r;
                        break;
                    default:
                        if ((r = comp(x, y, o)) != 0)
                            return r;
                        break;
                }
            return 1;
        }

        public static int compare(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }

        public static int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }

        private int comp(XMap x, XMap y, String o) {
            switch (o.indexOf(':')) {
                case 0:
                    o = o.substring(1);
                    return ObjU.toString(x.get(o)).compareTo(ObjU.toString(y.get(o)));
                case 1:
                    switch (o.charAt(0)) {
                        case 's':
                            o = o.substring(2);
                            return ObjU.toString(x.get(o)).compareTo(ObjU.toString(y.get(o)));
                        case 'd':
                            o = o.substring(2);
                            return ObjU.toDate(x.get(o)).compareTo(ObjU.toDate(y.get(o)));
                        case 'f':
                            o = o.substring(2);
                            return Float.compare(ObjU.toFloat(x.get(o)), ObjU.toFloat(y.get(o)));
                        case 'i':
                            o = o.substring(2);
                            return compare(ObjU.toInt(x.get(o)), ObjU.toInt(y.get(o)));
                        case 'l':
                            o = o.substring(2);
                            return compare(ObjU.toLong(x.get(o)), ObjU.toLong(y.get(o)));
                        default:
                            return ObjU.toString(x.get(o)).compareTo(ObjU.toString(y.get(o)));
                    }
                default:
                    return ObjU.toString(x.get(o)).compareTo(ObjU.toString(y.get(o)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void sum(XMap m, String f, XMap r) {
        String n = "sum_" + f;
        m.put(n, m.getFloat(n) + r.getFloat(f));
    }

    @SuppressWarnings("unchecked")
    private static void max(XMap m, String f, XMap r) {
        String n = "max_" + f;
        if (!m.containsKey(n))      //bug:if all field <0 , the max is 0
            m.put(n, r.getFloat(f));

        m.put(n, Float.max(m.getFloat(n), r.getFloat(f)));
    }

    @SuppressWarnings("unchecked")
    private static void min(XMap m, String f, XMap r) {
        String n = "min_" + f;
        if (!m.containsKey(n))
            m.put(n, r.getFloat(f));
        m.put(n, Float.min(m.getFloat(n), r.getFloat(f)));
    }
    //</editor-fold>

    @SuppressWarnings("unchecked")
    private XList<XMap> order(SQL sql, int z) {

        TreeSet tree = new TreeSet(new XCmp(sql.order.get(1)));

        if (sql.order.get(0) == null) {
            tree.addAll(this);
            return new XList<>(tree);
        }

        for (int i = 0; i < z; i++) {
            XMap map = this.getXMap(i);
            if (sql.filter(map))
                tree.add(map);
        }

        return new XList<>(tree);
    }

    private XList<XMap> filter(SQL sql, int z) {

        XList<XMap> list = new XList<>();

        for (int i = 0; i < z; i++) {
            XMap map = this.getXMap(i);
            if (sql.filter(map))
                list.add(map);
        }

        return list;
    }

    private XMap filterFirst(SQL sql, int z) {

        for (int i = 0; i < z; i++) {
            XMap map = this.getXMap(i);
            if (sql.filter(map))
                return map;
        }

        return new XMap();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="fun-operation">
    @SuppressWarnings("unchecked")
    public Set project(Object key) {
        return ((XList<XMap>) this)
                .parallelStream()
                .collect(
                        HashSet::new,//JSON$O to String O.o
                        (x, y) -> x.add(y.getString(key)),
                        HashSet::addAll
                );
    }

    @SuppressWarnings("unchecked")
    public XList<XMap> leftJoin(XList<XMap> other, Object key) {
        XMap k = other.toXMap(key);
        return ((XList<XMap>) this)
                .parallelStream()
                .collect(XList::new,
                        (x, y) -> x.add(y.merge(k.getXMap(y.getString(key)))),
                        XList::addAll);
    }

    @SuppressWarnings("unchecked")
    public XList<XMap> innerJoin(XList<XMap> other, Object key) {
        XMap k = other.toXMap(key);
        return ((XList<XMap>) this)
                .parallelStream()
                .collect(XList::new,
                        (x, y) -> {
                            XMap w = k.getXMap(y.getString(key));
                            if (!w.isEmpty())
                                x.add(y.merge(w));
                        },
                        XList::addAll);
    }

    public XList<T> where(Predicate<T> w) {
        return this
                .parallelStream()
                .collect(XList::new,
                        (x, y) -> {
                            if (w.test(y))
                                x.add(y);
                        },
                        XList::addAll);
    }

    public XList<T> each(Consumer<T> action) {
        this.parallelStream().forEach(action);
        return this;
    }

    public <R> XList<R> map(Function<T, R> mapper) {
        return this
                .parallelStream()
                .collect(XList::new,
                        (x, y) -> x.add(mapper.apply(y)),
                        XList::addAll);
    }

    public static <U> Collector<U, ?, XList<U>> collector() {
        return Collector.of(XList::new, XList::add, XList::merge, Collector.Characteristics.IDENTITY_FINISH);
    }
    //</editor-fold>

    private static class SQL {

        private List<String> tokens = null;
        private int index = 0, count = 0;
        public List<List<String>> group = null;
        public List<List<String>> order = null;
        private static final List<String> EMPTY = new ArrayList<>(0);

        public SQL(String xql) {
            tokens = new Lexer(xql).tokens();
            index = -1;
            count = tokens.size();
            if (null == (group = group()))
                order = order();
        }

        private List<List<String>> group() {
            int i = tokens.indexOf(GROUP);
            if (i < 0)
                return null;
            List<List<String>> list = new ArrayList<>(4);
            if (i == 0)
                list.add(null);
            else
                list.add(EMPTY);

            int j = tokens.indexOf(FOR);
            if (j > i + 2 && j + 1 < count) {
                list.add(tokens.subList(i + 2, j));
                int k = tokens.indexOf(ORDER);
                if (k < 0) {
                    list.add(tokens.subList(j + 1, count));
                    list.add(null);
                    return list;
                }
                if (k + 2 < count) {
                    list.add(tokens.subList(j + 1, k));
                    list.add(tokens.subList(k + 2, count));
                    return list;
                }
            }

            return null;
        }

        private List<List<String>> order() {
            int i = tokens.indexOf(ORDER);
            if (i < 0)
                return null;
            List<List<String>> list = new ArrayList<>(2);
            if (i == 0)
                list.add(null);
            else
                list.add(EMPTY);
            if (i + 2 < count) {
                list.add(tokens.subList(i + 2, count));
                return list;
            }
            return null;
        }

        public boolean filter(XMap dr) {
            next();
            boolean result = this.andOr(dr);
            index = -1;
            return result;
        }

        private void next() {
            if (index < count - 1)
                index++;
        }

        private boolean andOr(XMap d) {
            String p;
            boolean r = this.not(d), t;
            while ((p = tokens.get(index)).equals(AND) || p.equals(OR)) {
                next();

                if (p.equals(AND)) {
                    t = this.not(d);
                    r = r && t;
                } else {
                    t = this.not(d);
                    r = r || t;
                }
            }
            return r;
        }

        private boolean not(XMap d) {
            String p;

            if ((p = tokens.get(index)).equals(NOT))
                next();

            boolean r = this.bracket(d);

            if (p.equals(NOT))
                return !r;

            return r;
        }

        private boolean bracket(XMap d) {

            boolean r;
            if (tokens.get(index).equals("(")) {
                next();
                r = this.andOr(d);
                next();
            } else
                r = this.compare(d);
            return r;
        }

        private boolean compare(XMap d) {

            if (!d.containsKey(tokens.get(index)))
                LogU.error(tokens.get(index) + " field not found!");

            Object o = d.get(tokens.get(index));
            next();
            String p = tokens.get(index);
            next();
            String v = tokens.get(index);
            next();
            switch (p.hashCode()) {
                case 3321751://like
                    return like(ObjU.toString(o), v);
                case 62://>
                    return gt(o, v);
                case 60://<
                    return lt(o, v);
                case 1952://==
                    return eq(o, v);
                case 1983://>=
                    return !lt(o, v);
                case 1921://<=
                    return !gt(o, v);
                case 1084://!=
                    return !eq(o, v);
            }

            return false;

        }

        private static boolean like(String f, String v) {
            int n = v.length();
            if (v.startsWith("'%") && v.endsWith("%'"))
                return f.contains(v.substring(2, n - 2));
            else if (v.startsWith("'%"))
                return f.endsWith(v.substring(2, n - 1));
            else if (v.endsWith("%'"))
                return f.startsWith(v.substring(1, n - 2));
            else
                return f.equals(v.substring(1, n - 1));
        }

        private static boolean gt(Object f, String v) {

            if (v.charAt(0) == '\'')
                return ObjU.toString(f).compareTo(v.substring(1, v.length() - 1)) > 0;

            switch (NT.numT(v)) {
                case NT.NT_INTEGER:
                    return ObjU.toInt(f) > ObjU.toInt(v);
                case NT.NT_LONG:
                    return ObjU.toLong(f) > ObjU.toLong(v);
                case NT.NT_FLOAT:
                    return ObjU.toFloat(f) > ObjU.toFloat(v);
                case NT.NT_DOUBLE:
                    return ObjU.toDouble(f) > ObjU.toDouble(v);
                default:
                    return ObjU.toString(f).compareTo(v) > 0;
            }
        }

        private static boolean lt(Object f, String v) {
            if (v.charAt(0) == '\'')
                return ObjU.toString(f).compareTo(v.substring(1, v.length() - 1)) < 0;

            switch (NT.numT(v)) {
                case NT.NT_INTEGER:
                    return ObjU.toInt(f) < ObjU.toInt(v);
                case NT.NT_LONG:
                    return ObjU.toLong(f) < ObjU.toLong(v);
                case NT.NT_FLOAT:
                    return ObjU.toFloat(f) < ObjU.toFloat(v);
                case NT.NT_DOUBLE:
                    return ObjU.toDouble(f) < ObjU.toDouble(v);
                default:
                    return ObjU.toString(f).compareTo(v) < 0;
            }
        }

        private static boolean eq(Object f, String v) {
            if (v.charAt(0) == '\'')
                return ObjU.toString(f).equals(v.substring(1, v.length() - 1));

            switch (v.hashCode()) {
                case 3569038://true
                    return ObjU.toBool(f);
                case 97196323://false
                    return !ObjU.toBool(f);
                case 3392903://null
                    return f == null;
                default:
                    switch (NT.numT(v)) {
                        case NT.NT_INTEGER:
                            return ObjU.toInt(f) == ObjU.toInt(v);
                        case NT.NT_LONG:
                            return ObjU.toLong(f) == ObjU.toLong(v);
                        case NT.NT_FLOAT:
                            return Math.abs(ObjU.toFloat(f) - ObjU.toFloat(v)) < NT.EPSILON;
                        case NT.NT_DOUBLE:
                            return Math.abs(ObjU.toDouble(f) - ObjU.toDouble(v)) < NT.EPSILON;
                        default:
                            return ObjU.toString(f).equals(v);
                    }
            }
        }

        private static class Lexer {

            private ArrayList<String> t = new ArrayList<>();

            public Lexer(String s) {
                int n = s.length();
                char[] a = s.toCharArray();

                for (int i = 0; i < n; i++)
                    switch (a[i]) {
                        case ' ':
                        case ',':
                            flush(s, i);
                            break;

                        case '<':

                            if (i + 1 < n && a[i + 1] == '=') {
                                flush(s, i + 1);
                                t.add("<=");
                                i++;
                            } else {
                                flush(s, i);
                                t.add("<");
                            }
                            break;

                        case '>':
                            if (i + 1 < n && a[i + 1] == '=') {
                                flush(s, i + 1);
                                t.add(">=");
                                i++;
                            } else {
                                flush(s, i);
                                t.add(">");
                            }
                            break;

                        case '(':
                            flush(s, i);
                            t.add("(");
                            break;

                        case ')':
                            flush(s, i);
                            t.add(")");
                            break;

                        case '=':
                            flush(s, i + 1);
                            t.add("==");
                            i++;
                            break;

                        case '!':
                            flush(s, i + 1);
                            t.add("!=");
                            i++;
                            break;

                        default:
                            k = i;
                            f = 1;
                            break;
                    }

                flush(s, n);
            }

            public List<String> tokens() {
                return this.t;
            }
            int j = 0, k = 0, f = 0;

            private void flush(String s, int i) {
                if (1 == f) {
                    t.add(s.substring(j, k + 1));
                    f = 0;
                }
                j = i + 1;
            }
        }

        private static final String GROUP = "group";
        private static final String FOR = "for";
        private static final String ORDER = "order";
        private static final String AND = "and";
        private static final String NOT = "not";
        private static final String OR = "or";
    }

    private static class NT {

        public static final int NT_NOT_NUMBER = -1;
        public static final int NT_INTEGER = 10;
        public static final int NT_LONG = 19;
        public static final int NT_FLOAT = 7;
        public static final int NT_DOUBLE = 16;
        public static final int NT_BIG_INTEGER = 1024;
        public static final int NT_BIG_DECIMAL = 1025;
        public static double EPSILON = 0.001;

        public static int numT(String num) {

            if (num == null)
                return -1;

            String s = num.trim();

            int n = s.length();

            if (n == 0)
                return -1;

            char[] b = s.toCharArray();

            int k = 0;
            int j = 0;

            for (int i = n - 1; i >= 0; i--)
                switch (b[i]) {
                    case '+':
                    case '-':
                        if (i != 0 || n == 1)
                            return -1;
                        break;
                    case '.':
                        if (++k > 1)
                            return -1;
                        if (i == n - 1 || i == 0)
                            return -1;
                        break;
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (++j > 1024)
                            return -1;
                        break;
                    default:
                        return -1;
                }

            if (k == 0) {

                if (j < 10)
                    return 10;

                if (j < 19)
                    return 19;

                return 1024;
            }

            if (j < 7)
                return 7;

            if (j < 16)
                return 16;

            return 1025;
        }

    }

    //<editor-fold defaultstate="collapsed" desc="IX">
    @Override
    public boolean isXList() {
        return true;
    }

    @Override
    public XMap toXMap() {
        return new XMap(this);
    }
    //</editor-fold>
}
//</editor-fold>
