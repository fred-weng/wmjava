//<editor-fold>
package j.w;

import j.Env;
import j.m.JSON;
import j.u.LogU;
import j.u.ObjU;
import j.u.StrU;
import java.io.*;
import java.lang.reflect.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WsServer {

    public static void start(int port) throws IOException {

        System.out.println("j-7.0.2 ws server is listening on port: " + port);
        ExecutorService exec = Executors.newCachedThreadPool();
        ServerSocket server = new ServerSocket(port);
        while (true)
            exec.execute(new WsHandler(server.accept()));
    }

    public static void start() throws Exception {

        start(80);
    }
}

class WsHandler implements Runnable {

    private final Socket socket;

    public WsHandler(Socket socket) throws IOException {
        this.socket = socket;
    }

    @Override
    public void run() {

        BufferedReader br = null;
        PrintWriter out = null;

        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String s = br.readLine();
            if (s == null)
                return;

            String[] a = StrU.split(s, ' ');//{method,url,data}
            if (a.length != 3)
                return;

            int i = 0;//content-length or ? index
            switch (a[0]) {
                case "POST":
                    while (!"".equals(s = br.readLine()))
                        if (s.startsWith("Content-Length:"))
                            i = ObjU.toInt(s.substring(16));
                    if (i > 0) {
                        char[] cs = new char[i];
                        br.read(cs);//read content-length
                        a[2] = new String(cs);
                    }
                    break;
                case "GET":
                    i = a[1].indexOf('?');
                    if (i > 0) {
                        a[2] = a[1].substring(i + 1);//query
                        a[1] = a[1].substring(0, i);//no query url
                    } else
                        a[2] = null;//no query
                    break;
                default:
                    //not support other http protocal
                    return;
            }

            LogU.info(StrU.fastFormat("{0}\t{1}\t{2}", a[0], a[1], a[2]));

            if ("/".equals(a[1]))
                s = INDEX_HTML;
            else if (a[1].endsWith("/"))
                if (WS_MAP.containsKey(a[1]))
                    s = WS_MAP.get(a[1]).html;
                else
                    s = a[1] + " is not found!";
            else if (REQ_MAP.containsKey(a[1]))
                try {
                    s = JSON.toJSON(REQ_MAP.get(a[1]).cal(a[0], a[2]));
                } catch (InvocationTargetException e) {
                    s = "WsException:" + e.getTargetException().getMessage();
                } catch (ReflectiveOperationException | IOException e) {
                    s = "WsException:" + e.getMessage();//it will hardly appear
                }
            else
                s = a[1] + " is not found!";             //it will hardly appear

            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK").append(Env.LINE_SEP_OS)
                    .append("Content-Type:text/html;charset=utf-8").append(Env.LINE_SEP_OS)
                    .append("Content-Length: ").append(s.length()).append(Env.LINE_SEP_OS)
                    .append(Env.LINE_SEP_OS) //empty line
                    .append(s);

            out = new PrintWriter(socket.getOutputStream());
            out.print(sb.toString());

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (out != null)
                    out.close();
                if (br != null)
                    br.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="static init">
    private static String INDEX_HTML;
    private static int ROOT_LEN = 0;
    private static final char FILE_SEP = Env.FILE_SEP_OS.charAt(0);

    private static final HashMap<String, MI> REQ_MAP = new HashMap<>();
    private static final TreeMap<String, WS_Inf> WS_MAP = new TreeMap<>();

    static {
        init();
    }

    public static void init() {

        StackTraceElement[] s = Thread.currentThread().getStackTrace();

        try {
            String p = Class.forName(s[s.length - 1].getClassName()).getProtectionDomain().getCodeSource().getLocation().getFile();
            ROOT_LEN = p.length() - 1;

            if (p.endsWith(".jar"))
                loadJ(p);
            else
                loadF(new File(p));

            if (REQ_MAP.isEmpty())
                loadW(j.w.ExpWS.class);
        } catch (ReflectiveOperationException | IOException e) {
            System.out.println(e.getMessage());
        }

        appHTML();
    }

    private static void loadJ(String p) throws ReflectiveOperationException, IOException {
        Enumeration<JarEntry> e = new JarFile(p).entries();
        while (e.hasMoreElements()) {
            p = e.nextElement().getName();
            if (p.endsWith(".class"))
                loadW(Class.forName(p.substring(0, p.lastIndexOf('.')).replace('/', '.')));
        }
    }

    private static void loadF(File f) throws ReflectiveOperationException {
        /**
         * f.getPath : class file name. eg: api.a.aa.A.class cn: class full
         * name. eg: api.a.aa.A dir eg:
         * /C:/Users/weng.mingjun/apps/exp/build/classes/
         *
         * @param f
         * @param len
         * @throws ServletException
         */
        if (f.isDirectory())
            for (File i : f.listFiles())
                loadF(i);
        else {
            String p = f.getPath();
            if (p.endsWith(".class")) {
                p = p.substring(ROOT_LEN).replace(FILE_SEP, '.');
                loadW(Class.forName(p.substring(0, p.lastIndexOf('.'))));
            }
        }
    }

    private static void appHTML() {
        //only root app CTX_PATH == ""
        INDEX_HTML = HTML.build(
                () -> "/",//<title>
                () -> "/",//H2 topic
                () -> "", //app description no set or class description
                () -> {
                    StringBuilder s = new StringBuilder("<ol>");
                    //class list
                    WS_MAP.forEach((k, v) -> s.append(
                    StrU.fastFormat("<li><a href='{0}'>{1}</a> {2}</li>",
                            k,
                            v.value.substring(1),
                            v.desc.equals("") ? "" : StrU.fastFormat("<span class='comment'>//{0}</span>", v.desc)
                    )));
                    s.append("</ol>");
                    return s.toString();
                }
        );
    }

    private static void loadW(Class wsClazz) throws ReflectiveOperationException {

        //load web service class and its method information
        WS ws = (WS) wsClazz.getAnnotation(WS.class);
        //no @WS annotation
        if (ws == null)
            return;

        WS_Inf wsi = new WS_Inf(ws, wsClazz);
        //create WS class instance
        Constructor cs = wsClazz.getDeclaredConstructor();
        //the public modifier of the class is not necessary
        cs.setAccessible(true);
        Object o = cs.newInstance();

        for (Method m : wsClazz.getDeclaredMethods()) {
            WM wm = m.getAnnotation(WM.class);
            if (wm == null)
                continue;

            Wm_Inf wmi = new Wm_Inf(wm, m);
            String p = wsi.path.concat(wmi.value);

            if (REQ_MAP.containsKey(p))
                throw new ReflectiveOperationException(StrU.fastFormat("@WM value '{0}' is duplicated!", p));

            wsi.add(wmi);
            //for do url Request
            REQ_MAP.put(p, new MI(m, o));
        }

        wsi.wsHTML();

        //only for index HTML
        if (WS_MAP.containsKey(wsi.path))
            throw new ReflectiveOperationException(StrU.fastFormat("@WS value '{0}' is duplicated!", wsi.path));

        WS_MAP.put(wsi.path, wsi);
    }
    //</editor-fold>
}

//</editor-fold>
