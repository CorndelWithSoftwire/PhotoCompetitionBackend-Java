package org.softwire.training.integration.helpers;

import com.google.common.io.ByteStreams;
import org.softwire.training.rawImageStore.RawImageDao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;

public class MockRawImageDao implements RawImageDao {
    private final HashMap<String, byte[]> data = new HashMap<>();

    @Override
    public void put(String key, InputStream inputStream, String contentType, long contentLength) {
        try {
            data.put(key, ByteStreams.toByteArray(inputStream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void delete(String key) {
        data.remove(key);
    }

    @Override
    public void delete(List<String> keysToDelete) {
        keysToDelete.forEach(data::remove);
    }

    @Override
    public void ping() {
    }

    public byte[] get(String key) {
        return data.get(key);
    }
}
