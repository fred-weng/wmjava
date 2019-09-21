//<editor-fold>
package j.c;

import j.u.StrU;
import java.util.Map;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentHashMap;


/**
 * FIFO Cache
 *
 * @author Fred Weng
 * @param <K> key type
 * @param <V> value type
 */
public class Cache<K, V> {

    private Node head = new Node();
    private Node tail = new Node();
    private Map<K, Node> map = new ConcurrentHashMap<>();
    int z = 0, c = 0, d = 0;

    public Cache(int capacity, int duration) {
        c = capacity <= 0 ? 1024 : capacity;
        d = duration;
        head.next = tail;
        tail.prev = head;
    }

    /**
     * node duration not set ,default duration is Cache duration
     *
     * @param key key 
     * @param value value
     */
    public final V put(K key, V value) {
        this.put(key, value, d);
        return value;
    }

    /**
     * negative duration indicate unlimit duration
     *
     * @param key key
     * @param value value
     * @param duration duration
     */
    public synchronized final V put(K key, V value, int duration) {
        Node n;
        if ((n = map.get(key)) != null) {
            n.load(value);
            discon(n);
            prefix(n);
            return value;
        }

        if (z < c) {
            n = new Node(key, value, duration);
            map.put(key, n);
            z++;
            prefix(n);
        } else {
            n = tail.prev;
            map.remove(n.key);
            n.load(key, value);
            map.put(key, n);
            discon(n);
            prefix(n);
        }
        return value;
    }

    /**
    *  get value of special key, if timeout move current node to tail
    *  @param key 
    *  @return 
    */
    public synchronized final V get(K key) {
        Node n;

        if ((n = map.get(key)) == null)
            return null;

        if (n.d < 0 || n.d + n.s > tick())
            return n.value;

        discon(n);
        suffix(n);
        return null;
    }

    private void discon(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    private void prefix(Node n) {
        if (head.next == n)
            return;

        n.next = head.next;
        n.prev = head;
        n.next.prev = n;
        n.prev.next = n;
    }

    private void suffix(Node n) {
        if (tail.prev == n)
            return;
        n.prev = tail.prev;
        n.next = tail;
        n.prev.next = n;
        n.next.prev = n;
    }
    

    class Node {

        Node() {
        }

        Node(K k, V v, int d) {
            this.key = k;
            this.value = v;
            this.s = tick();
            this.d = d;
        }

        void load(K k, V v) {
            this.key = k;
            this.value = v;
            this.s = tick();
        }

        void load(V v) {
            this.value = v;
            this.s = tick();
        }
        Node prev, next;
        K key;
        V value;
        long s;
        int d;
    }

    
    static long tick() {
        return System.currentTimeMillis();
    }

    
    /**
    * return the previous value associated with key,or null if there was no mapping for key.
    * @param <K> key type
    * @param <V> value type
    * @param key key name
    * @param cache 
    * @param supplier
    * @return 
    */
    public static <K, V> V lazy(K key, Map<K, V> cache, Supplier<V> supplier) {
        V r = cache.get(key);
        if (null == r) {
            r = supplier.get();
            //map class put method return the last value of the current key
            cache.put(key, r);  
        }
        return r;
    }

    
    /**
        * the custom class Cache put method return the value of current putting
        * @param key
        * @param supplier 
        * @return cache value of key
    */
    public V lazy(K key, Supplier<V> supplier) {
        
        //return lazy(key,this,supplier);
        
        V r = this.get(key);
        //the custome cache class put method return the value just inserted 
        return r == null ? this.put(key, supplier.get()) : r; 
        
        
    }

    
    /**
    * visit all items of the Cache
    * @return 
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(StrU.fastFormat("head(capacity={0},duration={1},size={2})->", this.c, this.d, this.z));
        Node curr = this.head.next;
        while (curr != this.tail) {
            sb.append(StrU.fastFormat("({0},{1})", curr.key, curr.value)).append("->");
            curr = curr.next;
        }
        sb.append("tail");
        return sb.toString();
    }

}
//</editor-fold>
