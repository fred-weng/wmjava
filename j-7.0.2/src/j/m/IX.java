//<editor-fold>
/**
 * @author wengmj interface for XMap and XList
 */
package j.m;

import j.u.ObjU;
import java.util.Date;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

public interface IX {

    default public String toJSON() {
        return JSON.toJSON(this);
    }

    default public String toXML() {
    /**
     * only serialize XMap/XList or nested structure to xml XMap/XList included
     * any object to xml use XML.toXML(this) 2017-09-20
     *
     * @return XML String
     */
        return XML.to_XML(this);
    }

    default public String toPrettyJSON() {
        return JSON.toPrettyJSON(this);
    }

    default public String toPrettyXML() {
    /**
     * only serialize XMap/XList or nested structure to pretty xml
     * XML.toPrettyXML
     *
     * @return pretty XML String
     */
        return XML.to_PrettyXML(this);
    }

    default public Object path(String path) {
        return XPath.path(this, path);
    }

    default public String pathString(String path) {
        return ObjU.toString(path(path));
    }

    default public int pathInt(String path) {
        return ObjU.toInt(path(path));
    }

    default public long pathLong(String path) {
        return ObjU.toLong(path(path));
    }

    default public boolean pathBool(String path) {
        return ObjU.toBool(path(path));
    }

    default public float pathFloat(String path) {
        return ObjU.toFloat(path(path));
    }

    default public Date pathDate(String path) {
        return ObjU.toDate(path(path));
    }

    default public XList pathXList(String path) {

        Object o = path(path);
        if (o instanceof XList)
            return (XList) o;
        return new XList();
    }

    default public XMap pathXMap(String path) {
        Object o = path(path);
        if (o instanceof XMap)
            return (XMap) o;
        return new XMap();
    }

    default boolean isXList() {
        return false;
    }

    default boolean isXMap() {
        return false;
    }

    default public XMap toXMap() {
        return (XMap) this;
    }

    default public XList toXList() {
        return (XList) this;
    }

    default public <T> T toObject(ParameterizedType pType) {
     /**
     * create ParameterizedType by IX.pT(Type
     * rawType,Type...actualTypeArguments);
     *
     * @param <T> generic return type class
     * @param pType if pType instanceof Type use Class
     * @return type object
     */
        return toType(this, pType);
    }

    @SuppressWarnings("unchecked")
    public static <T> T toType(Object o, Type t) {
        //for hide XMap.o2t method , only j.w.WM use it
        try {
            return (T) XMap.o2t(null, o, t);
        } catch (Throwable e) {
            //type convert failed
            return null;
        }
    }
}
//</editor-fold>
