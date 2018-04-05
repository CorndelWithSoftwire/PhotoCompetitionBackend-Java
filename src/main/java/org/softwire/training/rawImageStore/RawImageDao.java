package org.softwire.training.rawImageStore;

import java.io.InputStream;
import java.util.List;

public interface RawImageDao {
    void put(String key, InputStream inputStream, String contentType, long contentLength);

    void delete(String key);

    void delete(List<String> keys);

    void ping();
}
