package org.softwire.training.models;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

@SuppressWarnings("unused")
public class Image {
    private final Integer id;
    private final String user;
    private final String author;
    private final String name;
    private final String filename;
    private final License license;
    private final Integer score;
    private final byte[] thumbnail;

    @JdbiConstructor
    public Image(@ColumnName("id") Integer id,
                 @ColumnName("user") String user,
                 @ColumnName("author") String author,
                 @ColumnName("name") String name,
                 @ColumnName("filename") String filename,
                 @ColumnName("license") License license,
                 @ColumnName("score") Integer score,
                 @ColumnName("thumbnail") byte[] thumbnail) {
        this.id = id;
        this.user = user;
        this.author = author;
        this.name = name;
        this.filename = filename;
        this.license = license;
        this.score = score;
        this.thumbnail = thumbnail;
    }

    public Integer getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public License getLicense() {
        return license;
    }

    public Integer getScore() {
        return score;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }
}
