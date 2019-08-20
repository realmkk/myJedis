package redis.clients.jedis;

import java.util.List;

public class CommandTest {
    public static void main(String[] args) {
        MyJedis myJedis = new MyJedis();

        System.out.println(myJedis.executeClientCommand("xrange Queue_SQL_handler_02 - +"));
//        list.forEach(x -> System.out.println(x + " : " + myJedis.executeClientCommand("type " + x)));

//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 10000; i++)
////            myJedis.get("hello");
//            myJedis.hgetall("helloHash");
//        System.out.println(System.currentTimeMillis() - start);
    }
}
