package redis.clients.jedis.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {
    public static Map parseMap(List<String> list) {
        if (list == null || list.size() == 0) return null;
        Map<String, String> map = new HashMap<>();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            map.put(iterator.next(), iterator.next());
        }
        return map;
    }
}
