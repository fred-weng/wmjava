//<editor-fold>
package j.c;

import j.u.FileU;
import java.io.File;

/**
 * The Cache dependent files updated. A few (getDuration) seconds to read a
 * dependent file lastModified time, if the file update reread it's content
 * again
 *
 * @author Fred Weng
 *
 */
public class FileCache {

    private long p = -1L;
    protected final File dFile;
    private final SingleCache<Long> sCache;
    private String cache;

    public FileCache(long duration, String fp) {
        this.dFile = new File(fp);
        this.sCache = new SingleCache<>(duration);
    }

    public FileCache(String fp) {
        this(5000, fp);
    }

    private long getlastModified() {
        Long s = sCache.get();
        if (null == s) {
            s = dFile.lastModified();
            sCache.set(s);
        }
        return s;
    }

    public String getCache() {
        if (null == dFile)
            throw new RuntimeException("cache dependence file not found!");
        long c = getlastModified();
        if (p < c) {
            cache = FileU.read(dFile);
            p = c;
        }
        return cache;
    }
}
//</editor-fold>