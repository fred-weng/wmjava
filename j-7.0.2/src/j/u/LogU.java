package j.u;

//<editor-fold>
import j.Conf;
import j.Env;
import java.io.*;

public class LogU {

    //<editor-fold defaultstate="collapsed" desc="ctor">
    private static final Conf conf = Conf.getDefault();
    private static final int level = Level.of(conf.pathString("{u}{log}{level}", "WARNING"));
    private static final String path = conf.pathString("{u}{log}{path}", Env.USER_HOME).concat(Env.FILE_SEP_OS);

    private static void write(String f, String m) {

        XDate now = XDate.getNow();
        String d = now.toString("yyyyMMdd");
        String msg = StrU.fastFormat("{0}\t{1}:{2}{3}", now.toString("HH:mm:ss"), f, m, Env.LINE_SEP_OS);

        try (FileWriter pw = new FileWriter(path.concat(d).concat(".txt"), true)) {
            System.err.print(msg);
            pw.write(msg);
        } catch (IOException e) {
            System.err.print(e.getMessage());
        }
    }

    private static class Level {

        static final int ALL = 0;
        static final int INFO = 800;
        static final int WARNING = 900;
        static final int ERROR = 1000;
        static final int OFF = 10000;

        static final String S_INFO = "INFO";
        static final String S_WARNING = "WARNING";
        static final String S_ERROR = "ERROR";

        static int of(String v) {
            if (v == null)
                return Level.ALL;
            switch (v) {
                case "OFF":
                    return Level.OFF;
                case "ERROR":
                    return Level.ERROR;
                case "WARNING":
                    return Level.WARNING;
                case "INFO":
                    return Level.INFO;
                default:
                    return Level.ALL;
            }
        }
    }
    //</editor-fold>

    public static boolean info(String message) {
        if (level > Level.INFO)
            return false;
        write(Level.S_INFO, message);
        return true;
    }

    public static boolean warning(String message) {
        if (level > Level.WARNING)
            return false;
        write(Level.S_WARNING, message);
        return true;
    }

    public static boolean error(String message) {
        if (level > Level.ERROR)
            return false;
        write(Level.S_ERROR, message);
        return true;
    }

    public static boolean error(Exception e) {
        if (level > Level.ERROR)
            return false;

        StringWriter s = new StringWriter(); 
        e.printStackTrace(new PrintWriter(s));  
        write(Level.S_ERROR, s.toString());
        return true;
    }
}
//</editor-fold>
