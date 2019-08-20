package redis.clients.jedis;

public class MyJedisTest {
    public static void main(String[] args) {
//        MyJedis myJedis = new MyJedis("192.168.230.130:6380/redis");
        MyJedis myJedis = new MyJedis();
//        System.out.println(myJedis.set("hello", "$world\r\n\f\t\b"));
//        String hello = myJedis.get("hello");
//        System.out.println(hello);

        String hashKey = "helloHash";
//        System.out.println(myJedis.del(hashKey));
//        System.out.println(myJedis.hset(hashKey, "hello", "world"));
        System.out.println(myJedis.hget("helloHash", "hello"));

        System.out.println(myJedis.hmset(hashKey, "a", "b", "c", "", "e", "f"));
        System.out.println(myJedis.hmget(hashKey, "a", "b", "a"));
        System.out.println(myJedis.hgetall(hashKey));

        System.out.println(myJedis.xrange("Queue_SQL_handler_02", "-", "+"));
    }
}
