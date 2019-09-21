//<editor-fold>
/**
 * The class is used to get the OS and JRE environment variables and context resources.
 *
 * @author wengmj
 */
package j;

import j.u.FileU;
import j.u.LogU;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class Env {

    private static final Class ENV_CLS = Env.class;
    private static final ClassLoader ENV_CLS_LDER = ENV_CLS.getClassLoader();
    private static final URL RES_ROOT_URL = ENV_CLS.getResource("/");

    public static final String RES_ROOT = RES_ROOT_URL == null ? null : RES_ROOT_URL.getPath();
    public static final String LINE_SEP_OS = System.getProperty("line.separator");
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String FILE_SEP_OS = System.getProperty("file.separator");

    public static final String getResourceAsString(String fileName) {
        InputStream is = getResourceAsStream(fileName);
        if (null == is) {
            return null;
        }

        try {
            return FileU.readEnd(new InputStreamReader(is));
        } catch (IOException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static final InputStream getResourceAsStream(String fileName) {
        return ENV_CLS_LDER.getResourceAsStream(fileName);
    }

    public static final URL getResource(String fileName) {
        return ENV_CLS_LDER.getResource(fileName);
    }
}
//</editor-fold>
