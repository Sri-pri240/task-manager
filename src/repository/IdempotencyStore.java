package repository;

import api.ApiResponse;
import exception.ConflictException;
import java.util.HashMap;
import java.util.Map;

public class IdempotencyStore {
    private static IdempotencyStore instance;

    private static class Record {
        final String fingerprint;
        final ApiResponse response;
        Record(String fingerprint, ApiResponse response) {
            this.fingerprint = fingerprint;
            this.response = response;
        }
    }

    private final Map<String, Record> store = new HashMap<>();

    private IdempotencyStore() {}

    public static synchronized IdempotencyStore getInstance() {
        if (instance == null) {
            instance = new IdempotencyStore();
        }
        return instance;
    }

    public synchronized ApiResponse check(String key, String fingerprint) {
        if (key == null || key.isBlank()) return null;
        Record existing = store.get(key);
        if (existing == null) return null;

        if (!existing.fingerprint.equals(fingerprint)) {
            throw new ConflictException("Idempotency key reused with different request payload");
        }
        return existing.response;
    }

    public synchronized void save(String key, String fingerprint, ApiResponse response) {
        if (key != null && !key.isBlank()) {
            store.put(key, new Record(fingerprint, response));
        }
    }
}

