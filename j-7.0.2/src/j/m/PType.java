//<editor-fold>
package j.m;

import java.util.Map;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

public class PType implements ParameterizedType {

    private final Type r;
    private final Type[] a;

    private PType(Type rawType, Type... typeArgs) {
        this.r = rawType;
        this.a = typeArgs;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return this.a;
    }

    @Override
    public Type getRawType() {
        return this.r;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public String getTypeName() {
        return typeName(this);
    }

    static String typeName(Type t) {

        StringBuilder sb = new StringBuilder();
        typeS(t, sb, "<", ">", true);
        return sb.toString();
    }


    /**
     * Author : Fred Weng at 20180813 15:50 fixed bug: other ParameterizedType
     * implement getTypeName also for WM.typeS to create method description in WS HTML page
     * 
     * @param t Type or ParameterizedType
     * @param s StringBuilder
     * @param l if HTML use &lt;
     * @param r if HTML use &gt;
     * @param f getName or getSimpleName
     */
    public static void typeS(Type t, StringBuilder s, String l, String r, boolean f) {

        if (t instanceof ParameterizedType) {

            ParameterizedType p = (ParameterizedType) t;

            //RawType
            typeS(p.getRawType(), s, l, r, f);

            //argsType
            s.append(l);
            Type[] ts = p.getActualTypeArguments();
            int n = ts.length;
            typeS(ts[0], s, l, r, f);
            for (int i = 1; i < n; i++) {
                s.append(", ");
                typeS(ts[i], s, l, r, f);
            }
            s.append(r);

        } else
            s.append(f ? ((Class) t).getName() : ((Class) t).getSimpleName());
    }

    /**
     * Author : Fred Weng at 20170504 22:25 , for support custom class generic
     * type transfer ParameterizedType :
     * sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
     * p.getActualTypeArguments() == p.actualTypeArguments.clone(); The reverse
     * traversal can avoid repeated calculation of the length of the type array
     * ps private for only the owner class XMap
     *
     * @param m
     * @param p
     * @return
     */
    static PType newP(Map<String, Type> m, ParameterizedType p) {
        Type t, ps[] = p.getActualTypeArguments();

        for (int i = ps.length - 1; i >= 0; i--)
            //mixing parametered and specific types
            if ((t = m.get(ps[i].getTypeName())) != null)
                ps[i] = t;
        return new PType(p.getRawType(), ps);
    }

    /**
     * Author : Fred Weng at 20180803 11:47 holiday@luo'hu baoshan shanghai just
     * for user create complex ParameterizedType simply.
     *
     * @param rType
     * @param tArgs
     * @return
     */
    public static ParameterizedType compose(Type rType, Type... tArgs) {
        return new PType(rType, tArgs);
    }
}
//</editor-fold>
