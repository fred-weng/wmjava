package j.i;

/**
 * Tuple : Two Three Author : Fred Weng
 */
//<editor-fold>

public abstract class Tuple {

    public static <X, Y> Couple<X, Y> of(X x, Y y) {
        return new Couple<>(x, y);
    }

    public static <X, Y, Z> Triad<X, Y, Z> of(X x, Y y, Z z) {
        return new Triad<>(x, y, z);
    }
}
//</editor-fold>
