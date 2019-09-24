package demo;

import j.m.JSON;
import j.m.PType;
import j.m.XList;
import j.m.XMap;
import j.v.Range;
import j.v.RegExp;
import j.v.Require;
import j.v.Valid;
import java.math.BigDecimal;
import static java.util.Comparator.comparing;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        list_query();
        deserialization_verification();
        generic_deserialization();
        functional_query();
    }

    private static void list_query() {

        String data = "{'students':["
                + "{'id':1,'name':'a','age':22,'money':2000.00},"
                + "{'id':2,'name':'b','age':45,'money':2849.99},"
                + "{'id':3,'name':'c','age':35,'money':2356.50},"
                + "{'id':4,'name':'d','age':55,'money':10000.00},"
                + "{'id':5,'name':'e','age':35,'money':3478.99},"
                + "{'id':6,'name':'f','age':35,'money':3999.99},"
                + "{'id':7,'name':'g','age':35,'money':2400.00},"
                + "{'id':8,'name':'k','age':22,'money':1500.99},"
                + "{'id':8,'name':'h','age':22,'money':1500.99}"
                + "]}";
        //这里展现了本工具包提供的最实用的JSON解析功能耗时：10ms，fastjson-1.2.58：90ms （配置 intel core i5-7200 2.5G 内存8G）
        XList list = XMap.fromJSON(data).getXList("students");
        System.out.println(list.path("[3]{name}")); // d
        System.out.println(list.project("id"));//[1, 2, 3, 4, 5, 6, 7, 8]
        System.out.println(list.getXMap(3).getInt("age") == 55);//true
        //真实项目中list来源于数据库或远程API调用，同样可以执行如下的列表sql查询操作
        String s = list.select("(money>1000 and age<30) or (money>3000 and age<40) order by -f:money,i:age,s:name").toPrettyJSON();
        System.out.println(s);
        //支持更复杂的统计和排序功能
        list.select("(money>1000 and age<30) or (money>3000 and age<40) group by age for count,sum money,avg money order by -i:count, +f:sum_money").tableFormat();
        //支持两个数据集的innerJoin和leftJoin,例如 xxx 数据集和 yyy 数据集 按 product_id 关联
        //s = xxx.innerJoin(yyy).select("status==true and sale_price>0"), "product_id").toPrettyJSON();
    }

    private static void deserialization_verification() {

        V v = XMap.fromJSON("{x:999,y:'123456789',z:1999,email:'ab777777777777777777c@163.com',ids:[1,2,3,4],d:123.456}").toObject(V.class);
        System.out.println(JSON.toJSON(v));

    }

    private static void generic_deserialization() {
        A<Double[], Map<String, Integer>> a = JSON.parseObject("{'a':[1,3,5.6,7.8],'b':{'id':123,'age':34}}",
                PType.compose(A.class, Double[].class, PType.compose(Map.class, String.class, Integer.class)));
        System.out.println(JSON.toJSON(a));
    }
    
    
    private static void functional_query(){
        
        String data = "{'students':["
                + "{'id':1,'name':'a','age':22,'money':2000.00},"
                + "{'id':2,'name':'b','age':45,'money':2849.99},"
                + "{'id':3,'name':'c','age':35,'money':2356.50},"
                + "{'id':4,'name':'d','age':55,'money':10000.00},"
                + "{'id':5,'name':'e','age':35,'money':3478.99},"
                + "{'id':6,'name':'f','age':35,'money':3999.99},"
                + "{'id':7,'name':'g','age':35,'money':2400.00},"
                + "{'id':8,'name':'k','age':22,'money':1500.99},"
                + "{'id':8,'name':'h','age':22,'money':1500.99}"
                + "]}";
        
        XList<XMap> list = XMap.<XMap>fromJSON(data).getXList("students");
        list.toObjectList(Emp.class).parallelStream()
                .filter(p -> (p.getM() > 1000 && p.getAge() < 30) || (p.getM() > 3000 && p.getAge() < 40))
                .sorted(comparing(Emp::getM)
                        .reversed()
                        .thenComparing(comparing(Emp::getAge))
                        .thenComparing(comparing(Emp::getName)))
                .collect(XList.collector())
                .tableFormat();
    }

}

class Emp {
    private int id, age;
    private String name;
    private double m;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public double getM() {
        return m;
    }
    public void setM(double m) {
        this.m = m;
    }
}

class A<T, U> {

    private T a;
    private U b;

    public T getA() {
        return a;
    }

    public void setA(T a) {
        this.a = a;
    }

    public U getB() {
        return b;
    }

    public void setB(U b) {
        this.b = b;
    }
}

@Valid
class V {

    @Range(value = "(,1000]", desc = "x的值不可大于1000!")// x<=1000
    private int x;
    @Range("[9,)") //y.length>=9
    private String y;
    @Range("[1000,2000)")// z>=1000 && z <2000
    private java.math.BigDecimal z;
    @Require("必须填写邮件地址！")
    @Range("(,30)") //email.length<30
    @RegExp("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$") //email format check
    private String email;
    @Require//ids!=null
    @Range("(1,5)")//ids.size>1 && ids.size<5
    private List<Integer> ids;
    @Range("[123.456,200)")//d>=123.456 && d<200
    private double d;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public BigDecimal getZ() {
        return z;
    }

    public void setZ(BigDecimal z) {
        this.z = z;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }
}
