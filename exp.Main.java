package exp;

import j.m.JSON;
import j.m.PType;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        A<List<Double>> a = JSON.parseObject("{x:1,y:[1,2,3,4]}", PType.compose(A.class, PType.compose(List.class, Double.class)));

        System.out.println(a.getY());
    }
}

class A<T> {

    private int x;
    private T y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public T getY() {
        return y;
    }

    public void setY(T y) {
        this.y = y;
    }
}