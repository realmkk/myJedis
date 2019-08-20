package redis.clients.jedis.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ArrayUtils {
    public static  <T> T[] addToHead(Class<T> type, T x, T[] array) {
        T[] newArray = (T[])Array.newInstance(type, array.length + 1);
        newArray[0] = x;
        System.arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

    public static String[] addStringToHead(String str, String[] array) {
        return addToHead(String.class, str, array);
    }

    public static String[] addStringsToHead(String[] tail, String... head) {
        String[] array = new String[tail.length + head.length];
        System.arraycopy(head, 0, array, 0, head.length);
        System.arraycopy(tail, 0, array, head.length, tail.length);
        return array;
    }

    public static  <T> List<T> of(T... args) {
        return Arrays.asList(args);
    }
}
