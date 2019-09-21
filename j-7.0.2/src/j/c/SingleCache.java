//<editor-fold>
package j.c;

public class SingleCache<T> {

    private T n;
    private long s;
    private final long d;

    public SingleCache(long millis) {
        d = millis;
    }

    public void set(T content) {
        n = content;
        s = Cache.tick();
    }

    public T get() {
        if (s + d < Cache.tick())
            n = null;
        return n;
    }
}
//</editor-fold>