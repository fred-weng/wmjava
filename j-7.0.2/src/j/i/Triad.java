/**
 * restructure Triad must change N.Triad package path
 */

package j.i;

import j.m.JSON;

public class Triad<X, Y, Z> {

    public final X _1;
    public final Y _2;
    public final Z _3;
    

    Triad(X x, Y y, Z z) {
        this._1 = x;
        this._2 = y;
        this._3 = z;
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
