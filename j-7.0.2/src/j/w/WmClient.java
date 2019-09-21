//<editor-fold>
package j.w;

import j.i.*;
import j.m.*;
import j.u.*;
import j.v.*;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WmClient {

    public String value() default ""; //if empty return method name
}

class WsException extends RuntimeException {

    WsException(String message) {
        super(message);
    }
}

//<editor-fold defaultstate="collapsed" desc="Example-WS">
@WS(value = "Example-WS", desc = "demo of ws")
class ExpWS {

    @WM
    String hello() {
        return "hello world!";
    }

    @WM("hello-name")
    String greeting(String name) {
        return StrU.fastFormat("hello {0}!", name);
    }

    @WM("add.wm")
    int add(int x, int y) {
        return x + y;
    }

    @WM(desc = "get the value of x div y . may produce an '/ by zero' Exception")
    int div(int x, int y) {
        return x / y;
    }

    @WM
    XList<Stu> getStuList() {

        return XList.fromJSON("["
                + "{'id':1,'name':'aaa','age':30},"
                + "{'id':2,'name':'bbb','age':22},"
                + "{'id':3,'name':'ccc','age':40}"
                + "]").toObjectList(Stu.class);
        //or toObjectList(Stu.class);
    }

    private static class Stu {

        private int id;
        private String name;
        private Integer age;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    @WM
    Couple<String, XList<String>> getCouple(String name, XList<String> favs) {
        return Tuple.of(name, favs);
    }

    @WM
    @SuppressWarnings("unchecked")
    XList<XList<Integer>> getListList() {
        return XList.of(XList.of(1, 2, 3), XList.of(4, 5, 6), XList.of(7, 8, 9));
    }

    @WM
    String queryString(XMap params) {

        String name = params.getString("name");

        if (name == null)
            return "set name value of the parameter by appending <a href='?name=xxx'>?name=xxx</a> "
                    + "to url in the browser address bar plsease.";

        return StrU.fastFormat("name from queryString is {0}.", name);
    }

    @WM(desc = "method signature recommended by the author.")
    XResult<Integer> bornYear(XMap params) {
        return XResult.succeed(params.toObject(Biz.class).getBornYear());
    }

    @Valid
    private static class Biz {

        @Require("age value can not be null, "
                + "\r\n<br/>when rpc mode set parameter of the method with a Map including age key and value , "
                + "\r\n<br/>when testing on browser mode set it by appending <a href='?age=28'>?age=28</a> to url.")
        @Range("[0,120]")
        private Integer age;

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @SuppressWarnings("deprecation")
        public Integer getBornYear() {
            return XDate.getNow().getYear() + 1900 - age;
        }
    }
}
//</editor-fold>

//</editor-fold>
