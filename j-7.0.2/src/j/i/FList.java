//<editor-fold desc="immutable collection">
package j.i;

import j.m.JSON;
import java.util.Comparator;
import java.util.function.*;

public final class FList<T> {

    public final T head;
    public final FList<T> tail;
    public final int length;
    public final boolean isEmpty;

    private FList() {
        head = null;
        tail = null;
        isEmpty = true;
        length = 0;
    }

    private FList(T i, FList<T> n) {
        head = i;
        tail = n;
        isEmpty = false;
        length = n.length + 1;
    }

    public FList<T> add(T item) {
        return new FList<>(item, this);
    }

    @SuppressWarnings("unchecked")
    public FList<T> reverse() {
        return foldLeft(nil, (x, y) -> x.add(y));
    }

    public <U> U foldLeft(U i, BiFunction<U, T, U> f) {
        return isEmpty ? i : tail.foldLeft(f.apply(i, head), f);
    }

    public <U> U foldRight(U i, BiFunction<U, T, U> f) {
        return isEmpty ? i : f.apply(tail.foldRight(i, f), head);
    }

    @SuppressWarnings("unchecked")
    public FList<T> filter(Predicate<T> p) {
        return foldRight(nil, (x, y) -> p.test(y) ? x.add(y) : x);
    }

    public FList<T> remove(Predicate<T> p) {
        return filter(p.negate());
    }

    public boolean exist(Predicate<T> p) {
        return !isEmpty && (p.test(head) || tail.exist(p));
    }

    public boolean forAll(Predicate<T> p) {
        return !exist(p.negate());
    }

    // x+0 can avoid " Unboxing possibly null value "
    public int count(Predicate<T> p) {
        return foldLeft(0, (x, y) -> p.test(y) ? x + 1 : x + 0);
    }

    public FList<T> merge(FList<T> other) {
        return other == null || other.isEmpty ? this : other.foldRight(this, (x, y) -> x.add(y));
    }

    @Override
    public String toString() {
        return JSON.toJSON(this);
    }

    public String toJSON() {
        return JSON.toJSON(this);
    }

    public String toPrettyJSON() {
        return JSON.toPrettyJSON(this);
    }

    public FList<T> sort(Comparator<T> c) {
        return length > 20 ? msort(c) : isort(c);
    }
     

    @SuppressWarnings("unchecked")
    private FList<T> isort(Comparator<T> c) {
        return isEmpty ? nil : ins(c, head, tail.isort(c));
    }

    //not error,just get sublist not change Original FList 
    public FList<T> drop(int n) {
        return n <= 0 || isEmpty ? this : tail.drop(n - 1);
    }

    public void foreach(Consumer<T> a) {
        if (!isEmpty) {
            a.accept(head);
            tail.foreach(a);
        }
    }

    @SuppressWarnings("unchecked")
    public <R> FList<R> map(Function<T, R> map) {
        return foldRight(nil, (x, y) -> x.add(map.apply(y)));
    }

    public T reduce(T i, BinaryOperator<T> f) {
        return isEmpty ? i : tail.reduce(f.apply(i, head), f);
    }

    public static final FList nil = new FList<>();

    @SuppressWarnings("unchecked")
    public static <T> FList<T> of(T... args) {
        return a2f(args, args.length - 1);
    }

    @SuppressWarnings("unchecked")
    private static <T> FList<T> a2f(T[] a, int i) {
        return i < 0 ? nil : new FList<>(a[i], a2f(a, i - 1));
    }

    private static <T> FList<T> ins(Comparator<T> c, T h, FList<T> t) {
        return t.isEmpty || c.compare(h, t.head) > -1 ? t.add(h) : ins(c, h, t.tail).add(t.head);
    }

    public static <T> FList<T> generate(FList<T> r, Function<FList<T>, T> f, int m) {
        return m == 1 ? r : generate(new FList<>(f.apply(r), r), f, m - 1);
    }

    @SuppressWarnings({"unchecked", "empty-statement"})
    public static <T> FList<T> iterate(T s, UnaryOperator<T> f, int m) {

        FList<T> n;
        for (n = FList.of(s); m > 1; m--, s = f.apply(s), n = n.add(s));
        return n;
    }

    private static <T> Couple<FList<T>, FList<T>> split(FList<T> x, FList<T> y, int n) {
        if (x.isEmpty || n == 0)
            return Tuple.of(x, y);
        return split(x.tail, y.add(x.head), n - 1);
    }

    private static <T> FList<T> merge(FList<T> x, FList<T> y, Comparator<T> c) {
        if (x.isEmpty)
            return y;
        if (y.isEmpty)
            return x;
        return c.compare(x.head, y.head) > -1 ? merge(x.tail, y, c).add(x.head) : merge(x, y.tail, c).add(y.head);
    }

    @SuppressWarnings("unchecked")
    private FList<T> msort(Comparator<T> c) {
        int n = length / 2;
        if (n == 0)
            return this;
        else {
            Couple<FList<T>, FList<T>> p = split(this, nil, n);
            return merge(p._1.msort(c), p._2.msort(c), c);
        }
    }
}
//</editor-fold>
