package redis.clients.jedis;

import java.util.Map;

public class StreamEntry {

    private String entryId;

    private Map<String, String> entry;

    public String getEntryId() {
        return entryId;
    }

    public Map<String, String> getEntry() {
        return entry;
    }

    public StreamEntry(String entryId, Map entry) {
        this.entryId = entryId;
        this.entry = entry;
    }

    @Override
    public String toString() {
        return String.format("EntryId: %s, Entry: %s", entryId, entry);
    }
}
