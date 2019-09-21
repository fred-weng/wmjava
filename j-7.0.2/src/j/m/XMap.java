//<editor-fold>
/**
 * extend hashmap
 *
 * @author Fred Weng
 */
package j.m;

import j.i.*;
import j.u.*;
import j.v.*;
import j.c.Cache;
import java.util.*;
import java.time.Instant;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.BiConsumer;
import java.lang.annotation.Annotation;
import java.util.function.DoublePredicate;
import java.util.concurrent.ConcurrentHashMap;

public class XMap extends HashMap implements IX {

    public static final String TYPE_NAME = N.XMAP;

    //<editor-fold defaultstate="collapsed" desc="ctor">
    public XMap() {
        super();
    }

    @SuppressWarnings("unchecked")
    public XMap(Map m) {
        super(m);
    }

    public XMap(Object... params) {
        super();
        this.append(params);
    }

    public XMap(Collection params) {
        super();
        this.append(params);
    }

    @SuppressWarnings("unchecked")
    public final XMap append(Collection params) {
        Iterator i;
        Object k;
        for (i = params.iterator(); i.hasNext();) {
            k = i.next();
            if (i.hasNext())
                this.put(k, i.next());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public final XMap append(Object key, Object value) {
        this.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public final XMap append(Object... params) {
        int z = params.length;
        for (int i = 0; i < z - 1; i += 2)
            this.put(params[i], params[i + 1]);
        return this;
    }

    public final XMap appends(Object[] params) {
        return append(params);
    }

    @SuppressWarnings("unchecked")
    public final XMap merge(XMap other) {
        if (null != other)
            this.putAll(other);
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="public core function" defaultstate="collapsed">
    public <T> T toObject(Class<T> rawClass) {
        return m2e(this, rawClass, rawClass.getName());
    }

    public <T> T toObject(Class<T> rawClass, Class... classArgs) {
        return g2e(this, PType.compose(rawClass, classArgs), rawClass.getName());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private method">
    /**
     * map to entity object 20160823 Fred Weng : Merge two duplicate types
     * determine code m.getParameterTypes()[0] and
     * m.getParameters()[0].getParameterizedType()
     *
     * @param <T>
     * @param map the map data source to convert to normal entity
     * @param c entity
     * @param n class name
     * @return the entity
     */
    @SuppressWarnings("unchecked")
    private static <T> T m2e(Map map, Class<T> c, String n) {

        MI mi = MI.getMI(c, n);

        final T o;
        try {
            o = (T) mi.ctor().newInstance();
        } catch (ReflectiveOperationException e) {
            return null;
        }

        //parallel set
        mi.setM.parallelStream().forEach(m -> ObjU.cal(o, m.method, valid(o2t(null, getV(map, m), m.pT), m)));
        return o;
    }

    /**
     * @param o
     * @param m
     * @return
     */
    private static Object valid(Object o, MI.M m) {

        //if the class set non-valid annotation ivs is empty list
        if (m.iVs == null)
            return o;

        //parallel valid
        m.iVs.parallelStream().forEach(x -> x.valid(o));

        return o;
    }

    /**
     * custom generic java bean ,this function is not commonly used.
     *
     * @param map the map data source to convert to generic entity
     * @param t the generic entity types with actual type parameters
     * @param n the generic entity rawType name (eg:usr.dto.UserDTO). the value
     * can get from t.getRawType.getTypeName too. redundant for performance
     * @return generic entity with actual type
     */
    @SuppressWarnings("unchecked")
    private static <T> T g2e(Map map, ParameterizedType t, String n) {

        Class<T> c = (Class<T>) t.getRawType();
        //since 5.0.1
        MI mi = MI.getMI(c, n);

        final T o;
        try {
            o = (T) mi.ctor().newInstance();
        } catch (ReflectiveOperationException e) {
            return null;
        }

        Map<String, Type> vt = VFty.getVT(t, c);
        //parallel set 20171213 and use Cache.lazy
        mi.setM.parallelStream().forEach(m -> ObjU.cal(o, m.method, valid(o2t(vt, getV(map, m), Cache.lazy(m.pN, vt, () -> m.pT)), m)));
        return o;
    }

    /**
     * fix bug : Third party incoming JSON field first capital letters
     *
     * @param map Map containing the field of the upper case name
     * @param m the upper case name
     * @return The corresponding value of the field of the capital first letters
     */
    private static Object getV(Map map, MI.M m) {

        Object o = map.get(m.name);

        if (o != null)
            m.uF = true;//m.name is equals field name
        else if (!m.uF) {
            String s = StrU.firstCharUpper(m.name);
            if ((o = map.get(s)) != null) {
                //set the method corresponding to the field name to first letters capital
                m.uF = true;
                m.name = s;
            }
        }
        return o;
    }

    //<editor-fold defaultstate="collapsed" desc="method o2t">
    /**
     * core type convertion method object to special type also for
     * XList.toObjectList key method
     *
     * @param vt
     * @param o
     * @param t
     * @return
     */
    @SuppressWarnings("unchecked")
    static Object o2t(Map<String, Type> vt, Object o, Type t) {

        String name;
        boolean isP; // isParameterizedType flag; 
        ParameterizedType p = null;

        //upgrade@20180803 for toPObject(ParameterizedType t);
        if (t instanceof ParameterizedType) {
            isP = true;
            p = (ParameterizedType) t;
            name = p.getRawType().getTypeName();
        } else {
            isP = false;
            name = t.getTypeName();
        }

        switch (name) {

            // 8 primitive type can not be null . null will cast to primitive default value
            case N.TYPE_BOOLEAN:
                return ObjU.toBool(o);
            case N.TYPE_CHAR:
                return ObjU.toChar(o);
            case N.TYPE_BYTE:
                return ObjU.toByte(o);
            case N.TYPE_SHORT:
                return ObjU.toShort(o);
            case N.TYPE_INT:
                return ObjU.toInt(o);
            case N.TYPE_LONG:
                return ObjU.toLong(o);
            case N.TYPE_FLOAT:
                return ObjU.toFloat(o);
            case N.TYPE_DOUBLE:
                return ObjU.toDouble(o);
            default:
                break;
        }

        if (o == null)
            return null;

        switch (name) {

            //  8 Boxed Primitive type  
            case N.BOOLEAN:
                return ObjU.toOBool(o);
            case N.CHARACTER:
                return ObjU.toOChar(o);
            case N.BYTE:
                return ObjU.toOByte(o);
            case N.SHORT:
                return ObjU.toOShort(o);
            case N.INTEGER:
                return ObjU.toOInt(o);
            case N.LONG:
                return ObjU.toOLong(o);
            case N.FLOAT:
                return ObjU.toOFloat(o);
            case N.DOUBLE:
                return ObjU.toODouble(o);

            // Common Object Type,j.m.O is not public class Must not appear here    
            case N.STRING:
                return ObjU.toString(o);
            case N.OBJECT:
                return o;
            case N.DATE:
            case N.SDATE:
                return ObjU.toDate(o);
            case N.XDATE://2017-12-11
                return ObjU.toXDate(o);
            case N.INSTANT://2017-12-11
                return ObjU.toInstant(o);
            case N.TIME_STAMP:
                return ObjU.toTimestamp(o);
            case N.TIME:
                return ObjU.toTime(o);
            case N.BIG_DECIMAL:
                return ObjU.toBigDecimal(o);
            case N.BIG_INTEGER:
                return ObjU.toBigInteger(o);

            //20161125
            case N.SET:
            case N.HASHSET:
                return l2s(vt, (List) o, isP ? argiType(p, 0) : Object.class);

            ///////////////////////////////// List Type
            case N.LIST:
            case N.XLIST:
            case N.ARRAYLIST:
                return l2l(vt, (List) o, isP ? argiType(p, 0) : Object.class);

            ///////////////////////////////// Map Type
            case N.MAP:
            case N.XMAP:
            case N.HASHMAP:
                return m2m(vt, (Map) o, isP ? argiType(p, 1) : Object.class);

            case N.COUPLE:
                return l2c(vt, (List) o, t, !isP);

            case N.TRIAD:
                return l2t(vt, (List) o, t, !isP);

            //Non generic List and Map types can be treated as Object    
            ///////////////////////////////// Array and Entity Bean Type
            default: {

                /**
                 * hack method: replace t instanceof TypeVariable i less than 0
                 * means not generic type Generic Array Type here
                 */
                if (!isP && name.equals(t.toString()))
                    return o2t(vt, o, vt.get(name));
                /**
                 * ? treat as object if(name.equals("?")) return o; ? extends
                 * XXX or ? super YYY has bug if(name.charAt(0)=='?') return
                 * o2t(o,((WildcardType)t).getUpperBounds()[0]);
                 */
                switch (o.getClass().getName()) {

                    /**
                     * not support type, perhaps the generic type is not
                     * specified Custom and complex type processing json value
                     * is map but toObject setMethod parameter is not map , must
                     * be customer Entity bean If the class is a custom generic,
                     * this includes a bug,bugfix 20160929 fred Weng g2e: map o
                     * to ParameterizedType entity eg:
                     * {x:'hello',y:1234,z:1234.567} ->
                     * B<String, Integer, Float>()
                     */
                    case N.XMAP:
                    case N.HASHMAP:
                    case N.MAP:
                        if (!isP)
                            return m2e((Map) o, (Class) t, name);

                        //vt == null : All type is specified types.
                        if (null == vt)
                            return g2e((Map) o, p, name);

                        /**
                         * bug 20170504: Not Support custom class generic type
                         * transfer Bug: A<T> -> B<V> ?????? new P(rT, tArgs)
                         */
                        return g2e((Map) o, PType.newP(vt, p), name);

                    /**
                     * json value is list but toObject setMethod parameter is
                     * not list , mustbe array
                     */
                    case N.XLIST:
                    case N.LIST:
                    case N.ARRAYLIST:
                        return l2a(vt, (List) o, ((Class) t).getComponentType());

                    default:
                        return o;
                }
            }
        }
    }

    private static Type argiType(ParameterizedType p, int i) {
        Type[] ts = p.getActualTypeArguments();
        if (ts.length > i)
            return ts[i];
        return Object.class;
    }
    //</editor-fold>

    /*20180801 start just for Tuple deserialize*/
    private static Couple<?, ?> l2c(Map<String, Type> vt, List l, Type t, boolean f) {
        if (f)
            return Tuple.of(
                    l.get(0),
                    l.get(1));
        Type[] ts = ((ParameterizedType) t).getActualTypeArguments();
        return Tuple.of(
                o2t(vt, l.get(0), ts[0]),
                o2t(vt, l.get(1), ts[1])
        );
    }

    private static Triad<?, ?, ?> l2t(Map<String, Type> vt, List l, Type t, boolean f) {
        if (f)
            return Tuple.of(
                    l.get(0),
                    l.get(1),
                    l.get(2)
            );
        Type[] ts = ((ParameterizedType) t).getActualTypeArguments();
        return Tuple.of(
                o2t(vt, l.get(0), ts[0]),
                o2t(vt, l.get(1), ts[1]),
                o2t(vt, l.get(2), ts[2])
        );
    }

    /*20180801 end*/
    //<editor-fold defaultstate="collapsed" desc="parallel l2x">
    /**
     * List to Array see also JSON.aL
     */
    @SuppressWarnings("unchecked")
    private static Object l2a(Map<String, Type> vt, List l, Class z) {

        if (z.isPrimitive()) {

            //just cast List to z[] , z is 8 primitive type ; 20171205
            Object[] ls = l.parallelStream().map(p -> o2t(vt, p, z)).toArray();
            int n = l.size();

            Object o = Array.newInstance(z, n);
            for (int i = 0; i < n; i++)
                Array.set(o, i, ls[i]);

            return o;
        }
        //List<Object> to List<T> ; fast cast List<T> to T[]
        return l2l(vt, l, z).toArray((Object[]) Array.newInstance(z, 0));
    }

    @SuppressWarnings("unchecked")
    private static XList l2l(Map<String, Type> vt, List l, Type t) {
        return (XList) l.parallelStream()
                .collect(XList::new,
                        (BiConsumer<XList, ?>) (x, y) -> x.add(o2t(vt, y, t)),
                        (BiConsumer<XList, XList>) XList::addAll
                );
    }

    @SuppressWarnings("unchecked")
    private static Set l2s(Map<String, Type> vt, List l, Type t) {
        return (Set) l.parallelStream()
                .collect(HashSet::new,
                        (BiConsumer<Set, ?>) (x, y) -> x.add(o2t(vt, y, t)),
                        (BiConsumer<Set, Set>) Set::addAll
                );
    }

    @SuppressWarnings("unchecked")
    private static XMap m2m(Map<String, Type> vt, Map m, Type t) {
        return (XMap) m.keySet().parallelStream()
                .collect(XMap::new,
                        (BiConsumer<XMap, ?>) (x, y) -> x.put(y, o2t(vt, m.get(y), t)),
                        (BiConsumer<XMap, XMap>) XMap::putAll
                );

    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public get">
    //////////////////////////////////// get Primitive boxed Object type
    public Character getOChar(Object key) {
        return ObjU.toOChar(get(key));
    }

    public Byte getOByte(Object key) {
        return ObjU.toOByte(get(key));
    }

    public Short getOShort(Object key) {
        return ObjU.toOShort(get(key));
    }

    public Integer getOInt(Object key) {
        return ObjU.toOInt(get(key));
    }

    public Long getOLong(Object key) {
        return ObjU.toOLong(get(key));
    }

    public Boolean getOBoolean(Object key) {
        return ObjU.toOBool(get(key));
    }

    public Float getOFloat(Object key) {
        return ObjU.toOFloat(get(key));
    }

    public Double getODouble(Object key) {
        return ObjU.toODouble(get(key));
    }

    //////////////////////////////////// get Primitive type
    public char getChar(Object key) {
        return ObjU.toChar(get(key));
    }

    public byte getByte(Object key) {
        return ObjU.toByte(get(key));
    }

    public short getShort(Object key) {
        return ObjU.toShort(get(key));
    }

    public int getInt(Object key) {
        return ObjU.toInt(get(key));
    }

    public long getLong(Object key) {
        return ObjU.toLong(get(key));
    }

    public boolean getBoolean(Object key) {
        return ObjU.toBool(get(key));
    }

    public float getFloat(Object key) {
        return ObjU.toFloat(get(key));
    }

    public double getDouble(Object key) {
        return ObjU.toDouble(get(key));
    }
    ////////////////////////////////////

    public Date getDate(Object key) {
        return ObjU.toDate(get(key));
    }

    public Instant getIntant(Object key) {
        return ObjU.toInstant(get(key));
    }

    public BigDecimal getBigDecimal(Object key) {
        return ObjU.toBigDecimal(get(key));
    }

    public String getString(Object key) {
        return ObjU.toString(get(key));
    }

    /**
     * can not return null for jquery style operation Fix the nested instance of
     * HashMap/ArrayList the XMap new (Map) into the XMap instance getXMap /
     * getXList subitem for Empty Map bug
     *
     * @param key key
     * @return xmap
     */
    public XMap getXMap(Object key) {
        Object o = this.get(key);

        if (o instanceof XMap)
            return (XMap) o;
        else if (o instanceof Map)
            return new XMap((Map) o);
        else
            return new XMap();
    }

    @SuppressWarnings("unchecked")
    public <R> XList<R> getXList(Object key) {
        Object o = get(key);

        if (o instanceof XList)
            return (XList<R>) o;

        if (o instanceof List)
            return new XList<>((List) o);

        return new XList<>();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="public from">
    public static XMap fromJSON(String json) {
        Object o = JSON.parse(json);
        if (o instanceof XMap)
            return (XMap) o;

        return new XMap();
    }

    public static XMap fromXML(String xml) {
        Object o = XML.parse(xml);
        if (o instanceof XMap)
            return (XMap) o;

        return new XMap();
    }

    public static final XMap of(Object... params) {
        return new XMap(params);
    }

    public static final XMap fromArray(Object[] params) {
        return new XMap(params);
    }
    //</editor-fold>

    private static class VFty {

        /**
         * one different generic class parameter actual type define one
         * relationship of parameters and actual type the different actual type
         * define order also represents a different relationship
         */
        private static final ConcurrentHashMap<String, Map<String, Type>> G_T = new ConcurrentHashMap<>();

        /**
         * get VT from cache
         *
         * @param t
         * @param c
         * @return
         */
        public static Map<String, Type> getVT(ParameterizedType t, Class c) {
            // why not to use sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl.getTypeName()
            // PType.typeName is Easy to follow
            return Cache.lazy(PType.typeName(t), G_T, () -> pvt(t, c));
        }

        /**
         * get Relationship of TypeVariable and Actual Type Arguments
         *
         * @param p
         * @param c
         * @return
         */
        @SuppressWarnings("unchecked")
        private static Map<String, Type> pvt(ParameterizedType p, Class c) {

            Type[] t = p.getActualTypeArguments();
            TypeVariable<Class>[] v = c.getTypeParameters();

            Map<String, Type> m = new HashMap<>();
            for (int i = t.length - 1; i >= 0; i--)
                m.put(v[i].getTypeName(), t[i]);

            return m;
        }

    }

    //<editor-fold defaultstate="collapsed" desc="implements IX">
    @Override
    public boolean isXMap() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public XList toXList() {
        return new XList(this.values());
    }
    //</editor-fold>

}

//<editor-fold defaultstate="collapsed" desc="MI">
/**
 * java Bean Method Information of a Class One class define one MethodInfo all
 * set and get method info of a class refer from XMap.m2e g2e getV in this file
 * and JSON.e2j in JSON.java
 *
 * @author fred weng
 */
class MI {

    List<M> setM;
    List<M> getM;
    private Class clazz;
    private Constructor cS = null;//add@20171205,change@20180818: lazy get

    @SuppressWarnings("unchecked")
    Constructor ctor() {

        if (cS == null)
            try {
                cS = clazz.getDeclaredConstructor();
                cS.setAccessible(true);
            } catch (NoSuchMethodException e) {
                LogU.error(e.getMessage());
            }

        return cS;
    }

    MI(List<M> s, List<M> g, Class c) {
        this.setM = s;
        this.getM = g;
        //20171205 caching constructor method 
        this.clazz = c; //20180818
    }

    //get MI of class
    private static final ConcurrentHashMap<String, MI> M_C = new ConcurrentHashMap<>();

    static MI getMI(Class c, String n) {
        return Cache.lazy(n, M_C, () -> mic(c, n));
    }

    @SuppressWarnings("unchecked")
    private static MI mic(Class c, String cN) {

        //field valid annotation array map
        HashMap<String, Annotation[]> map = new HashMap<>();
        //set and get method list
        ArrayList<M> s = new ArrayList<>(), g = new ArrayList<>();

        //initial valid annotation field name mapping
        if (c.getAnnotation(j.v.Valid.class) != null)
            for (Class i = c; i != Object.class; i = i.getSuperclass())
                for (Field k : i.getDeclaredFields()) {
                    Annotation[] a = k.getAnnotations();
                    //20161125 fixed bug:get field from parent after child,in contrast, the parent covers the child
                    if (a != null && !map.containsKey(k.getName()))
                        map.put(k.getName(), a);
                }

        String n;//method name
        int p;//parameter count
        for (Method m : c.getMethods()) {
            n = m.getName();
            p = m.getParameterCount();
            if (p == 0 && n.startsWith("get") && !"getClass".equals(n))
                g.add(new M(firstCharLower(n.substring(3)), m));
            else if (p == 0 && n.startsWith("is"))
                g.add(new M(firstCharLower(n.substring(2)), m));
            else if (p == 1 && n.startsWith("set"))
                s.add(new M(m, firstCharLower(n.substring(3)), map, cN));
        }

        return new MI(s, g, c);
    }

    /**
     * first letter fast toLowerCase only refer from MI.mic method in this file
     * see also StrU.firstCharUpper
     *
     * @param n
     * @return
     */
    private static String firstCharLower(String n) {

        char[] cs = n.toCharArray();
        // fixed 20161118 online bug,uppcase to lowercase , not uppercase unchanged
        if (cs[0] >= 'A' && cs[0] <= 'Z')
            cs[0] += 32;
        return new String(cs);
    }

    //refer from XMap
    static class M {

        /**
         * one method of a class infomation refer from XMap field name , get
         * from set or get method substring and first letter to lowcase eg
         * setName -> name; setpType-> pType setPName -> pName
         */
        public String name;
        //method
        public Method method;
        //The first and only one Parameter type
        public Type pT;
        //The type name of pT
        public String pN;
        //name has equals field
        public boolean uF = false;
        //Method associated field validator function list
        public List<IValid> iVs = null;

        /**
         * get or is method info
         *
         * @param n get method bean style name
         * @param m setMethod
         */
        public M(String n, Method m) {
            this.name = n;
            this.method = m;
            //20171205
            this.method.setAccessible(true);
        }

        /**
         * set method info
         *
         * @param m setMethod
         * @param n set method bean style name
         * @param v class method annotation
         */
        public M(Method m, String n, HashMap<String, Annotation[]> v, String cN) {

            this(n, m);
            this.pT = m.getParameters()[0].getParameterizedType();
            this.pN = this.pT.getTypeName();

            Annotation[] a = v.get(n);
            if (a != null)
                this.uF = true;
            else if (!this.uF) {
                String s = StrU.firstCharUpper(n);
                if ((a = v.get(s)) != null) {
                    this.uF = true;
                    this.name = s;
                }
            }

            this.iVs = vS(a, name, pN, cN);
        }

        /**
         * get field associated valid annotation list
         *
         * @param annos field associated annotation array
         * @param n field name
         * @param pN type name
         * @param cN class name
         * @return field valid list
         */
        private static List<IValid> vS(Annotation[] annos, String n, String pN, String cN) {

            if (null == annos)
                return null;

            List<IValid> iVs = new ArrayList<>();
            for (Annotation an : annos)
                switch (an.annotationType().getName()) {
                    case N.ANNO_REQUIRE:
                        iVs.add(new RqV((Require) an, n, cN));
                        break;
                    case N.ANNO_REGEXP:
                        iVs.add(new RegExpV((RegExp) an, n, cN));
                        break;
                    case N.ANNO_RANGE:
                        iVs.add(new RangeV((Range) an, n, pN, cN));
                        break;
                    default://skip non-valid annotation,but vS method return empty(not null) list
                        break;
                }
            return iVs;
        }

    }

    //refer from XMap toObject valid method
    static interface IValid {

        void valid(Object o);
    }

    private static class RqV implements IValid {

        //Require Valid class
        private final Require r;
        private final String n;
        private final String cN;
        //exp.A.pType's value can not be null.
        private static final String F = "{0}.{1}'s value can not be null.";

        public RqV(Require require, String name, String cN) {
            this.r = require;
            this.n = name;
            this.cN = cN;
        }

        @Override
        public void valid(Object o) {
            if (null == o)
                throw new ValidException(r.value().equals("") ? StrU.fastFormat(F, this.cN, n) : r.value());
        }
    }

    private static class RegExpV implements IValid {

        //Regular Express Valid class
        private final RegExp e;
        private final String n;
        private final Pattern p;
        private final String cN;
        //exp.A.code's value:'12' mismatched regular express:'\d{3}'
        private static final String F = "{0}.{1}'s value:'{2}' mismatched regular express:'{3}'";

        public RegExpV(RegExp exp, String name, String cN) {
            this.e = exp;
            this.n = name;
            this.cN = cN;
            this.p = Pattern.compile(e.value());
        }

        @Override
        public void valid(Object o) {
            if (o != null && !p.matcher(o.toString()).matches())
                throw new ValidException(e.desc().equals("") ? StrU.fastFormat(F, cN, n, o, e.value()) : e.desc());
        }

    }

    private static class RangeV implements IValid {

        //Range Valid Class
        private final Range r;
        private final RangeF rF;
        private final String n;
        private final String cN;

        //(or[number(can omit),number(can omit)]or)
        private static final Pattern P = Pattern.compile("^(\\[|\\()((\\+|\\-)?(\\d*\\.)?\\d+)?,((\\+|\\-)?(\\d*\\.)?\\d+)?(\\]|\\))$");

        /**
         * Range Valid class constuctor
         *
         * @param range Range class instance
         * @param name method name
         * @param pN type name
         * @param cN class name
         */
        public RangeV(Range range, String name, String pN, String cN) {
            Matcher m = P.matcher(range.value());
            if (!m.find())
                throw new ValidException(StrU.fastFormat("{0}.{1}'s Range annotation value syntax error:\"{2}\"", cN, name, range.value()));

            this.r = range;
            this.n = name;
            this.cN = cN;
            this.rF = RangeF.rangeF(pN, new Interval(m.group(1), m.group(2), m.group(5), m.group(8), -Double.MAX_VALUE, Double.MAX_VALUE));
        }

        @Override
        public void valid(Object o) {
            if (o != null && !rF.valid(o)) //exp.A.code's value:'400' not in range:'[100,300]'
                throw new ValidException(r.desc().equals("") ? StrU.fastFormat("{0}.{1}'s value:'{2}' not in range:'{3}'", cN, n, o, r.value()) : r.desc());
        }

        private static class Interval {

            private DoublePredicate between;

            /**
             * (a,b) [a,b) (a,b] [a,b]
             *
             * @param l left bracket
             * @param x
             * @param y
             * @param r right bracket
             */
            public Interval(String l, String x, String y, String r, double min, double max) {
                initGL("[".equals(l),
                        x == null ? min : Double.parseDouble(x),
                        y == null ? max : Double.parseDouble(y),
                        "]".equals(r));
            }

            private void initGL(boolean l, double x, double y, boolean r) {
                DoublePredicate great = l ? z -> z >= x : z -> z > x;
                DoublePredicate less = r ? z -> z <= y : z -> z < y;
                between = great.and(less);
            }

            public boolean in(double x) {
                return between.test(x);
            }
        }

        //Abstract Range
        private static abstract class RangeF {

            protected Interval i;

            public RangeF init(Interval i) {
                this.i = i;
                return this;
            }

            public abstract boolean valid(Object x);

            public static RangeF rangeF(String pN, Interval i) {

                int x = pN.indexOf('<');
                if (x > 0)
                    pN = pN.substring(0, x);

                switch (NCat.cat(pN)) {

                    case NCat.CAT_INT:
                        return new IRangeF().init(i);
                    case NCat.CAT_DOU:
                        return new DRangeF().init(i);
                    case NCat.CAT_MAP:
                        return new MRangeF().init(i);
                    case NCat.CAT_LST:
                        return new CRangeF().init(i);
                    case NCat.CAT_STR:
                        return new SRangeF().init(i);
                    case NCat.CAT_NA:
                    default:
                        return new URangeF().init(i);
                }
            }
        }

        //Integer Range
        private static class IRangeF extends RangeF {

            @Override
            public boolean valid(Object x) {
                return i.in(((Number) x).intValue());
            }
        }

        //Double Range
        private static class DRangeF extends RangeF {

            @Override
            public boolean valid(Object x) {
                return i.in(((Number) x).doubleValue());
            }
        }

        //String Range
        private static class SRangeF extends RangeF {

            @Override
            public boolean valid(Object x) {
                return i.in(x.toString().length());
            }
        }

        //Map Range
        private static class MRangeF extends RangeF {

            @Override
            public boolean valid(Object x) {
                return i.in(((Map) x).size());
            }
        }

        //Collection Range
        private static class CRangeF extends RangeF {

            @Override
            public boolean valid(Object x) {
                return i.in(((Collection) x).size());
            }
        }

        //Unknow Type Range for generic type range
        private static class URangeF extends RangeF {

            @Override
            public boolean valid(Object x) {

                switch (NCat.cat(x.getClass().getName())) {
                    case NCat.CAT_INT:
                        return i.in(((Number) x).intValue());
                    case NCat.CAT_DOU:
                        return i.in(((Number) x).doubleValue());
                    case NCat.CAT_MAP:
                        return i.in(((Map) x).size());
                    case NCat.CAT_LST:
                        return i.in(((Collection) x).size());
                    case NCat.CAT_STR:
                        return i.in(x.toString().length());
                    case NCat.CAT_NA:
                    default:
                        LogU.error("Warning: range not defined for " + x.getClass().getName() + ",ignore this validation.");
                        return true;
                }
            }
        }

        /**
         * type classification just for range : RangeF and URangeF
         */
        private static class NCat {

            static final int CAT_INT = 90001;
            static final int CAT_DOU = 90010;
            static final int CAT_MAP = 90020;
            static final int CAT_LST = 90030;
            static final int CAT_STR = 90040;
            static final int CAT_NA = 100000;

            /**
             * name of type category for MI.rangeF
             *
             * @param typeName
             * @return
             */
            static int cat(String typeName) {

                switch (typeName) {
                    case N.BYTE:
                    case N.SHORT:
                    case N.INTEGER:
                    case N.LONG:
                    case N.TYPE_BYTE:
                    case N.TYPE_SHORT:
                    case N.TYPE_INT:
                    case N.TYPE_LONG:
                    case N.BIG_INTEGER:
                        return CAT_INT;

                    case N.FLOAT:
                    case N.DOUBLE:
                    case N.TYPE_FLOAT:
                    case N.TYPE_DOUBLE:
                    case N.BIG_DECIMAL:
                        return CAT_DOU;

                    case N.MAP:
                    case N.XMAP:
                    case N.HASHMAP:
                    case N.HASHTABLE:
                        return CAT_MAP;

                    case N.LIST:
                    case N.XLIST:
                    case N.ARRAYLIST:
                    case N.LINKED_LIST:
                    case N.VECTOR:
                    case N.SET:
                    case N.HASHSET:
                        return CAT_LST;

                    case N.STRING:
                        return CAT_STR;

                    default:
                        return CAT_NA;
                }
            }
        }
    }

}
//</editor-fold>

//</editor-fold>
