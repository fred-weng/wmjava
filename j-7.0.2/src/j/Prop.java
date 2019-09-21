//<editor-fold>
package j;

import j.u.LogU;
import j.u.ObjU;
import j.u.StrU;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author wengmj
 */
public class Prop {

    private final Properties p = new Properties();

    public Prop(String name) {
        InputStream is = Env.getResourceAsStream(name);
        if (null == is)
            LogU.warning(StrU.fastFormat("{0}ERROR: {1} not found in classpath Resource,Use Empty Properties", Env.LINE_SEP_OS, name));
        else
            try {
                p.load(is);
            } catch (IOException e) {
                LogU.error(e.getMessage());
            }
    }

    public String getString(String key) {
        return p.getProperty(key);
    }

    public int getInt(String key) {
        return ObjU.toInt(p.getProperty(key));
    }

    private static Prop PROP = null;

    public static Prop getDefault(){
        
        if(PROP == null)
            PROP = new Prop("prop.properties");
        return PROP;
    }
}
//</editor-fold>
