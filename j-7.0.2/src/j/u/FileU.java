//<editor-fold>
package j.u;

import j.Env;
import java.io.*;

/**
 * File Util Class
 * @author wengmj
 */
public class FileU {

    public static String readFile(String path) {
        return read(new File(path));
    }

    public static boolean write(String path, String content, boolean append) {
        return write(new File(path), content, append);
    }

    public static String read(File f) {
        try {
            return readEnd(new FileReader(f));
        } catch (IOException e) {
            LogU.error(e.getMessage());
            return null;
        }
    }

    public static String readEnd(Reader reader) throws IOException {
        
        String s;
        
        try (BufferedReader br = new BufferedReader(reader)) {
            if ((s = br.readLine()) != null) {
                StringBuilder sb = new StringBuilder(s);
                while ((s = br.readLine()) != null)
                    sb.append(Env.LINE_SEP_OS).append(s);
                s = sb.toString();
            }
        }
        
        return s;
    }
    
    public static boolean write(File f, String content, boolean append) {
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, append))) {bw.write(content);} catch (Exception e) {
            LogU.error(e.getMessage());
            return false;
        }
        
        return true;
    }
}
//</editor-fold>
