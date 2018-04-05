package org.softwire.training.rawImageStore;

@FunctionalInterface
public interface ImageUrlBuilder {
    String build(String filename);
}
