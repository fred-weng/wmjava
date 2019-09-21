/**
 * restructure Triad must change N.Couple package path
 */

package j.i;

import j.m.JSON;

public class Couple<X, Y> {

    public final X _1;
    public final Y _2;

    Couple(X x, Y y) {
        this._1 = x;
        this._2 = y;
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
}
