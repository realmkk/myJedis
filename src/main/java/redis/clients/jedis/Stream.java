package redis.clients.jedis;

import java.util.ArrayList;
import java.util.List;

public class Stream {
    private List<StreamEntry> stream;
    private int length;

    public Stream() {
        this.stream = new ArrayList<>();
        length = 0;
    }

    public void add(StreamEntry streamEntry) {
        this.stream.add(streamEntry);
        length++;
    }

    public StreamEntry get(int index) {
        return stream.get(index);
    }

    public int length() {
        return length;
    }

    @Override
    public String toString() {
        if (stream == null || stream.size() == 0) return "";
        StringBuffer sb = new StringBuffer();
        stream.forEach(entry -> {
            sb.append(entry);
            sb.append("\n");
        });
        sb.setLength(sb.length()-1);
        return sb.toString();
    }
}
