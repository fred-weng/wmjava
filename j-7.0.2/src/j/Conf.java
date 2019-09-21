//<editor-fold>
/**
 * The class is used to get the info from conf.xml in class path.
 *
 * @author wengmj
 */
package j;

import j.m.XList;
import j.m.XMap;

public class Conf {

    private final XMap d;

    //LogU initialization depends on initialization of Conf , can't use LogU here.
    public Conf(String name) {
        //no name conf return Empty XMap
        d = XMap.fromXML(Env.getResourceAsString(name));
    }

    //not found return null
    public Object path(String path) {
        return d.path(path);
    }

    public String pathString(String path, String defVal) {
        String s = d.pathString(path);
        return s == null ? defVal : s;
    }

    public int pathInt(String path, int defVal) {
        int v = d.pathInt(path);
        return v == 0 ? defVal : v;
    }

    /**
     * for no conf.xml
     *
     * @param path
     * @return
     */
    public XMap pathXMap(String path) {

        return d.pathXMap(path);

    }

    public XList pathXList(String path) {

        return d.pathXList(path);
    }

    private static Conf CONF = null;

    public static Conf getDefault() {
        if (null == CONF)
            CONF = new Conf("conf.xml"); //20180806 change to lazy singleton
        return CONF;
    }
}
//</editor-fold>
